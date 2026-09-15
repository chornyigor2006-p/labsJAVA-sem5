package org.example;

import java.io.File;

public class HotReloadApp {
    public static void main(String[] args) throws Exception {
        // a way to a file
        File workingDir = new File("src/main/java/org/example");
        File sourceFile = new File(workingDir, "TestModule.java");

        if (!sourceFile.exists()) {
            System.err.println("File not found: " + sourceFile.getAbsolutePath());
            return;
        }

        long lastModified = 0;

        System.out.println("Application started. Modify TestModule.java and save changes...\n");

        while (true) {
            long currentModified = sourceFile.lastModified();

            if (currentModified != lastModified) {
                // pause so it wont explode
                Thread.sleep(100);

                System.out.println("-> Changes detected in TestModule.java. Recompiling...");
                boolean success = CompilerUtils.compile(sourceFile);

                if (success) {
                    lastModified = currentModified;

                    // making new class loader
                    DynamicClassLoader loader = new DynamicClassLoader(workingDir);

                    // class with a correct name
                    Class<?> testModuleClass = loader.loadClass("TestModule");
                    Object t = testModuleClass.getDeclaredConstructor().newInstance();

                    // results
                    System.out.println("[JVM Output]: " + t);
                } else {
                    System.err.println("Compilation failed.");
                }
            }

            // Інтервал перевірки — 1 секунда
            Thread.sleep(1000);
        }
    }
}