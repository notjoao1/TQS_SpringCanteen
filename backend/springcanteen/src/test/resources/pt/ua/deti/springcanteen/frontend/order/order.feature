@order
Feature: Make an order on SpringCanteen
    
    @order_basic
    Scenario: User wants to make an order
        When I navigate to "http://localhost:5173/order"
        And I select the menu number "2"
        And I select the Main Dish number "1"
        And I select the Drink number "1"
        And I click on "Confirm selection"
        Then I should see the message "Successfully added menu to order."

    @order_with_2_menus
    Scenario: User wants to make an order adding 2 menus
        When I navigate to "http://localhost:5173/order"
        And I select the menu number "2"
        And I select the Main Dish number "1"
        And I select the Drink number "1"
        And I click on "Confirm selection"
        And I select the menu number "1"
        And I select the Main Dish number "1"
        And I select the Drink number "3"
        And I click on "Confirm selection"
        And I click to "View order"
        Then I should see the 2 menus added to the order

    @order_cancel
    Scenario: User wants to make an order but cancels it
        When I navigate to "http://localhost:5173/order"
        And I select the menu number "1"
        And I select the Main Dish number "1"
        And I select the Drink number "2"
        And I click on "Cancel selection"
        And I click to "View order"
        Then I should see the 0 menus added to the order

    @order_remove_a_menu
    Scenario: User wants to make an order, and then remove a menu
        When I navigate to "http://localhost:5173/order"
        And I select the menu number "2"
        And I select the Main Dish number "1"
        And I select the Drink number "1"
        And I click on "Confirm selection"
        And I click to "View order"
        And I remove the menu number "1"
        Then I should see the 0 menus added to the order   

    @order_full_flow
    Scenario: User wants to make an order with a menu, and see his order number
        When I navigate to "http://localhost:5173/order"
        And I select the menu number "3"
        And I select the Main Dish number "1"
        And I select the Drink number "1"
        And I click on "Confirm selection"
        And I click on "Customize and pay"
        And I write my NIF as "123456789"
        And I check the Priority Queue checkbox
        And I click on "Confirm order"
        Then I should see my order number as "7"
        And I should see total cost as "7.00"€
        And I should see that my order is a priority order