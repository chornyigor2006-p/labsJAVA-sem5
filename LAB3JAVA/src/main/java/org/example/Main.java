package org.example;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

enum Category {
    PUPIL("Учнівська"),
    STUDENT("Студентська"),
    STANDARD("Звичайна");

    private final String title;
    Category(String title) { this.title = title; }
    public String getTitle() { return title; }
}

enum CardType {
    MONTHLY_SUBSCRIPTION("На місяць"),
    TEN_DAYS_SUBSCRIPTION("На 10 днів"),
    TRIP_LIMITED("Обмежена поїздками"),
    ACCUMULATIVE("Накопичувальна ");

    private final String title;
    CardType(String title) { this.title = title; }
    public String getTitle() { return title; }
}

// клас картки

class TravelCard {
    private final String id;
    private final Category category;
    private final CardType cardType;

    private LocalDate expiryDate; // null для накопичувальних
    private int remainingTrips;   // для карток з лімітом поїздок
    private double balance;       // для накопичувальних карток

    private TravelCard(String id, Category category, CardType cardType) {
        this.id = id;
        this.category = category;
        this.cardType = cardType;
    }

    public static TravelCard createTimeSubscription(String id, Category category, CardType type, int daysValid) {
        TravelCard card = new TravelCard(id, category, type);
        card.expiryDate = LocalDate.now().plusDays(daysValid);
        return card;
    }

    public static TravelCard createTripLimited(String id, Category category, int trips) {
        TravelCard card = new TravelCard(id, category, CardType.TRIP_LIMITED);
        card.remainingTrips = trips;
        return card;
    }

    public static TravelCard createAccumulative(String id, double initialBalance) {
        TravelCard card = new TravelCard(id, Category.STANDARD, CardType.ACCUMULATIVE);
        card.balance = initialBalance;
        return card;
    }

    public String getId() { return id; }
    public Category getCategory() { return category; }
    public CardType getCardType() { return cardType; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public int getRemainingTrips() { return remainingTrips; }
    public double getBalance() { return balance; }

    public void deductTrip() {
        if (cardType == CardType.TRIP_LIMITED && remainingTrips > 0) {
            remainingTrips--;
        }
    }

    public void deductBalance(double amount) {
        if (cardType == CardType.ACCUMULATIVE && balance >= amount) {
            balance -= amount;
        }
    }

    public void topUpBalance(double amount) {
        if (cardType == CardType.ACCUMULATIVE && amount > 0) {
            balance += amount;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(id)
                .append(" | Тип: ").append(category.getTitle())
                .append(" (").append(cardType.getTitle()).append(")");

        if (cardType == CardType.MONTHLY_SUBSCRIPTION || cardType == CardType.TEN_DAYS_SUBSCRIPTION) {
            sb.append(" | Дійсна до: ").append(expiryDate);
        } else if (cardType == CardType.TRIP_LIMITED) {
            sb.append(" | Залишок поїздок: ").append(remainingTrips);
        } else if (cardType == CardType.ACCUMULATIVE) {
            sb.append(String.format(" | Баланс: %.2f грн", balance));
        }
        return sb.toString();
    }
}

// реестр

class CardRegistrySystem {
    private final Map<String, TravelCard> registeredCards = new LinkedHashMap<>();

    // Випуск за часом
    public TravelCard issueTimeCard(Category category, CardType type) {
        int days = (type == CardType.MONTHLY_SUBSCRIPTION) ? 30 : 10;
        String id = generateId();
        TravelCard card = TravelCard.createTimeSubscription(id, category, type, days);
        registeredCards.put(id, card);
        return card;
    }

    // Випуск за кількістю поїздок
    public TravelCard issueTripCard(Category category, int trips) {
        String id = generateId();
        TravelCard card = TravelCard.createTripLimited(id, category, trips);
        registeredCards.put(id, card);
        return card;
    }

    // Випуск накопичувальної картки
    public TravelCard issueAccumulativeCard(Category category, double initialBalance) {
        if (category != Category.STANDARD) {
            throw new IllegalArgumentException("Накопичувальні картки можуть бути тільки 'Звичайного' типу");
        }
        String id = generateId();
        TravelCard card = TravelCard.createAccumulative(id, initialBalance);
        registeredCards.put(id, card);
        return card;
    }

    public boolean isRegistered(String cardId) {
        return registeredCards.containsKey(cardId);
    }

    public TravelCard getCard(String cardId) {
        return registeredCards.get(cardId);
    }

    public Collection<TravelCard> getAllCards() {
        return registeredCards.values();
    }

    private String generateId() {
        return "CARD-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
    }
}

// тут всі процесси

class TurnstileProcessor {
    private final CardRegistrySystem registry;
    private final double farePrice; // Вартість однієї поїздки для накопичувальних карток

    private int totalPassesAllowed = 0;
    private int totalPassesDenied = 0;

    private final Map<Category, Integer> categoryAllowedStats = new EnumMap<>(Category.class);
    private final Map<Category, Integer> categoryDeniedStats = new EnumMap<>(Category.class);

    public TurnstileProcessor(CardRegistrySystem registry, double farePrice) {
        this.registry = registry;
        this.farePrice = farePrice;

        for (Category cat : Category.values()) {
            categoryAllowedStats.put(cat, 0);
            categoryDeniedStats.put(cat, 0);
        }
    }

    // Зчитування та перевірка картки на турнікеті
    public boolean processCard(TravelCard card) {
        // Спроба зчитування
        if (card == null || !registry.isRegistered(card.getId())) {
            recordDenied(Category.STANDARD);
            System.out.println("[ТУРНІКЕТ] ВІДМОВА. Картку не вдалося зчитати або вона відсутня в реєстрі .");
            return false;
        }

        Category category = card.getCategory();

        // Перевірка придатності картки
        switch (card.getCardType()) {
            case MONTHLY_SUBSCRIPTION:
            case TEN_DAYS_SUBSCRIPTION:
                if (card.getExpiryDate() == null || LocalDate.now().isAfter(card.getExpiryDate())) {
                    recordDenied(category);
                    System.out.println("[ТУРНІКЕТ] ВІДМОВА. Картка " + category.getTitle() + ": термін дії вичерпано.");
                    return false;
                }
                break;

            case TRIP_LIMITED:
                if (card.getRemainingTrips() <= 0) {
                    recordDenied(category);
                    System.out.println("[ТУРНІКЕТ] ВІДМОВА. Картка " + category.getTitle() + ": закінчилися поїздки.");
                    return false;
                }
                card.deductTrip();
                break;

            case ACCUMULATIVE:
                if (card.getBalance() < farePrice) {
                    recordDenied(category);
                    System.out.printf(" [ТУРНІКЕТ] ВІДМОВА. Недостатньо коштів на балансі (потрібно %.2f грн, є %.2f грн).%n",
                            farePrice, card.getBalance());
                    return false;
                }
                card.deductBalance(farePrice);
                break;
        }

        // Дозвіл проходу
        recordAllowed(category);
        System.out.println(" [ТУРНІКЕТ] ПРОХІД ДОЗВОЛЕНО. (" + category.getTitle() + " - " + card.getCardType().getTitle() + ")");
        return true;
    }

    private void recordAllowed(Category category) {
        totalPassesAllowed++;
        categoryAllowedStats.put(category, categoryAllowedStats.get(category) + 1);
    }

    private void recordDenied(Category category) {
        totalPassesDenied++;
        categoryDeniedStats.put(category, categoryDeniedStats.get(category) + 1);
    }

    public void printTotalSummary() {
        System.out.println("\n=== ЗАГАЛЬНА СТАТИСТИКА ТУРНІКЕТУ ===");
        System.out.println("Успішних проходів: " + totalPassesAllowed);
        System.out.println("Відмов у проході:  " + totalPassesDenied);
        System.out.println("Усього спроб:      " + (totalPassesAllowed + totalPassesDenied));
    }

    public void printDetailedStats() {
        System.out.println("\n=== ДЕТАЛІЗОВАНА СТАТИСТИКА ПО ТИПАХ КАРТОК ===");
        for (Category category : Category.values()) {
            int allowed = categoryAllowedStats.get(category);
            int denied = categoryDeniedStats.get(category);
            System.out.printf("Категорія: %-12s | Дозволено: %-3d | Відмовлено: %-3d%n",
                    category.getTitle() + ":", allowed, denied);
        }
    }
}

// мейн з консолькой

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final CardRegistrySystem registry = new CardRegistrySystem();
    private static final TurnstileProcessor turnstile = new TurnstileProcessor(registry, 30.00); // Вартість поїздки 30 грн (жесть)

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        System.out.println("==================================================");
        System.out.println("      МОДЕЛЬ ТУРНІКЕТУ ШВИДКІСНОГО ТРАМВАЮ        ");
        System.out.println("==================================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> issueCardMenu();
                case "2" -> passTurnstileMenu();
                case "3" -> topUpCardMenu();
                case "4" -> listAllCards();
                case "5" -> turnstile.printTotalSummary();
                case "6" -> turnstile.printDetailedStats();
                case "0" -> {
                    running = false;
                    System.out.println("\nПрограму завершено.");
                }
                default -> System.out.println("Некоректний вибір. Спробуйте ще раз.");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n----------------- ГОЛОВНЕ МЕНЮ -----------------");
        System.out.println("1. Випустити нову проїзну картку");
        System.out.println("2. Прикласти картку до турнікету");
        System.out.println("3. Поповнити накопичувальну картку");
        System.out.println("4. Переглянути всі випущені картки");
        System.out.println("5. Загальна статистика турнікету");
        System.out.println("6. Статистика турнікету за типами квитків");
        System.out.println("0. Вихід");
        System.out.print("Оберіть дію: ");
    }

    // меню видачі нових карток
    private static void issueCardMenu() {
        System.out.println("\n--- ВИДАЧА НОВОЇ КАРТКИ ---");
        System.out.println("Оберіть тип картки за терміном/поїздками:");
        System.out.println("1. На місяць");
        System.out.println("2. На 10 днів");
        System.out.println("3. На кількість поїздок (5 або 10)");
        System.out.println("4. Накопичувальна");
        System.out.print("Ваш вибір: ");

        String typeChoice = scanner.nextLine().trim();

        if (typeChoice.equals("4")) {
            System.out.print("Введіть початковий баланс (грн): ");
            double balance = parseDoubleInput();
            try {
                TravelCard card = registry.issueAccumulativeCard(Category.STANDARD, balance);
                System.out.println(" Картку успішно випущено: " + card);
            } catch (Exception e) {
                System.out.println(" Помилка: " + e.getMessage());
            }
            return;
        }

        System.out.println("\nОберіть категорію пасажира:");
        System.out.println("1. Учнівська");
        System.out.println("2. Студентська");
        System.out.println("3. Звичайна");
        System.out.print("Ваш вибір: ");

        Category category = switch (scanner.nextLine().trim()) {
            case "1" -> Category.PUPIL;
            case "2" -> Category.STUDENT;
            default -> Category.STANDARD;
        };

        switch (typeChoice) {
            case "1" -> {
                TravelCard card = registry.issueTimeCard(category, CardType.MONTHLY_SUBSCRIPTION);
                System.out.println(" Випущено: " + card);
            }
            case "2" -> {
                TravelCard card = registry.issueTimeCard(category, CardType.TEN_DAYS_SUBSCRIPTION);
                System.out.println("Випущено: " + card);
            }
            case "3" -> {
                System.out.print("Введіть кількість поїздок (наприклад, 5 або 10): ");
                int trips = parseIntInput();
                TravelCard card = registry.issueTripCard(category, trips);
                System.out.println("Випущено: " + card);
            }
            default -> System.out.println("Невідомий тип картки.");
        }
    }

    // прикладання
    private static void passTurnstileMenu() {
        System.out.println("\n--- СМОДЕЛЮВАТИ ПРОХІД ЧЕРЕЗ ТУРНІКЕТ ---");
        System.out.println("1. Ввести ID існуючої картки з системи");
        System.out.println("2. Прикласти незареєстровану / підроблену картку");
        System.out.print("Ваш вибір: ");

        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            if (registry.getAllCards().isEmpty()) {
                System.out.println("У системі ще немає випущених карток. Спочатку випустіть картку");
                return;
            }
            System.out.print("Введіть ID картки (CARD-XXXXX): ");
            String id = scanner.nextLine().trim().toUpperCase();

            TravelCard card = registry.getCard(id);
            turnstile.processCard(card);
        } else if (choice.equals("2")) {

            TravelCard fakeCard = TravelCard.createTripLimited("FAKE-9999", Category.STANDARD, 5);
            turnstile.processCard(fakeCard);
        }
    }

    // поповнення
    private static void topUpCardMenu() {
        System.out.print("\nВведіть ID накопичувальної картки: ");
        String id = scanner.nextLine().trim().toUpperCase();

        TravelCard card = registry.getCard(id);

        if (card == null) {
            System.out.println("Картку з таким ID не знайдено");
            return;
        }

        if (card.getCardType() != CardType.ACCUMULATIVE) {
            System.out.println("Поповнювати можна тільки накопичувальні картки");
            return;
        }

        System.out.print("Введіть суму поповнення (грн): ");
        double amount = parseDoubleInput();
        card.topUpBalance(amount);
        System.out.println("Баланс поповнено. Новий стан: " + card);
    }

    private static void listAllCards() {
        System.out.println("\n=== РЕЄСТР ВИПУЩЕНИХ КАРТОК ===");
        Collection<TravelCard> cards = registry.getAllCards();
        if (cards.isEmpty()) {
            System.out.println("Картки відсутні.");
        } else {
            cards.forEach(card -> System.out.println("- " + card));
        }
    }

    private static int parseIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseDoubleInput() {
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}