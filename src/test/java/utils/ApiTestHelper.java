package utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class ApiTestHelper {

    private RequestSpecification request;
    private Map<String, String> currentPathParams = new HashMap<>();
    private Map<String, String> currentQueryParams = new HashMap<>();
    private static final Logger logger = LogManager.getLogger(ApiTestHelper.class);

    public ApiTestHelper() {
        this.request = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
    }

    public void setBaseURI(String baseUri) {
        try {
            this.request.baseUri(baseUri);
        } catch (Exception e) {
            logger.error("Error setting base URI: " + baseUri, e);
            Assert.fail("Error setting base URI: " + baseUri);
        }
    }

    public void setHeaders(Map<String, String> headers) {
        try {
            this.request.headers(headers);
        } catch (Exception e) {
            logger.error("Error setting headers", e);
            Assert.fail("Error setting headers");
        }
    }

    public void setBasicAuth(String username, String password) {
        try {
            this.request.auth().preemptive().basic(username, password);
        } catch (Exception e) {
            logger.error("Error setting basic auth", e);
            Assert.fail("Error setting basic auth");
        }
    }

    public void setBearerToken(String token) {
        try {
            this.request.auth().preemptive().oauth2(token);
        } catch (Exception e) {
            logger.error("Error setting bearer token", e);
            Assert.fail("Error setting bearer token");
        }
    }

    public void setApiKey(String headerName, String apiKey) {
        try {
            this.request.header(headerName, apiKey);
        } catch (Exception e) {
            logger.error("Error setting API key", e);
            Assert.fail("Error setting API key");
        }
    }

    public void setDigestAuth(String username, String password) {
        try {
            this.request.auth().digest(username, password);
        } catch (Exception e) {
            logger.error("Error setting digest auth", e);
            Assert.fail("Error setting digest auth");
        }
    }

    public void setOAuth1(String consumerKey, String consumerSecret, String accessToken, String secretToken) {
        try {
            this.request.auth().oauth(consumerKey, consumerSecret, accessToken, secretToken);
        } catch (Exception e) {
            logger.error("Error setting OAuth1", e);
            Assert.fail("Error setting OAuth1");
        }
    }

    public void setBody(Object body) {
        try {
            this.request.body(body);
        } catch (Exception e) {
            logger.error("Error setting request body", e);
            Assert.fail("Error setting request body");
        }
    }

    public void setQueryParams(Map<String, String> queryParams) {
        try {
            this.request.queryParams(queryParams);
            this.currentQueryParams = new HashMap<>(queryParams);
        } catch (Exception e) {
            logger.error("Error setting query params", e);
            Assert.fail("Error setting query params");
        }
    }

    public void setPathParams(Map<String, String> pathParams) {
        try {
            this.request.pathParams(pathParams);
            this.currentPathParams = new HashMap<>(pathParams);
        } catch (Exception e) {
            logger.error("Error setting path params", e);
            Assert.fail("Error setting path params");
        }
    }
    public Map<String, String> getPathParams() {
        return new HashMap<>(this.currentPathParams);
    }

    public Map<String, String> getQueryParams() {
        return new HashMap<>(this.currentQueryParams);
    }

    public void addMultiPart(File file) {
        try {
            this.request.multiPart(file);
        } catch (Exception e) {
            logger.error("Error adding multipart file", e);
            Assert.fail("Error adding multipart file");
        }
    }

    public void setFormParams(Map<String, String> formParams) {
        try {
            this.request.formParams(formParams);
        } catch (Exception e) {
            logger.error("Error setting form params", e);
            Assert.fail("Error setting form params");
        }
    }

    public void addCookie(String name, String value) {
        try {
            this.request.cookie(name, value);
        } catch (Exception e) {
            logger.error("Error adding cookie", e);
            Assert.fail("Error adding cookie");
        }
    }

    public Response sendRequest(String method, String endpoint) {
        try {
//            // Ensure request is built with all previously set configurations
            RequestSpecification builtRequest = RestAssured.given(this.request);
            return builtRequest.request(method, endpoint);
        } catch (Exception e) {
            logger.error("Error sending request to endpoint: " + endpoint, e);
            Assert.fail("Error sending request to endpoint: " + endpoint);
            return null;
        }
    }

    public static boolean verifyStatusCode(Response response, int expectedStatusCode) {
        try {
            return response.getStatusCode() == expectedStatusCode;
        } catch (Exception e) {
            logger.error("Error verifying status code", e);
            Assert.fail("Error verifying status code");
            return false;
        }
    }

    public static boolean verifyResponseField(Response response, String jsonPath, Object expectedValue) {
        try {
            return response.jsonPath().get(jsonPath).toString().equals(expectedValue);
        } catch (Exception e) {
            logger.error("Error verifying response field", e);
            Assert.fail("Error verifying response field");
            return false;
        }
    }

    public static boolean verifyResponseContains(Response response, String content) {
        try {
            return response.asString().contains(content);
        } catch (Exception e) {
            logger.error("Error verifying response content", e);
            Assert.fail("Error verifying response content");
            return false;
        }
    }

    public static boolean verifyResponseTime(Response response, long expectedMaxTimeMillis) {
        try {
            return response.getTime() <= expectedMaxTimeMillis;
        } catch (Exception e) {
            logger.error("Error verifying response time", e);
            Assert.fail("Error verifying response time");
            return false;
        }
    }

    public static boolean verifyResponseHeader(Response response, String header, String expectedValue) {
        try {
            return expectedValue.equals(response.getHeader(header));
        } catch (Exception e) {
            logger.error("Error verifying response header", e);
            Assert.fail("Error verifying response header");
            return false;
        }
    }

    public static String extractValueFromResponse(Response response, String jsonPath) {
        try {
            return response.jsonPath().get(jsonPath);
        } catch (Exception e) {
            logger.error("Error extracting value from response", e);
            Assert.fail("Error extracting value from response");
            return null;
        }
    }

    public static void validateJsonSchema(Response response, String schemaPath) {
        try {
            response.then().assertThat().body(matchesJsonSchemaInClasspath(schemaPath));
        } catch (Exception e) {
            logger.error("Error validating JSON schema", e);
            Assert.fail("Error validating JSON schema");
        }
    }

    public static XmlPath getXmlPath(Response response) {
        try {
            return new XmlPath(response.asString());
        } catch (Exception e) {
            logger.error("Error getting XML path", e);
            Assert.fail("Error getting XML path");
            return null;
        }
    }

    public static <T> List<T> extractValuesFromResponse(Response response, String jsonPath) {
        try {
            return response.jsonPath().getList(jsonPath);
        } catch (Exception e) {
            logger.error("Error extracting values list from response", e);
            Assert.fail("Error extracting values list from response");
            return null;
        }
    }

    public static boolean isJson(String value) {
        try {
            JsonElement element = JsonParser.parseString(value);
            return element.isJsonObject() || element.isJsonArray();
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }
}


