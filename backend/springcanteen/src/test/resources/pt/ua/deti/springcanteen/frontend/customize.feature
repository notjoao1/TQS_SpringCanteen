@customize
Feature: Make an order on SpringCanteen

    Scenario: User wants to make an order
        When I navigate to "http://localhost:5173/order"
        And I select the menu number "2"
        And I select the Main Dish number "1"
        And I select the Drink number "1"
        And I click on "Confirm selection"
        Then I should see the message "Successfully added menu to order."