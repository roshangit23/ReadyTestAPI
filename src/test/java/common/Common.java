package common;

import org.yaml.snakeyaml.Yaml;
import runners.TestRunner;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
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
    public String getApiPathFromYaml(String pathName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("apiPaths/apiPaths.yaml")) {
            Yaml yaml = new Yaml();
            Map<String, Map<String, String>> data = yaml.load(inputStream);

            for (Map.Entry<String, Map<String, String>> page : data.entrySet()) {
                Map<String, String> elements = page.getValue();
                if (elements.containsKey(pathName)) {
                    return elements.get(pathName);
                }
            }
            throw new IllegalArgumentException("path '" + pathName + "' not found in the YAML file.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load or parse the apiPaths.yaml file.", e);
        }
    }

    public String getQueryFromYaml(String queryName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("databaseQueries/databaseQueries.yaml")) {
            Yaml yaml = new Yaml();
            Map<String, Map<String, String>> data = yaml.load(inputStream);

            for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
                Map<String, String> queries = entry.getValue();
                if (queries.containsKey(queryName)) {
                    return queries.get(queryName);
                }
            }

            throw new IllegalArgumentException("Query '" + queryName + "' not found in the YAML file.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load or parse the databaseQueries.yaml file.", e);
        }
    }
    public List<String> getBatchQueriesFromYaml(String batchQueryName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("databaseQueries/databaseQueries.yaml")) {
            Yaml yaml = new Yaml();
            Map<String, Map<String, List<String>>> data = yaml.load(inputStream);

            for (Map.Entry<String, Map<String, List<String>>> entry : data.entrySet()) {
                Map<String, List<String>> batchQueries = entry.getValue();
                if (batchQueries.containsKey(batchQueryName)) {
                    return batchQueries.get(batchQueryName);
                }
            }

            throw new IllegalArgumentException("Batch query '" + batchQueryName + "' not found in the YAML file.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load or parse the databaseQueries.yaml file.", e);
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
