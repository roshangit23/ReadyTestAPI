Feature: Delete User

  Scenario: Deleting a user
    Given I set the base URI to "testUrl"
    When I send a "DELETE" request to "singleUserPath"
    Then I expect the response status code to be 204
