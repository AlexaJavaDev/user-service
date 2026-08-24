package com.example.console;

import java.util.Scanner;
import java.util.regex.Pattern;

public class UserConsoleInput {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Zа-яА-ЯёЁ ]{3,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.(com|ru)$");
    private static final int MAX_EMAIL_LENGTH = 50;

    public String promptName(Scanner scanner, boolean allowEmpty) {
        while (true) {
            String input = scanner.nextLine().trim();
            if (allowEmpty && input.isEmpty()) return null;
            if (NAME_PATTERN.matcher(input).matches() && input.chars().filter(ch -> ch == ' ').count() <= 3) {
                return input;
            }
            System.out.println("Ошибка: имя от 3 до 50 букв, максимум 3 пробела.");
        }
    }

    public String promptEmail(Scanner scanner, boolean allowEmpty) {
        while (true) {
            String input = scanner.nextLine().trim();
            if (allowEmpty && input.isEmpty()) return null;
            if (input.length() <= MAX_EMAIL_LENGTH && EMAIL_PATTERN.matcher(input).matches()) {
                return input;
            }
            System.out.println("Ошибка: неверный формат email (.com/.ru, до 50 символов).");
        }
    }

    public Integer promptAge(Scanner scanner, boolean allowEmpty) {
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                if (allowEmpty) return null;
                System.out.println("Ошибка: возраст не может быть пустым.");
                continue;
            }
            try {
                int age = Integer.parseInt(input);
                if (age >= 10 && age <= 80) return age;
                System.out.println("Ошибка: возраст от 10 до 80.");
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }

    public Long promptId(Scanner scanner, String action) {
        while (true) {
            System.out.print("Введите ID (или 'q' для отмены): ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("q")) {
                System.out.println("Действие \"" + action + "\" отменено.");
                return null;
            }
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: ID должен быть числом.");
            }
        }
    }

    public boolean confirmDelete(Scanner scanner, String name) {
        System.out.print("Удалить пользователя " + name + "? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        return confirm.equals("y") || confirm.equals("yes");
    }
}

