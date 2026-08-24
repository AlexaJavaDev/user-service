package com.example.console;

import com.example.entity.User;
import com.example.exception.UserAlreadyExistsException;
import com.example.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserConsoleApp {
    private final UserService userService;
    private final UserConsoleInput input = new UserConsoleInput();
    private final Scanner scanner = new Scanner(System.in);

    public UserConsoleApp(UserService userService) {
        this.userService = userService;
    }

    public void run() {
        boolean exit = false;
        while (!exit) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createUser();
                case "2" -> findUser();
                case "3" -> listAllUsers();
                case "4" -> updateUser();
                case "5" -> deleteUser();
                case "6" -> {
                    exit = true;
                    System.out.println("Выход.");
                }
                default -> System.out.println("Неверный ввод.");
            }
        }
        scanner.close();
    }

    private void printMenu() {
        System.out.println("\n=== Управление пользователями ===");
        System.out.println("1. Создать");
        System.out.println("2. Найти по ID");
        System.out.println("3. Показать всех");
        System.out.println("4. Обновить");
        System.out.println("5. Удалить");
        System.out.println("6. Выход");
        System.out.print("Ваш выбор: ");
    }

    private void createUser() {
        try {
            System.out.print("Имя: ");
            String name = input.promptName(scanner, false);
            System.out.print("Email: ");
            String email = input.promptEmail(scanner, false);
            System.out.print("Возраст (Enter - пусто): ");
            Integer age = input.promptAge(scanner, true);

            User user = new User(name, email, age);
            userService.create(user);
            System.out.println("Создан: " + user);
        } catch (UserAlreadyExistsException e) {
            System.out.println("Ошибка бизнес-логики: " + e.getMessage());
        }
    }

    private void findUser() {
        Long id = input.promptId(scanner, "поиск");
        if (id == null) return;

        Optional<User> user = userService.findById(id);
        user.ifPresentOrElse(
                u -> System.out.println("Найден: " + u),
                () -> System.out.println("Не найден.")
        );
    }

    private void listAllUsers() {
        List<User> users = userService.findAll();
        if (users.isEmpty()) System.out.println("Список пуст.");
        else users.forEach(System.out::println);
    }

    private void updateUser() {
        Long id = input.promptId(scanner, "обновление");
        if (id == null) return;

        Optional<User> optional = userService.findById(id);
        if (optional.isEmpty()) {
            System.out.println("Не найден.");
            return;
        }

        User user = optional.get();
        System.out.println("Текущие данные: " + user);

        System.out.print("Новое имя (" + user.getName() + "): ");
        String name = input.promptName(scanner, true);
        if (name != null) user.setName(name);

        System.out.print("Новый Email (" + user.getEmail() + "): ");
        String email = input.promptEmail(scanner, true);
        if (email != null) user.setEmail(email);

        System.out.print("Новый возраст (" + user.getAge() + "): ");
        Integer age = input.promptAge(scanner, true);
        if (age != null) user.setAge(age);

        try {
            userService.update(user); // Идем через СЕРВИС
            System.out.println("Обновлен: " + user);
        } catch (UserAlreadyExistsException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void deleteUser() {
        Long id = input.promptId(scanner, "удаление");
        if (id == null) return;

        Optional<User> optional = userService.findById(id);
        if (optional.isEmpty()) {
            System.out.println("Не найден.");
            return;
        }

        if (input.confirmDelete(scanner, optional.get().getName())) {
            userService.delete(id);
            System.out.println("Удален.");
        } else {
            System.out.println("Отменено.");
        }
    }
}
