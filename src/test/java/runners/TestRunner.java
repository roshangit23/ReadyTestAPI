package runners;

import common.Common;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;


@CucumberOptions(
        features = "src/test/resources/features",
        glue = "stepdefinitions",
        plugin = "json:target/jsonReports/cucumber-report.json"
)
public class TestRunner extends AbstractTestNGCucumberTests {
    static {
        // Call method to check uniqueness in the YAML file
        Common.checkYamlFileForUniqueness("apiPaths/apiPaths.yaml");
        Common.checkYamlFileForUniqueness("databaseQueries/databaseQueries.yaml");
    }
}
