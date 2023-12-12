Feature: Update User

  Scenario: Updating a user's details
    Given I set the base URI to "testUrl"
    And I set request body:
    """
    {
      "name": "morpheus",
      "job": "zion resident"
    }
    """
    When I send a "PUT" request to "singleUserPath"
    Then I expect the response status code to be 200
    And I expect the response to contain "job"

  Scenario: Updating a user's details
    Given I set the base URI to "testUrl"
    And I set request body:
    """
    {
      "name": "morpheus",
      "job": "zion resident"
    }
    """
    When I send a "PUT" request to "singleUserPath"
    Then I expect the response status code to be 200
    And I expect the response to contain field "job" with value "zion resident"
    And I expect the response time to be less than 2000 milliseconds