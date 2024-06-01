@employee_desk_orders
Feature: Operations that a "Desk orders" employee can perform
    
    Background: Desk orders employee is Logged In
        Given I have a clean local storage
        And I navigate to the sign in page
        When I submit username and password
        And I click the sign in button
        Then I should be logged in 
    
    @view_ready_to_pick_up_orders
    Scenario: Desk orders employee wants to see orders ready to pick up
        When I navigate to the ready orders page
        Then I should see the single existing priority ready order

    @confirm_ready_to_pick_up_order
    Scenario: Desk orders employee wants to confirm an order that is ready to pick up
        When I navigate to the ready orders page
        And I click the Confirm pick up button for the first ready order
        Then I should see the confirmation snackbar
