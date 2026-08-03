package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ConsolApp {

    private static final Logger logger = LoggerFactory.getLogger(ConsolApp.class);
    private final UserDao userDao = new UserDaoImplem();

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[a-zA-Zа-яА-ЯёЁ\\s]{3,50}$"
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.(com|ru)$"
    );
    private static final int MAX_EMAIL_LENGTH = 50;

    public static void main(String[] args) {
        ConsolApp app = new ConsolApp();
        try {
            app.run();
        } finally {
            if (app.userDao instanceof UserDaoImplem) {
                ((UserDaoImplem) app.userDao).close();
            }
        }
    }

    private void run() {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    createUser(scanner);
                    break;
                case "2":
                    findUserById(scanner);
                    break;
                case "3":
                    listAllUsers();
                    break;
                case "4":
                    updateUser(scanner);
                    break;
                case "5":
                    deleteUser(scanner);
                    break;
                case "6":
                    exit = true;
                    System.out.println("Выход из приложения.");
                    break;
                default:
                    System.out.println("Неверный ввод. Пожалуйста, выберите пункт от 1 до 6.");
            }
        }
        scanner.close();
    }

    private void printMenu() {
        System.out.println("\n=== Управление пользователями ===");
        System.out.println("1. Создать нового пользователя");
        System.out.println("2. Найти пользователя по ID");
        System.out.println("3. Показать всех пользователей");
        System.out.println("4. Обновить пользователя");
        System.out.println("5. Удалить пользователя");
        System.out.println("6. Выход");
        System.out.print("Выберите действие: ");
    }

    // Валидация
    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        if (!NAME_PATTERN.matcher(name).matches()) return false;
        long spaces = name.chars().filter(ch -> ch == ' ').count();
        return spaces <= 3;
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        if (email.length() > MAX_EMAIL_LENGTH) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidAge(Integer age) {
        if (age == null) return true;
        return age >= 10 && age <= 80;
    }

    // Повторный запрос
    private Long promptId(Scanner scanner, String actionName) {
        while (true) {
            System.out.print("Введите ID пользователя (или 'q' для отмены): ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("q") || input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                System.out.println("Действие \"" + actionName + "\" отменено.");
                return null;
            }
            if (input.isEmpty()) {
                System.out.println("ID не может быть пустым. Повторите ввод.");
                continue;
            }
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: ID должен быть целым числом. Повторите ввод.");
            }
        }
    }

    private String promptName(Scanner scanner, boolean allowEmpty) {
        while (true) {
            String input = scanner.nextLine().trim();
            if (allowEmpty && input.isEmpty()) {
                return null;
            }
            if (isValidName(input)) {
                return input;
            }
            System.out.println("Ошибка: имя должно содержать от 3 до 50 букв (латиница или кириллица) и не более 3 пробелов.");
            System.out.print("Повторите ввод имени" + (allowEmpty ? " (или оставьте пустым, чтобы пропустить): " : ": "));
        }
    }

    private String promptEmail(Scanner scanner, boolean allowEmpty) {
        while (true) {
            String input = scanner.nextLine().trim();
            if (allowEmpty && input.isEmpty()) {
                return null;
            }
            if (isValidEmail(input)) {
                return input;
            }
            System.out.println("Ошибка: неверный формат email (допустимы латиница, цифры, @ . - _ ; длина до 50, домен .com или .ru).");
            System.out.print("Повторите ввод email" + (allowEmpty ? " (или оставьте пустым, чтобы пропустить): " : ": "));
        }
    }

    private Integer promptAge(Scanner scanner, boolean allowEmpty) {
        while (true) {
            String input = scanner.nextLine().trim();
            if (allowEmpty && input.isEmpty()) {
                return null;
            }
            if (input.isEmpty()) {
                return null; // возраст не обязателен
            }
            try {
                int age = Integer.parseInt(input);
                if (isValidAge(age)) {
                    return age;
                }
                System.out.println("Ошибка: возраст должен быть числом от 10 до 80.");
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: возраст должен быть целым числом.");
            }
            System.out.print("Повторите ввод возраста" + (allowEmpty ? " (или оставьте пустым, чтобы пропустить): " : " (или оставьте пустым, если не хотите указывать): "));
        }
    }

    // Формат вывода
    private void printUserDetails(User user) {
        System.out.println("-------------------------");
        System.out.println("ID: " + user.getId());
        System.out.println("Имя: " + user.getName());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Возраст: " + (user.getAge() != null ? user.getAge() : "не указан"));
        if (user.getCreatedAt() != null) {
            System.out.println("Создан: " + user.getCreatedAt().toLocalDate());
        } else {
            System.out.println("Создан: не указан");
        }
        System.out.println("-------------------------");
    }

    // crud
    private void createUser(Scanner scanner) {
        try {
            System.out.print("Введите имя (3-50 символов, только буквы, не более 3 пробелов): ");
            String name = promptName(scanner, false);

            System.out.print("Введите email (до 50 символов, латиница, цифры, @, ., -, _, обязательно @ и .com/.ru): ");
            String email = promptEmail(scanner, false);

            System.out.print("Введите возраст (10-80, или оставьте пустым): ");
            Integer age = promptAge(scanner, false);

            User user = new User(name, email, age);
            userDao.create(user);
            System.out.println("Пользователь успешно создан:");
            printUserDetails(user);

        } catch (Exception e) {
            logger.error("Ошибка при создании пользователя: {}", e.getMessage(), e);
            System.out.println("Ошибка при создании пользователя. Проверьте логи.");
        }
    }

    private void findUserById(Scanner scanner) {
        try {
            Long id = promptId(scanner, "поиск пользователя");
            if (id == null) return;

            System.out.print("Поиск пользователя по id " + id + ": ");
            Optional<User> optional = userDao.findById(id);
            if (optional.isPresent()) {
                System.out.println();
                printUserDetails(optional.get());
            } else {
                System.out.println("null");
                System.out.println("Пользователь с ID " + id + " не найден.");
            }
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя: {}", e.getMessage(), e);
            System.out.println("Ошибка при поиске. Проверьте логи.");
        }
    }

    private void listAllUsers() {
        try {
            List<User> users = userDao.findAll();
            if (users.isEmpty()) {
                System.out.println("Список пользователей пуст.");
            } else {
                System.out.println("Список всех пользователей (всего " + users.size() + "):");
                for (User user : users) {
                    printUserDetails(user);
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении списка пользователей: {}", e.getMessage(), e);
            System.out.println("Ошибка при получении списка. Проверьте логи.");
        }
    }

    private void updateUser(Scanner scanner) {
        try {
            Long id = promptId(scanner, "обновление пользователя");
            if (id == null) return;

            Optional<User> optional = userDao.findById(id);
            if (optional.isEmpty()) {
                System.out.println("Пользователь с ID " + id + " не найден.");
                return;
            }

            User user = optional.get();
            System.out.println("Текущие данные:");
            printUserDetails(user);
            System.out.println("Введите новые данные (оставьте пустым, чтобы не менять):");

            System.out.print("Имя (" + user.getName() + "): ");
            String newName = promptName(scanner, true);
            if (newName != null) {
                user.setName(newName);
            }

            System.out.print("Email (" + user.getEmail() + "): ");
            String newEmail = promptEmail(scanner, true);
            if (newEmail != null) {
                user.setEmail(newEmail);
            }

            System.out.print("Возраст (" + (user.getAge() != null ? user.getAge() : "не указан") + "): ");
            Integer newAge = promptAge(scanner, true);
            if (newAge != null) {
                user.setAge(newAge);
            }

            userDao.update(user);
            System.out.println("Пользователь обновлён:");
            printUserDetails(user);

        } catch (Exception e) {
            logger.error("Ошибка при обновлении пользователя: {}", e.getMessage(), e);
            System.out.println("Ошибка при обновлении. Проверьте логи.");
        }
    }

    private void deleteUser(Scanner scanner) {
        try {
            Long id = promptId(scanner, "удаление пользователя");
            if (id == null) return;

            Optional<User> optional = userDao.findById(id);
            if (optional.isEmpty()) {
                System.out.println("Пользователь с ID " + id + " не найден.");
                return;
            }

            System.out.print("Вы уверены, что хотите удалить пользователя " + optional.get().getName() + "? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if ("y".equals(confirm) || "yes".equals(confirm)) {
                userDao.delete(id);
                System.out.println("Пользователь с ID " + id + " удалён.");
            } else {
                System.out.println("Удаление отменено.");
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении пользователя: {}", e.getMessage(), e);
            System.out.println("Ошибка при удалении. Проверьте логи.");
        }
    }
}
