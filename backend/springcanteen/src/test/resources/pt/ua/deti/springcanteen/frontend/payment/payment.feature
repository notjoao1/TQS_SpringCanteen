@payment
Feature: Pay for my order on SpringCanteen

    @payment_fail_nif
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
        
    @payment_fail_no_payment_details
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
        Then I should see the error "Please fill in the payment form correctly before confirming the order!"

    @payment_fail_date
    Scenario: User wants to make an order, but doesn't enter a valid expiration date
        When I navigate to "http://localhost:5173/order"
        And I select the menu number "2"
        And I select the Main Dish number "1"
        And I select the Drink number "1"
        And I click on "Confirm selection"
        And I should see the message "Successfully added menu to order."
        And I click on "Customize and pay"
        And I fill in the NIF with "123456789"
        And I fill in the name on the card with "John Doe"
        And I fill in the card number with "1231231231231231"
        And I fill in the expiration date with "13/24"
        And I click on "Confirm order"
        Then I should see the error "Please fill in the payment form correctly before confirming the order!"

    @payment_fail_card
    Scenario: User wants to make an order, but doesn't enter a valid card number
        When I navigate to "http://localhost:5173/order"
        And I select the menu number "2"
        And I select the Main Dish number "1"
        And I select the Drink number "1"
        And I click on "Confirm selection"
        And I should see the message "Successfully added menu to order."
        And I click on "Customize and pay"
        And I fill in the NIF with "123456789"
        And I fill in the name on the card with "John Doe"
        And I fill in the card number with "123123123123123"
        And I fill in the expiration date with "12/24"
        And I click on "Confirm order"
        Then I should see the error "Please fill in the payment form correctly before confirming the order!"

    @payment_success
    Scenario: User wants to make an order, and enters all the payment details correctly
        When I navigate to "http://localhost:5173/order"
        And I select the menu number "2"
        And I select the Main Dish number "1"
        And I select the Drink number "1"
        And I click on "Confirm selection"
        And I should see the message "Successfully added menu to order."
        And I click on "Customize and pay"
        And I fill in the NIF with "123456789"
        And I fill in the name on the card with "John Doe"
        And I fill in the card number with "1231231231231231"
        And I fill in the expiration date with "12/24"
        And I click on "Confirm order"
        Then I should not see any errors

    @payment_success_desk   
    Scenario: User wants to make an order, and pay it in the desk
        When I navigate to "http://localhost:5173/order"
        And I select the menu number "2"
        And I select the Main Dish number "1"
        And I select the Drink number "1"
        And I click on "Confirm selection"
        And I should see the message "Successfully added menu to order."
        And I click on "Customize and pay"
        And I select to pay in the "desk"
        And I fill in the NIF with "123456789"
        And I click on "Confirm order"
        Then I should not see any errors