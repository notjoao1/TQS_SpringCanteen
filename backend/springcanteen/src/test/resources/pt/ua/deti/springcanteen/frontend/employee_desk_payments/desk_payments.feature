@employee_desk_payments
Feature: Operations that a "Desk payments" employee can perform
    
    Background: Desk payments employee is Logged In
        Given I have a clean local storage
        And I navigate to the sign in page
        When I submit username and password
        And I click the sign in button
        Then I should be logged in 
    
    @view_ready_to_pay_orders
    Scenario: Desk payments employee wants to see orders that are not yet paid
        When I navigate to the Desk payments page
        Then I should see the existing not yet paid orders

    @confirm_payment_succeeded
    Scenario: Desk payments employee wants to confirm an order was successfully paid
        When I navigate to the Desk payments page
        And I click the 'Request Payment' button for the first not yet paid order
        And I click the 'Confirm' button
        Then I should see the confirmation snackbar
        And the desk payments table should have one less order to be paid
