@visualizer @regression
Feature: Open the Dulux Visualizer experience
  To preview a colour before making a purchase decision
  As a Dulux customer
  I want to open the Visualizer from a selected shade page

  @smoke @desktop
  Scenario: Desktop customer opens the Visualizer for a shade
    Given a desktop customer is viewing shade "Violet Morning"
    When the customer opens the Visualizer experience
    Then the Visualizer opens in a new tab
    And the Visualizer page URL is "https://www.dulux.co.uk/en/articles/dulux-visualizer-app"

  @mobile
  Scenario: Mobile customer tries to open the Visualizer for a shade
    Given a mobile customer is viewing shade "Violet Morning"
    When the customer opens the Visualizer experience
    Then the page shows message "Inconsistent store data, contact support@adjust.com"

