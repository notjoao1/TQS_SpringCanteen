@payment
Feature: Pay for my order on SpringCanteen

    Scenario: User wants to make an order, but doesn't enter his NIF
        When I navigate to "http://localhost:5173/order"
        And I select the menu number "2"
        And I select the Main Dish number "1"
        And I select the Drink number "1"
        And I click on "Confirm selection"
        And I should see the message "Successfully added menu to order."
        And I click on "Customize and pay"
        And I click on "Confirm order"
        Then I should see the error "You must fill NIF with a 9 digit number! Example: 123456789"
        
    Scenario: User wants to make an order, but doesn't enter any payment details
        When I navigate to "http://localhost:5173/order"
        And I select the menu number "2"
        And I select the Main Dish number "1"
        And I select the Drink number "1"
        And I click on "Confirm selection"
        And I should see the message "Successfully added menu to order."
        And I click on "Customize and pay"
        And I fill in the NIF with "123456789"
        And I click on "Confirm order"
        Then I should see the error "Please fill in the payment form before confirming the order!"

    # Scenario: User wants to make an order adding 2 menus
    #     When I navigate to "http://localhost:5173/order"
    #     And I select the menu number "2"
    #     And I select the Main Dish number "1"
    #     And I select the Drink number "1"
    #     And I click on "Confirm selection"
    #     And I select the menu number "1"
    #     And I select the Main Dish number "1"
    #     And I select the Drink number "3"
    #     And I click on "Confirm selection"
    #     And I click to "View order"
    #     Then I should see the 2 menus added to the order


    # Scenario: User wants to make an order but cancels it
    #     When I navigate to "http://localhost:5173/order"
    #     And I select the menu number "1"
    #     And I select the Main Dish number "1"
    #     And I select the Drink number "2"
    #     And I click on "Cancel selection"
    #     And I click to "View order"
    #     Then I should see the 0 menus added to the order

    # Scenario: User wants to make an order, and then remove a menu
    #     When I navigate to "http://localhost:5173/order"
    #     And I select the menu number "2"
    #     And I select the Main Dish number "1"
    #     And I select the Drink number "1"
    #     And I click on "Confirm selection"
    #     And I click to "View order"
    #     And I remove the menu number "1"
    #     Then I should see the 0 menus added to the order   