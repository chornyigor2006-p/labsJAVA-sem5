package org.example;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;

public class CompilerUtils {
    public static boolean compile(File javaFile) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.err.println("Error: JDK Compiler is not available. Make sure to run under JDK rather than JRE.");
            return false;
        }

        int result = compiler.run(null, null, null, javaFile.getAbsolutePath());
        return result == 0;
    }
}