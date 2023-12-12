Feature: List Users
  @Login
  Scenario: Getting a list of users
    Given I set the base URI to "testUrl"
    And I set query parameters:
    |page | 2 |
    When I send a "GET" request to "usersPath"
    Then I expect the response status code to be 200
    And I expect the response to contain "page"
    And I expect the response to contain field "page" with value "2"

  @Login
  Scenario: Getting details of a specific user
    Given I set the base URI to "testUrl"
    When I send a "GET" request to "singleUserPath"
    Then I expect the response status code to be 200
    And I expect the response to contain field "data.id" with value "2"
    And I expect the response time to be less than 1000 milliseconds

  @Login
  Scenario: Getting a list of users with response time validation
    Given I set the base URI to "testUrl"
    And I set query parameters:
      |page | 2 |
    When I send a "GET" request to "usersPath"
    Then I expect the response status code to be 200
    And I expect the response time to be less than 1500 milliseconds