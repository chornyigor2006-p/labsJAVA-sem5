package org.example;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

//  модель адреси
class Address {
    private final String street;
    private final String houseNumber;
    private final String apartmentNumber;

    public Address(String street, String houseNumber, String apartmentNumber) {
        this.street = street;
        this.houseNumber = houseNumber;
        this.apartmentNumber = apartmentNumber;
    }

    @Override
    public String toString() {
        return String.format("вул. %s, буд. %s, кв. %s", street, houseNumber, apartmentNumber);
    }
}

// запис в журналі куратора
class CuratorJournalEntry {
    private final String lastName;
    private final String firstName;
    private final LocalDate birthDate;
    private final String phone;
    private final Address address;

    public CuratorJournalEntry(String lastName, String firstName, LocalDate birthDate, String phone, Address address) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthDate = birthDate;
        this.phone = phone;
        this.address = address;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return String.format("Студент: %s %s | Дата народження: %s | Телефон: %s | Адреса: %s",
                lastName, firstName, birthDate.format(formatter), phone, address);
    }
}

// головний клас з інтерфейсом користувача та валідацією
public class Main {
    private static Scanner scanner;
    private static final List<CuratorJournalEntry> journal = new ArrayList<>();

    // вирази для валідації
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zА-Яа-яІіЇїЄєҐґ'\\-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+380|0)\\d{9}$");
    private static final Pattern HOUSE_PATTERN = Pattern.compile("^[0-9]+[A-Za-zА-Яа-яІіЇїЄєҐґ]?(/[0-9]+)?$");
    private static final Pattern APARTMENT_PATTERN = Pattern.compile("^[0-9]+[A-Za-zА-Яа-яІіЇїЄєҐґ]?$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static void main(String[] args) {
        // Примусово встановлюю UTF-8 тк воно блін не працює нормально
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        while (true) {
            System.out.println("\n--- Меню журналу куратора ---");
            System.out.println("1. Додати новий запис");
            System.out.println("2. Показати всі записи");
            System.out.println("0. Вихід");
            System.out.print("Виберіть дію: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    createNewEntry();
                    break;
                case "2":
                    displayJournal();
                    break;
                case "0":
                    System.out.println("Роботу програми завершено.");
                    return;
                default:
                    System.out.println("Невірний вибір. Спробуйте ще раз.");
            }
        }
    }

    private static void createNewEntry() {
        System.out.println("\n--- Введення даних студента ---");

        String lastName = readValidatedInput(
                "Введіть прізвище: ",
                input -> NAME_PATTERN.matcher(input).matches(),
                "Помилка! Прізвище має містити лише літери."
        );

        String firstName = readValidatedInput(
                "Введіть ім'я: ",
                input -> NAME_PATTERN.matcher(input).matches(),
                "Помилка! Ім'я має містити лише літери."
        );

        LocalDate birthDate = readValidatedDate(
                "Введіть дату народження (у форматі ДД.ММ.РРРР): "
        );

        String phone = readValidatedInput(
                "Введіть телефон (+380XXXXXXXXX або 0XXXXXXXXX): ",
                input -> PHONE_PATTERN.matcher(input).matches(),
                "Помилка! Некоректний формат телефону."
        );

        System.out.println("\n-- Введення адреси проживання --");
        String street = readValidatedInput(
                "Введіть назву вулиці: ",
                input -> !input.isBlank(),
                "Помилка! Назва вулиці не може бути порожньою."
        );

        String houseNumber = readValidatedInput(
                "Введіть номер будинку (наприклад: 10, 15А, 12/1): ",
                input -> HOUSE_PATTERN.matcher(input).matches(),
                "Помилка! Некоректний номер будинку."
        );

        String apartmentNumber = readValidatedInput(
                "Введіть номер квартири: ",
                input -> APARTMENT_PATTERN.matcher(input).matches(),
                "Помилка! Некоректний номер квартири (має бути число)."
        );

        Address address = new Address(street, houseNumber, apartmentNumber);
        CuratorJournalEntry entry = new CuratorJournalEntry(lastName, firstName, birthDate, phone, address);

        journal.add(entry);
        System.out.println("\nЗапис успішно додано до журналу!");
    }

    private static void displayJournal() {
        System.out.println("\n--- Усі записи журналу куратора ---");
        if (journal.isEmpty()) {
            System.out.println("Журнал наразі порожній.");
            return;
        }

        for (int i = 0; i < journal.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, journal.get(i));
        }
    }

    private static String readValidatedInput(String prompt, Validator validator, String errorMessage) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (validator.isValid(input)) {
                return input;
            }
            System.out.println(errorMessage + " Будь ласка, повторіть введення.");
        }
    }

    private static LocalDate readValidatedDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                LocalDate date = LocalDate.parse(input, DATE_FORMATTER);
                if (date.isAfter(LocalDate.now())) {
                    System.out.println("Помилка! Ви не з фільму назад у минуле. Повторіть введення.");
                    continue;
                }
                return date;
            } catch (DateTimeParseException e) {
                System.out.println("Помилка! Некоректна дата або формат (очікується ДД.ММ.РРРР). Повторіть введення.");
            }
        }
    }

    @FunctionalInterface
    interface Validator {
        boolean isValid(String value);
    }
}