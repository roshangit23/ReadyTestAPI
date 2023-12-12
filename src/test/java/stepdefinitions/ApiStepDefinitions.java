package stepdefinitions;

import com.google.gson.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.yaml.snakeyaml.Yaml;
import utils.ApiTestHelper;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static common.Common.*;
import static utils.ApiTestHelper.*;

public class ApiStepDefinitions {

    private ApiTestHelper apiHelper = CucumberHooks.getApiHelper();
    private Response response;
    private String extractedValue;
    public ApiStepDefinitions() {

    }
    private String getApiPathFromYaml(String pathName) {
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
    @Given("I set the base URI to {string}")
    public void iSetTheBaseURITo(String baseUri) {
        String url = getApiPathFromYaml(baseUri);
        apiHelper.setBaseURI(url);
    }

    @Given("I set request headers:")
    public void iSetRequestHeaders(DataTable headersTable) {
        Map<String, String> headers = headersTable.asMap(String.class, String.class);
        apiHelper.setHeaders(headers);
    }

    @Given("I set basic authentication with username {string} and password {string}")
    public void iSetBasicAuthentication(String username, String password) {
        apiHelper.setBasicAuth(username, password);
    }

    @Given("I set bearer token from extracted value")
    public void iSetBearerTokenFromExtractedValue() {
        apiHelper.setBearerToken(extractedValue);
    }

    @Given("I set API key with header {string} and value {string}")
    public void iSetApiKey(String headerName, String apiKey) {
        apiHelper.setApiKey(headerName, apiKey);
    }

    @Given("I set digest authentication with username {string} and password {string}")
    public void iSetDigestAuth(String username, String password) {
        apiHelper.setDigestAuth(username, password);
    }

    @Given("I set OAuth1 authentication with consumer key {string}, consumer secret {string}, access token {string}, and secret token {string}")
    public void iSetOAuth1(String consumerKey, String consumerSecret, String accessToken, String secretToken) {
        apiHelper.setOAuth1(consumerKey, consumerSecret, accessToken, secretToken);
    }
    @Given("I set request body:")
    public void iSetRequestBody(String body) {
        apiHelper.setBody(body);
    }

    @Given("I set request body with data:")
    public void iSetRequestBodyWithData(DataTable bodyData) {
        Map<String, String> dataMap = bodyData.asMap(String.class, String.class);
        String jsonBody = new Gson().toJson(dataMap); // Using Gson to convert map to JSON string
        System.out.println("Simple request data "+jsonBody);
         apiHelper.setBody(jsonBody);
    }

    @Given("I set complex request body with data:")
    public void iSetComplexRequestBodyWithData(DataTable bodyData) {
        Map<String, String> dataMap = bodyData.asMap(String.class, String.class);
        JsonObject requestBody = new JsonObject();

        dataMap.forEach((key, value) -> {
            if (isJson(value)) {
                JsonElement element = JsonParser.parseString(value);
                requestBody.add(key, element);
            } else if (isInteger(value)) {
                requestBody.addProperty(key, Integer.parseInt(value));
            } else if (isFloat(value)) {
                requestBody.addProperty(key, Float.parseFloat(value));
            } else if (isBoolean(value)) {
                requestBody.addProperty(key, Boolean.parseBoolean(value));
            } else {
                requestBody.addProperty(key, value);
            }
        });
        System.out.println(requestBody.toString());
        apiHelper.setBody(requestBody.toString());
    }

    @Given("I set query parameters:")
    public void iSetQueryParameters(DataTable queryParamsTable) {
        Map<String, String> queryParams = queryParamsTable.asMap(String.class, String.class);
        apiHelper.setQueryParams(queryParams);
    }

    @Given("I set path parameters:")
    public void iSetPathParameters(DataTable pathParamsTable) {
        Map<String, String> pathParams = pathParamsTable.asMap(String.class, String.class);
        apiHelper.setPathParams(pathParams);
    }

    @Given("I add multipart file {string}")
    public void iAddMultipartFile(String filePath) {
        apiHelper.addMultiPart(new File(filePath));
    }

    @Given("I set form parameters:")
    public void iSetFormParameters(DataTable formParamsTable) {
        Map<String, String> formParams = formParamsTable.asMap(String.class, String.class);
        apiHelper.setFormParams(formParams);
    }

    @Given("I add cookie {string} with value {string}")
    public void iAddCookie(String name, String value) {
        apiHelper.addCookie(name, value);
    }

    @When("I send a {string} request to {string}")
    public void iSendARequestTo(String method, String endpointPath) {
        String endpoint = getApiPathFromYaml(endpointPath);
        // Handling path parameters
        if (!apiHelper.getPathParams().isEmpty()) {
            for (Map.Entry<String, String> entry : apiHelper.getPathParams().entrySet()) {
                endpoint = endpoint.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        // Handling query parameters
        if (!apiHelper.getQueryParams().isEmpty()) {
            StringJoiner queryParamString = new StringJoiner("&", "?", "");
            for (Map.Entry<String, String> entry : apiHelper.getQueryParams().entrySet()) {
                queryParamString.add(entry.getKey() + "=" + entry.getValue());
            }
            endpoint += queryParamString.toString();
        }

        response = apiHelper.sendRequest(method, endpoint);
    }

    @Then("I expect the response status code to be {int}")
    public void iExpectTheResponseStatusCodeToBe(int statusCode) {
        Assert.assertTrue(ApiTestHelper.verifyStatusCode(response, statusCode),"Status code is not as expected");
    }

    @Then("I expect the response to contain field {string} with value {string}")
    public void iExpectTheResponseToContainFieldWithValue(String jsonPath, String expectedValue) {
        Assert.assertTrue(ApiTestHelper.verifyResponseField(response, jsonPath, expectedValue),"Response field value is not as expected");
    }

    @Then("I expect the response to contain {string}")
    public void iExpectTheResponseToContain(String content) {
        Assert.assertTrue( ApiTestHelper.verifyResponseContains(response, content),"Response does not contain expected content");
    }

    @Then("I expect the response time to be less than {long} milliseconds")
    public void iExpectTheResponseTimeToBeLessThan(long maxTimeMillis) {
        Assert.assertTrue(ApiTestHelper.verifyResponseTime(response, maxTimeMillis),"Response time is greater than expected");
    }

    @Then("I expect the response header {string} to be {string}")
    public void iExpectTheResponseHeaderToBe(String header, String expectedValue) {
        Assert.assertTrue(ApiTestHelper.verifyResponseHeader(response, header, expectedValue),"Response header value is not as expected");
    }

    @Given("I extract value from response using JSON path {string}")
    public void iExtractValueFromResponseUsingJsonPath(String jsonPath) {
        extractedValue  = ApiTestHelper.extractValueFromResponse(response, jsonPath);
    }

    @Then("I validate the response against JSON schema {string}")
    public void iValidateTheResponseAgainstJsonSchema(String schemaPath) {
        ApiTestHelper.validateJsonSchema(response, schemaPath);
    }

    @Then("I extract XML from the response and store it")
    public void iExtractXmlFromTheResponse() {
        XmlPath xmlPath = ApiTestHelper.getXmlPath(response);
    }

    @Given("I extract values list from response using JSON path {string}")
    public void iExtractValuesListFromResponseUsingJsonPath(String jsonPath) {
        List<?> values = ApiTestHelper.extractValuesFromResponse(response, jsonPath);
    }
}
