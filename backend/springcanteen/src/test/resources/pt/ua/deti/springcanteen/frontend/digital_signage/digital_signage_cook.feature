@digital_signage_cook
Feature: Digital Signage shwowing the status of customer orders, updated by the Cook

    Background: Cook employee is Logged In
        Given I have a clean local storage
        And I navigate to the sign in page
        When I submit my cook username and password
        And I click the sign in button
        Then I should be logged in as a cook

    @update_order_status_to_preparing
    Scenario: Cook confirms the order is being prepared, changing the Digital Signage
        When I navigate to the cook orders page
        And I click the Start Cooking button for the first idle order
        And I navigate to the Digital Signage page
        Then I should find one order in the "Preparing" side

    @update_order_status_to_ready
    Scenario: Cook confirms the order is ready, changing the Digital Signage
        When I navigate to the cook orders page
        And I click the Ready to pick up button for the first preparing order
        And I navigate to the Digital Signage page
        Then I should find one order in the "Delivery" side
        