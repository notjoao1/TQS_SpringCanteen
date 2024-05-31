@employee_cook
Feature: Operations that a "Cook" employee can perform
    
    Background: Cook employee is Logged In
        Given I have a clean local storage
        And I navigate to the sign in page
        When I submit username and password
        And I click the sign in button
        Then I should be logged in 
    
    @view_existing_idle_orders
    Scenario: Cook wants to see existing idle orders
        When I navigate to the cook orders page
        Then I should see the single existing priority idle order

    @start_cooking_idle_order
    Scenario: Cook wants to start cooking the top priority order
        When I navigate to the cook orders page
        And I click the Start Cooking button for the first idle order
        Then I should see the confirmation snackbar
        And I should see an order in the 'Preparing' orders section
        And I should no longer see any orders in the 'Idle' orders section

    @confirm_finished_cooking_order
    Scenario: Cook wants to confirm he finished cooking an order
        When I navigate to the cook orders page
        And I click the Ready to pick up button for the first preparing order
        Then I should see the confirmation snackbar
        And I should no longer see an order in the 'Preparing' orders section