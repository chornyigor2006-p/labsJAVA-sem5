package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DynamicClassLoader extends ClassLoader {
    private final File classDir;

    public DynamicClassLoader(File classDir) {
        super(ClassLoader.getSystemClassLoader().getParent());
        this.classDir = classDir;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if ("TestModule".equals(name)) {
            return findClass(name);
        }
        return super.loadClass(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        File classFile = new File(classDir, name.replace('.', File.separatorChar) + ".class");
        if (!classFile.exists()) {
            throw new ClassNotFoundException("Class not found: " + name);
        }

        try {
            byte[] bytes = Files.readAllBytes(classFile.toPath());
            return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException("Failed to read class bytecode: " + name, e);
        }
    }
}