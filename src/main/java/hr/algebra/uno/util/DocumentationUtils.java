package hr.algebra.uno.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DocumentationUtils {
    private static final Logger log = LoggerFactory.getLogger(DocumentationUtils.class);
    private static final String PATH_WITH_CLASSES = "target/classes/";
    private static final String CLASS_FILE_NAME_EXTENSION = ".class";
    private static final String HTML_DOCUMENTATION_FILE_NAME = "doc/documentation.html";

    private DocumentationUtils() {}

    public static void generateDocumentationHtmlFile() throws IOException {
        Path start = Paths.get(PATH_WITH_CLASSES);
        try (Stream<Path> stream = Files.walk(start, Integer.MAX_VALUE)) {
            List<String> classList = stream
                    .filter(f -> f.getFileName().toString().endsWith(CLASS_FILE_NAME_EXTENSION)
                            && Character.isUpperCase(f.getFileName().toString().charAt(0)))
                    .map(String::valueOf)
                    .sorted()
                    .toList();

            String htmlString = generateDocumentationCode(classList);
            Path path = Path.of(HTML_DOCUMENTATION_FILE_NAME);
            Files.createDirectories(path.getParent());
            Files.writeString(path, htmlString);
        }
    }

    private static String generateDocumentationCode(List<String> classList) {
        StringBuilder html = new StringBuilder();

        html.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <title>UNO Game Docs</title>
            <style>
                body { font-family: Arial, sans-serif; margin: 20px; background: #fafafa; }
                h1 { color: #900; border-bottom: 2px solid #ccc; padding-bottom: 4px; }
                h2 { color: #333; margin-top: 25px; }
                .class-block { margin-bottom: 40px; padding: 10px 15px; background: #fff; }
                code { background: #eee; padding: 2px 4px; }
                summary { cursor: pointer; font-weight: bold; margin: 5px 0; }
                .sig { margin: 3px 0 3px 15px; font-family: Consolas, monospace; }
            </style>
        </head>
        <body>
        <h1>UNO Game - Reflection Docs</h1>
        <p>Auto-generated using Java Reflection API</p>
    """);

        for (String fullPath : classList) {
            String className = fullPath
                    .substring(PATH_WITH_CLASSES.length(), fullPath.length() - CLASS_FILE_NAME_EXTENSION.length())
                    .replace("\\", ".")
                    .replace("/", ".");

            try {
                Class<?> clazz = Class.forName(className);
                String simpleName = clazz.getSimpleName();
                String packageName = clazz.getPackageName();

                html.append("<div class='class-block'>");
                html.append("<h1 id='").append(simpleName).append("'>").append(simpleName).append("</h1>");

                // Package
                html.append("<p><strong>Package:</strong> ").append(packageName).append("</p>");

                // Superclass
                Class<?> superClass = clazz.getSuperclass();
                if (superClass != null && superClass != Object.class) {
                    html.append("<p><strong>Extends:</strong> ").append(superClass.getSimpleName()).append("</p>");
                }

                // Interfaces
                Class<?>[] interfaces = clazz.getInterfaces();
                if (interfaces.length > 0) {
                    html.append("<p><strong>Implements:</strong> ");
                    html.append(
                            Arrays.stream(interfaces)
                                    .map(Class::getSimpleName)
                                    .collect(Collectors.joining(", "))
                    );
                    html.append("</p>");
                }

                // Constructors
                Constructor<?>[] ctors = clazz.getDeclaredConstructors();
                html.append("<details><summary>Constructors</summary>");
                if (ctors.length == 0) {
                    html.append("<p class='sig'>No public constructors</p>");
                } else {
                    for (Constructor<?> ctor : ctors) {
                        String mods = Modifier.toString(ctor.getModifiers());
                        String params = Arrays.stream(ctor.getParameterTypes())
                                .map(Class::getSimpleName)
                                .collect(Collectors.joining(", "));
                        html.append("<p class='sig'>")
                                .append(mods).append(" ")
                                .append(simpleName)
                                .append("(").append(params).append(")")
                                .append("</p>");
                    }
                }
                html.append("</details>");

                // Fields
                Field[] fields = clazz.getDeclaredFields();
                html.append("<details><summary>Fields</summary>");
                if (fields.length == 0) {
                    html.append("<p class='sig'>No declared fields</p>");
                } else {
                    for (Field field : fields) {
                        String mods = Modifier.toString(field.getModifiers());
                        String type = field.getType().getSimpleName();
                        String name = field.getName();
                        html.append("<p class='sig'>")
                                .append(mods).append(" ")
                                .append(type).append(" ")
                                .append(name)
                                .append("</p>");
                    }
                }
                html.append("</details>");

                // Methods
                Method[] methods = clazz.getDeclaredMethods();
                html.append("<details><summary>Methods</summary>");
                if (methods.length == 0) {
                    html.append("<p class='sig'>No declared methods</p>");
                } else {
                    for (Method method : methods) {
                        String mods = Modifier.toString(method.getModifiers());
                        String returnType = method.getReturnType().getSimpleName();
                        String name = method.getName();
                        String params = Arrays.stream(method.getParameterTypes())
                                .map(Class::getSimpleName)
                                .collect(Collectors.joining(", "));
                        html.append("<p class='sig'>")
                                .append(mods).append(" ")
                                .append(returnType).append(" ")
                                .append(name)
                                .append("(").append(params).append(")")
                                .append("</p>");
                    }
                }
                html.append("</details>");

                html.append("</div>");

            } catch (ClassNotFoundException e) {
                log.error("Error while generating docs", e);
            }
        }

        html.append("""
        </body>
        </html>
    """);

        return html.toString();
    }
}
