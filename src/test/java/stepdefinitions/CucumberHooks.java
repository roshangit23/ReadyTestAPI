package stepdefinitions;

import common.Common;
import io.cucumber.java.Before;
import io.restassured.response.Response;
import utils.ApiTestHelper;
import utils.DatabaseHelper;

public class CucumberHooks {
    private static ApiTestHelper apiHelper;
    private static DatabaseHelper databaseHelper;
    public static String token;
    private Common common = new Common();
    @Before
    public void setupApiHelper() {
        apiHelper = new ApiTestHelper();
        // Fetching base URL from YAML
        String baseUri = common.getApiPathFromYaml("testUrl");
        apiHelper.setBaseURI(baseUri);
    }
    @Before(value = "@DatabaseSetup")
    public void setupDatabaseHelper() {
        // Fetching database configuration from YAML
        String url = common.getQueryFromYaml("databaseUrl");
        String username = common.getQueryFromYaml("databaseUsername");
        String password = common.getQueryFromYaml("databasePassword");
        databaseHelper = new DatabaseHelper(url, username, password);
    }
    public static DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }
    public static ApiTestHelper getApiHelper() {
        return apiHelper;
    }
    @Before(value = "@Login")
    public void loginAndSetToken() {
        // Fetching login path and body from YAML
        String loginPath = common.getApiPathFromYaml("loginPath");
        String email = common.getApiPathFromYaml("email");
        String password = common.getApiPathFromYaml("password");

        String body = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}";
        apiHelper.setBody(body);

        Response response = apiHelper.sendRequest("POST", loginPath);

        // Extract the token from the response
        token = response.jsonPath().get("token");

        // Set the bearer token to be used in subsequent requests
        apiHelper.setBearerToken(token);
    }
}
