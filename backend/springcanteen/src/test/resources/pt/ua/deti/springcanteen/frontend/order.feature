@order
Feature: Make an order on SpringCanteen

    Scenario: User wants to make an order
        When I navigate to "http://localhost:5173/order"
        And I select "Russian Salad & Water" menu
        And I select the Main Dish as "Russian Salad"
        And I select the Drink as "Water"
        And I click on "Confirm selection"
        Then I should see the message "Successfully added menu to order."