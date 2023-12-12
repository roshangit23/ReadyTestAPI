Feature: User Authentication

  Scenario: Successful login
    Given I set the base URI to "testUrl"
    And I set request body:
    """
    {
      "email": "eve.holt@reqres.in",
      "password": "cityslicka"
    }
    """
    When I send a "POST" request to "loginPath"
    Then I expect the response status code to be 200
    And I extract value from response using JSON path "token"
    And I set bearer token from extracted value
