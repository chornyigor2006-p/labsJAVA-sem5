import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== ЗАВДАННЯ 1: Максимальна кількість слів у рядку ===");
        Task1_MaxWords.execute(scanner);

        System.out.println("\n=== ЗАВДАННЯ 3: Шифрування символьного потоку ===");
        Task3_Cipher.execute(scanner);

        System.out.println("\n=== ЗАВДАННЯ 4: Підрахунок HTML тегів ===");
        Task4_TagFrequency.execute(scanner);
        
        scanner.close();
    }
}

class Task1_MaxWords {
    public static void execute(Scanner scanner) {
        System.out.print("Введіть шлях до текстового файлу для читання (наприклад, input.txt): ");
        String filePath = scanner.nextLine();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String maxLine = "";
            int maxWords = 0;

            while ((line = reader.readLine()) != null) {
                // Розбиваємо рядок на слова за пробілами
                String[] words = line.trim().split("\\s+");
                int currentWords = (line.trim().isEmpty()) ? 0 : words.length;

                if (currentWords > maxWords) {
                    maxWords = currentWords;
                    maxLine = line;
                }
            }
            System.out.println("Рядок з максимальною кількістю слів (" + maxWords + "):");
            System.out.println(maxLine);
        } catch (IOException e) {
            System.out.println("Помилка роботи з файлом: " + e.getMessage());
        }
    }
}

class Task3_Cipher {
    public static void execute(Scanner scanner) {
        System.out.print("Введіть текст для шифрування: ");
        String text = scanner.nextLine();
        
        System.out.print("Введіть один символ як ключ шифрування: ");
        char key = scanner.nextLine().charAt(0);
        
        System.out.print("Введіть ім'я файлу для збереження зашифрованого тексту: ");
        String filePath = scanner.nextLine();

        encryptData(text, key, filePath);
        
        decryptData(key, filePath);
    }

    private static void encryptData(String text, char key, String filePath) {
        try (CipherWriter writer = new CipherWriter(new FileWriter(filePath), key)) {
            writer.write(text);
            System.out.println("Текст успішно зашифровано та збережено у файл " + filePath);
        } catch (IOException e) {
            System.out.println("Помилка шифрування: " + e.getMessage());
        }
    }

    private static void decryptData(char key, String filePath) {
        try (CipherReader reader = new CipherReader(new FileReader(filePath), key)) {
            StringBuilder decryptedText = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                decryptedText.append((char) c);
            }
            System.out.println("Дешифрований текст з файлу: " + decryptedText.toString());
        } catch (IOException e) {
            System.out.println("Помилка дешифрування: " + e.getMessage());
        }
    }
}

class CipherWriter extends FilterWriter {
    private final int keyShift;

    protected CipherWriter(Writer out, char key) {
        super(out);
        this.keyShift = key; 
    }

    @Override
    public void write(int c) throws IOException {
        super.write(c + keyShift);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for (int i = off; i < off + len; i++) {
            write(cbuf[i]);
        }
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        for (int i = off; i < off + len; i++) {
            write(str.charAt(i));
        }
    }
}

class CipherReader extends FilterReader {
    private final int keyShift;

    protected CipherReader(Reader in, char key) {
        super(in);
        this.keyShift = key;
    }

    @Override
    public int read() throws IOException {
        int c = super.read();
        return (c == -1) ? -1 : (c - keyShift);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int numRead = super.read(cbuf, off, len);
        if (numRead == -1) return -1;
        for (int i = off; i < off + numRead; i++) {
            cbuf[i] = (char) (cbuf[i] - keyShift);
        }
        return numRead;
    }
}

class Task4_TagFrequency {
    public static void execute(Scanner scanner) {
        System.out.print("Введіть URL сторінки (наприклад, https://example.com): ");
        String urlString = scanner.nextLine();

        Map<String, Integer> tagCounts = new HashMap<>();

        try {
            URL url = new URL(urlString);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
           
            Pattern pattern = Pattern.compile("<([a-zA-Z0-9]+)[^>]*>");

            while ((inputLine = in.readLine()) != null) {
                Matcher matcher = pattern.matcher(inputLine);
                while (matcher.find()) {
                    String tag = matcher.group(1).toLowerCase();
                    tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
                }
            }
            in.close();

            Map<String, Integer> lexSorted = new TreeMap<>(tagCounts);
            System.out.println("\n--- Результати (лексикографічний порядок) ---");
            lexSorted.forEach((k, v) -> System.out.println(k + " : " + v));

            List<Map.Entry<String, Integer>> freqSorted = new ArrayList<>(tagCounts.entrySet());
            freqSorted.sort(Map.Entry.comparingByValue());

            System.out.println("\n--- Результати (зростання за частотою) ---");
            freqSorted.forEach(e -> System.out.println(e.getKey() + " : " + e.getValue()));
   
            System.out.print("\nВведіть шлях до файлу для збереження результатів (серіалізація Map): ");
            String savePath = scanner.nextLine();
            FileSerializationUtil.saveObjectToFile(savePath, new HashMap<>(tagCounts));

        } catch (Exception e) {
            System.out.println("Помилка завантаження/аналізу сторінки: " + e.getMessage());
        }
    }
}

class FileSerializationUtil {
    public static void saveObjectToFile(String filePath, Object data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(data);
            System.out.println("Дані успішно збережені у файл " + filePath + " за допомогою серіалізації.");
        } catch (IOException e) {
            System.out.println("Помилка збереження у файл: " + e.getMessage());
        }
    }

    public static Object readObjectFromFile(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Помилка читання з файлу: " + e.getMessage());
            return null;
        }
    }
}