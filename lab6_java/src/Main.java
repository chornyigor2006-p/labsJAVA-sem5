import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class EnglishToUkrainianTranslator {
    private final Map<String, String> dictionary;

    public EnglishToUkrainianTranslator() {
        this.dictionary = new HashMap<>();
    }
    public void addWord(String englishWord, String ukrainianWord) {
        if (englishWord != null && ukrainianWord != null && !englishWord.isBlank() && !ukrainianWord.isBlank()) {
            
            dictionary.put(englishWord.trim().toLowerCase(), ukrainianWord.trim().toLowerCase());
        }
    }

    public String translatePhrase(String phrase) {
        if (phrase == null || phrase.isBlank()) {
            return "";
        }
        Pattern wordPattern = Pattern.compile("[a-zA-Z']+");
        Matcher matcher = wordPattern.matcher(phrase);
        StringBuilder translatedResult = new StringBuilder();

        while (matcher.find()) {
            String originalWord = matcher.group();
            String lowerCaseWord = originalWord.toLowerCase();

            String translation = dictionary.getOrDefault(lowerCaseWord, "[" + originalWord + "]");

            
            if (Character.isUpperCase(originalWord.charAt(0)) && !translation.startsWith("[")) {
                translation = Character.toUpperCase(translation.charAt(0)) + translation.substring(1);
            }

            matcher.appendReplacement(translatedResult, Matcher.quoteReplacement(translation));
        }
        matcher.appendTail(translatedResult);

        return translatedResult.toString();
    }

    public int getDictionarySize() {
        return dictionary.size();
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, System.console() != null ? System.console().charset() : java.nio.charset.Charset.defaultCharset());
        EnglishToUkrainianTranslator translator = new EnglishToUkrainianTranslator();

        translator.addWord("hello", "привіт");
        translator.addWord("world", "світ");
        translator.addWord("i", "я");
        translator.addWord("love", "люблю");
        translator.addWord("programming", "програмування");
        translator.addWord("java", "джава");
        translator.addWord("is", "є");
        translator.addWord("a", "");
        translator.addWord("great", "чудова");
        translator.addWord("language", "мова");
        translator.addWord("cat", "кіт");
        translator.addWord("dog", "собака");
        translator.addWord("and", "і");

        System.out.println("=== Консольний перекладач (EN -> UA) ===");
        System.out.println("У базовому словнику вже є " + translator.getDictionarySize() + " слів.");

        System.out.println("\nБажаєте додати нові слова до словника? (так / ні):");
        String choice = scanner.nextLine().trim().toLowerCase();

        if (choice.equals("так") || choice.equals("yes") || choice.equals("y") || choice.equals("+")) {
            System.out.println("Вводьте пари слів. Для завершення введення натисніть Enter на порожньому рядку.");
            while (true) {
                System.out.print("Англійське слово: ");
                String enWord = scanner.nextLine().trim();
                if (enWord.isEmpty()) {
                    break;
                }

                System.out.print("Український переклад: ");
                String uaWord = scanner.nextLine().trim();
                if (uaWord.isEmpty()) {
                    System.out.println("Переклад не може бути порожнім. Спробуйте ще раз.");
                    continue;
                }

                translator.addWord(enWord, uaWord);
                System.out.println("Додано: " + enWord + " -> " + uaWord);
            }
        }

        System.out.println("\nВведіть фразу англійською мовою для перекладу:");
        String inputPhrase = scanner.nextLine();

        String translatedPhrase = translator.translatePhrase(inputPhrase);

        System.out.println("\nОригінал: " + inputPhrase);
        
        System.out.println("Переклад: " + translatedPhrase.replaceAll("\\s+", " ").trim());

        scanner.close();
    }
}