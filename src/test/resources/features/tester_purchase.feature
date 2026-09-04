@purchase @regression
Feature: Purchase a colour tester
  To try a paint colour at home before buying a full tin
  As a Dulux customer
  I want to add a tester for a chosen shade to my basket

  @smoke @desktop
  Scenario: Desktop customer adds a tester from the colour finder
    Given a desktop customer starts with an empty basket
    When the customer browses to shade "Violet Morning" from colour family "Violet"
    And the customer adds a tester to the basket
    Then the basket contains 1 item
    And the basket includes tester "Dulux Colour Tester" for shade "Violet Morning"

  @mobile
  Scenario: Mobile customer adds a tester from the colour finder
    Given a mobile customer starts with an empty basket
    When the customer browses to shade "Violet Morning" from colour family "Violet" using mobile navigation
    And the customer adds a tester to the basket
    Then the basket contains 1 item
    And the basket includes tester "Dulux Colour Tester" for shade "Violet Morning"

