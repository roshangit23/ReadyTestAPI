Feature: Create User

  Scenario: Creating a new user
    Given I set the base URI to "testUrl"
    And I set request body:
    """
    {
      "name": "morpheus",
      "job": "leader"
    }
    """
    When I send a "POST" request to "usersPath"
    Then I expect the response status code to be 201
    And I expect the response to contain "name"

  Scenario: Creating a new user
    Given I set the base URI to "testUrl"
    And I set request body with data:
      | name     | morpheus |
      | job      | leader   |
    When I send a "POST" request to "usersPath"
    Then I expect the response status code to be 201
    And I expect the response to contain "id"

  Scenario: Creating a user with complex data
    Given I set the base URI to "testUrl"
    And I set complex request body with data:
      | name  | morpheus    |
      | job   | leader      |
      | skills| ["java", "cucumber"] |
      | address | {"street":"123 Main St","city":"Metropolis"} |
    When I send a "POST" request to "usersPath"
    Then I expect the response status code to be 201

  Scenario: Creating a user with complex data
    Given I set the base URI to "testUrl"
    And I set complex request body with data:
      | comments          | Some comments about the order                      |
      | type              | TAKE_AWAY                                          |
      | customerId        | 1                                                  |
      | customer_count    | 4                                                  |
      | restaurantSectionId | 1                                                |
      | foodItemOrders    | [{"quantity":17,"foodItemId":16},{"quantity":5,"comments":"Comments for another food item","foodItemId":4}] |
    When I send a "POST" request to "usersPath"
    Then I expect the response status code to be 201

