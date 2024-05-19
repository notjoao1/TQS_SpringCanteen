@customize
Feature: Customize a menu on SpringCanteen
    Scenario: User wants to make an order and customize it
        When I navigate to "http://localhost:5173/order"
        And I select the menu number "1"
        And I select the Main Dish number "1"
        And I select the Drink number "2"
        And I click on "Confirm selection"
        And I click on "Customize and pay"
        And I click to customize the menu "1"
        And I increase the quantity of item "Cheese" by "1"
        And I increase the quantity of item "Bread" by "2"
        And I decrease the quantity of item "Ham" by "1"
        Then I should see the total price of "5.50"