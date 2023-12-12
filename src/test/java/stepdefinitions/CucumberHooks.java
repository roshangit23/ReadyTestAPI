package stepdefinitions;

import io.cucumber.java.Before;
import io.restassured.response.Response;
import utils.ApiTestHelper;

public class CucumberHooks {
    private static ApiTestHelper apiHelper;
    public static String token;
    @Before
    public void setup() {
        apiHelper = new ApiTestHelper();
    }
    public static ApiTestHelper getApiHelper() {
        return apiHelper;
    }
    @Before(value = "@Login")
    public void loginAndSetToken() {
        // Set the base URI, body, and send a POST request as per your login scenario
        apiHelper.setBaseURI("https://reqres.in/api");
        String body = "{\"email\": \"eve.holt@reqres.in\", \"password\": \"cityslicka\"}";
        apiHelper.setBody(body);
        Response response = apiHelper.sendRequest("POST", "/login");

        // Extract the token from the response
        token = response.jsonPath().get("token");

        // Set the bearer token to be used in subsequent requests
        apiHelper.setBearerToken(token);
    }
}
