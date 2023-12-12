package common;

import org.yaml.snakeyaml.Yaml;
import runners.TestRunner;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Common {

    public static void checkYamlFileForUniqueness(String filePath) {
        try (InputStream inputStream = TestRunner.class.getClassLoader().getResourceAsStream(filePath)) {
            Yaml yaml = new Yaml();
            Map<String, Map<String, String>> data = yaml.load(inputStream);

            Set<String> uniqueElements = new HashSet<>();
            for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
                for (String key : entry.getValue().keySet()) {
                    if (!uniqueElements.add(key)) {
                        throw new RuntimeException("Duplicate element name found: " + key);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error checking uniqueness in YAML file: " + e.getMessage(), e);
        }
    }

    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isFloat(String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }
}
