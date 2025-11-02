package hr.algebra.uno.util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class DocumentationUtils {
    private static final String PATH_WITH_CLASSES = "target/classes/";
    private static final String CLASS_FILE_NAME_EXTENSION = ".class";
    private static final String HTML_DOCUMENTATION_FILE_NAME = "doc/documentation.html";

    public static void generateDocumentationHtmlFile() throws IOException {
        Path start = Paths.get(PATH_WITH_CLASSES);
        try(Stream<Path> stream = Files.walk(start, Integer.MAX_VALUE)) {
            List<String> classList = stream
                    .filter(f -> f.getFileName().toString().endsWith(CLASS_FILE_NAME_EXTENSION)
                            && Character.isUpperCase(f.getFileName().toString().charAt(0)))
                    .map(String::valueOf)
                    .sorted()
                    .toList();

            String htmlString = generateDocumentationCode(classList);
            // Files.writeString(Path.of(HTML_DOCUMENTATION_FILE_NAME), htmlString);
            Path path = Path.of(HTML_DOCUMENTATION_FILE_NAME);
            Files.createDirectories(path.getParent());
            Files.writeString(path, htmlString);
        }
    }

    private static String generateDocumentationCode(List<String> classList) {
        StringBuilder htmlBuilder = new StringBuilder();

        String htmlStart = """
                <!DOCTYPE html>
                <html>
                <head>
                <title>Java documentation</title>
                </head>
                <body>
                <h1>List of classes:</h1>""";

        htmlBuilder.append(htmlStart);

        for (String className : classList) {
            className = className
                    .substring(PATH_WITH_CLASSES.length(), className.length() - CLASS_FILE_NAME_EXTENSION.length())
                    .replace("\\", ".")
                    .replace("/", ".");

            try {
                Class<?> clazz = Class.forName(className);
                htmlBuilder
                        .append("<h1>Class: ")
                        .append(className)
                        .append("</h1><br/>");

                Constructor<?>[] constructors = clazz.getConstructors();
                if(constructors.length > 0) {
                    htmlBuilder.append("<h2>List of constructors: </h2><br/>");
                    for (Constructor<?> constructor : constructors) {
                        htmlBuilder.append("<h3>Constructor: ").append(constructor).append("</h3><br />");
                    }
                }
                else {
                    htmlBuilder.append("<h2>No constructor</h2><br />");
                }

                Field[] declaredFields = clazz.getDeclaredFields();
                if(declaredFields.length > 0) {
                    htmlBuilder.append("<h2>List of declared fields: </h2><br/>");
                    for (Field field : declaredFields) {
                        htmlBuilder.append("<h3>Field: ").append(field).append("</h3><br />");
                    }
                }
                else {
                    htmlBuilder.append("<h2>No fields</h2><br />");
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        String htmlEnd = """
                </body>
                </html>""";
        htmlBuilder.append(htmlEnd);

        return htmlBuilder.toString();
    }
}
