@digital_signage_desk
Feature: Digital Signage shwowing the status of customer orders, updated by the Desk Employee

    Background: Desk orders employee is Logged In
        Given I have a clean local storage
        And I navigate to the sign in page
        When I submit my desk employee username and password
        And I click the sign in button
        Then I should be logged in as a desk employee

    @update_order_status_to_delivered
    Scenario: Desk Employee confirms the order was delivered, changing the Digital Signage
        When I navigate to the ready orders page
        And I click the Confirm pick up button for the first ready order
        And I navigate to the Digital Signage page
        Then I should not find any order