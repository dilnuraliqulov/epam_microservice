@component @h2
Feature: Workload Processing with H2 Database
  As a training workload service
  I want to process workload requests and persist data to H2 database
  So that trainer workload records are accurately maintained

  Background:
    Given the H2 workload service is running
    And a valid JWT token is available

  # ---------------------------------------------------------------
  # Happy Path - ADD Workload
  # ---------------------------------------------------------------

  Scenario: Successfully add workload for a new trainer
    Given a workload request for trainer "john.doe" with action "ADD"
    And the training date is "2024-03-15" with duration 60
    When I send the workload request
    Then the response status should be 200
    And trainer "john.doe" should exist in the H2 database

  Scenario: Add workload accumulates minutes for the same month
    Given a clean workload state for trainer "jane.smith"
    And trainer "jane.smith" already has 60 minutes recorded for "2024-05-01"
    When I send an ADD workload request for "jane.smith" with 45 minutes on "2024-05-20"
    Then the response status should be 200
    And the monthly hours for "jane.smith" in year 2024 month 5 should be 105

  Scenario: Add workload for multiple months in the same year
    Given a clean workload state for trainer "multi.month"
    When I send an ADD workload request for "multi.month" with 90 minutes on "2024-01-10"
    Then the response status should be 200
    When I send an ADD workload request for "multi.month" with 120 minutes on "2024-03-15"
    Then the response status should be 200
    And the monthly hours for "multi.month" in year 2024 month 1 should be 90
    And the monthly hours for "multi.month" in year 2024 month 3 should be 120

  Scenario: Add workload for multiple years
    Given a clean workload state for trainer "multi.year"
    When I send an ADD workload request for "multi.year" with 60 minutes on "2023-06-01"
    Then the response status should be 200
    When I send an ADD workload request for "multi.year" with 75 minutes on "2024-06-01"
    Then the response status should be 200
    And the monthly hours for "multi.year" in year 2023 month 6 should be 60
    And the monthly hours for "multi.year" in year 2024 month 6 should be 75

  # ---------------------------------------------------------------
  # Happy Path - DELETE Workload
  # ---------------------------------------------------------------

  Scenario: Successfully delete workload reduces monthly minutes
    Given a clean workload state for trainer "delete.trainer"
    And trainer "delete.trainer" already has 120 minutes recorded for "2024-04-10"
    When I send a DELETE workload request for "delete.trainer" with 40 minutes on "2024-04-10"
    Then the response status should be 200
    And the monthly hours for "delete.trainer" in year 2024 month 4 should be 80

  Scenario: Delete all workload minutes for a month
    Given a clean workload state for trainer "full.delete"
    And trainer "full.delete" already has 60 minutes recorded for "2024-07-01"
    When I send a DELETE workload request for "full.delete" with 60 minutes on "2024-07-01"
    Then the response status should be 200
    And the monthly hours for "full.delete" in year 2024 month 7 should be 0

  # ---------------------------------------------------------------
  # Summary Endpoint
  # ---------------------------------------------------------------

  Scenario: Retrieve trainer summary after adding workload
    Given a workload request for trainer "summary.trainer" with action "ADD"
    And the training date is "2024-02-10" with duration 90
    When I send the workload request
    Then the response status should be 200
    When I request the trainer summary for "summary.trainer"
    Then the response status should be 200
    And the summary response contains trainer username "summary.trainer"

  Scenario: Trainer summary contains correct year and month data
    Given trainer "year.month.trainer" has workload data:
      | year | month | duration |
      | 2024 | 1     | 60       |
      | 2024 | 2     | 90       |
      | 2024 | 3     | 120      |
    When I request the trainer summary for "year.month.trainer"
    Then the response status should be 200
    And the summary contains year 2024 with 3 months

  # ---------------------------------------------------------------
  # Monthly Hours Endpoint
  # ---------------------------------------------------------------

  Scenario: Retrieve monthly hours returns correct value
    Given trainer "hours.check" has workload data:
      | year | month | duration |
      | 2024 | 8     | 150      |
    When I request the monthly hours for "hours.check" in year 2024 month 8
    Then the response status should be 200
    And the response body contains integer 150

  Scenario: Retrieve monthly hours for trainer with no data returns zero
    Given a clean workload state for trainer "no.hours"
    When I send an ADD workload request for "no.hours" with 0 minutes on "2024-09-01"
    Then the response status should be 200
    And the monthly hours for "no.hours" in year 2024 month 9 should be 0

  # ---------------------------------------------------------------
  # Validation - Missing Required Fields
  # ---------------------------------------------------------------

  Scenario Outline: Reject workload request with missing required field
    Given a workload request with missing "<missingField>"
    When I send the invalid workload request
    Then the response status should be 400

    Examples:
      | missingField     |
      | trainerUsername  |
      | trainingDate     |
      | trainingDuration |
      | actionType       |

  Scenario: Reject workload request without action type
    Given a workload request for trainer "no.action" without action type
    When I send the invalid workload request
    Then the response status should be 400

  # ---------------------------------------------------------------
  # Security - Authentication
  # ---------------------------------------------------------------

  Scenario: Reject workload request without authentication token
    Given a workload request for trainer "unauth.trainer" with action "ADD"
    And the training date is "2024-03-15" with duration 60
    When I send the workload request without authentication
    Then the response status should be 401

  # ---------------------------------------------------------------
  # Edge Cases
  # ---------------------------------------------------------------

  Scenario: Add workload with minimum duration
    Given a workload request for trainer "min.duration" with action "ADD"
    And the training date is "2024-11-01" with duration 1
    When I send the workload request
    Then the response status should be 200
    And the monthly hours for "min.duration" in year 2024 month 11 should be 1

  Scenario: Multiple ADD requests for same trainer accumulate correctly
    Given a clean workload state for trainer "accumulate.trainer"
    When I send an ADD workload request for "accumulate.trainer" with 30 minutes on "2024-06-01"
    Then the response status should be 200
    When I send an ADD workload request for "accumulate.trainer" with 30 minutes on "2024-06-15"
    Then the response status should be 200
    When I send an ADD workload request for "accumulate.trainer" with 30 minutes on "2024-06-28"
    Then the response status should be 200
    And the monthly hours for "accumulate.trainer" in year 2024 month 6 should be 90
