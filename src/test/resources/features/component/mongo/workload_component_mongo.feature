@component @mongo
Feature: Workload Processing with MongoDB
  As a training workload service backed by MongoDB
  I want to process workload requests and persist data to MongoDB
  So that trainer workload records are accurately stored as documents

  Background:
    Given the MongoDB workload service is running
    And a valid JWT token is available

  # ---------------------------------------------------------------
  # Happy Path - ADD Workload
  # ---------------------------------------------------------------

  Scenario: Successfully add workload for a new trainer in MongoDB
    Given a workload request for trainer "mongo.trainer" with action "ADD"
    And the training date is "2024-03-15" with duration 60
    When I send the workload request
    Then the response status should be 200
    And trainer "mongo.trainer" should exist in the MongoDB database

  Scenario: Add workload accumulates minutes in the same month in MongoDB
    Given trainer "mongo.accumulate" already has 60 minutes recorded in MongoDB for "2024-05-01"
    When I send an ADD workload request for "mongo.accumulate" with 45 minutes on "2024-05-20"
    Then the response status should be 200
    And the MongoDB monthly hours for "mongo.accumulate" in year 2024 month 5 should be 105

  Scenario: Add workload for multiple months persists all months in a single document
    When I send an ADD workload request for "mongo.multimonth" with 60 minutes on "2024-01-10"
    Then the response status should be 200
    When I send an ADD workload request for "mongo.multimonth" with 90 minutes on "2024-04-15"
    Then the response status should be 200
    And the MongoDB monthly hours for "mongo.multimonth" in year 2024 month 1 should be 60
    And the MongoDB monthly hours for "mongo.multimonth" in year 2024 month 4 should be 90
    And only one MongoDB document exists for trainer "mongo.multimonth"

  Scenario: Add workload across different years stores all years in one document
    When I send an ADD workload request for "mongo.multiyear" with 60 minutes on "2023-11-01"
    Then the response status should be 200
    When I send an ADD workload request for "mongo.multiyear" with 80 minutes on "2024-11-01"
    Then the response status should be 200
    And the MongoDB monthly hours for "mongo.multiyear" in year 2023 month 11 should be 60
    And the MongoDB monthly hours for "mongo.multiyear" in year 2024 month 11 should be 80
    And only one MongoDB document exists for trainer "mongo.multiyear"

  # ---------------------------------------------------------------
  # Happy Path - DELETE Workload
  # ---------------------------------------------------------------

  Scenario: Successfully delete workload reduces monthly minutes in MongoDB
    Given trainer "mongo.delete" already has 120 minutes recorded in MongoDB for "2024-04-10"
    When I send a DELETE workload request for "mongo.delete" with 50 minutes on "2024-04-10"
    Then the response status should be 200
    And the MongoDB monthly hours for "mongo.delete" in year 2024 month 4 should be 70

  Scenario: Delete all workload minutes results in zero for that month
    Given trainer "mongo.full.delete" already has 90 minutes recorded in MongoDB for "2024-07-01"
    When I send a DELETE workload request for "mongo.full.delete" with 90 minutes on "2024-07-01"
    Then the response status should be 200
    And the MongoDB monthly hours for "mongo.full.delete" in year 2024 month 7 should be 0

  # ---------------------------------------------------------------
  # Document Uniqueness
  # ---------------------------------------------------------------

  Scenario: Multiple operations on the same trainer result in exactly one MongoDB document
    When I send an ADD workload request for "single.doc.trainer" with 30 minutes on "2024-02-01"
    Then the response status should be 200
    When I send an ADD workload request for "single.doc.trainer" with 60 minutes on "2024-03-01"
    Then the response status should be 200
    When I send a DELETE workload request for "single.doc.trainer" with 30 minutes on "2024-02-01"
    Then the response status should be 200
    And only one MongoDB document exists for trainer "single.doc.trainer"

  # ---------------------------------------------------------------
  # Summary Endpoint
  # ---------------------------------------------------------------

  Scenario: Retrieve trainer summary from MongoDB
    Given a workload request for trainer "mongo.summary" with action "ADD"
    And the training date is "2024-06-10" with duration 90
    When I send the workload request
    Then the response status should be 200
    When I request the trainer summary for "mongo.summary"
    Then the response status should be 200
    And the summary response contains trainer username "mongo.summary"

  Scenario: Trainer summary from MongoDB contains correct year and month count
    Given trainer "mongo.year.month" has MongoDB workload data:
      | year | month | duration |
      | 2024 | 1     | 60       |
      | 2024 | 6     | 75       |
      | 2024 | 12    | 90       |
    When I request the trainer summary for "mongo.year.month"
    Then the response status should be 200
    And the summary contains year 2024 with 3 months

  Scenario: Trainer summary spans multiple years in MongoDB
    Given trainer "mongo.two.years" has MongoDB workload data:
      | year | month | duration |
      | 2023 | 9     | 45       |
      | 2024 | 3     | 60       |
    When I request the trainer summary for "mongo.two.years"
    Then the response status should be 200
    And the summary contains year 2023 with 1 months
    And the summary contains year 2024 with 1 months

  # ---------------------------------------------------------------
  # Monthly Hours Endpoint
  # ---------------------------------------------------------------

  Scenario: Retrieve monthly hours from MongoDB returns correct value
    Given trainer "mongo.hours" has MongoDB workload data:
      | year | month | duration |
      | 2024 | 9     | 200      |
    When I request the monthly hours for "mongo.hours" in year 2024 month 9
    Then the response status should be 200
    And the response body contains integer 200

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

  # ---------------------------------------------------------------
  # Security - Authentication
  # ---------------------------------------------------------------

  Scenario: Reject workload request to MongoDB service without authentication token
    Given a workload request for trainer "mongo.unauth" with action "ADD"
    And the training date is "2024-03-15" with duration 60
    When I send the workload request without authentication
    Then the response status should be 401

  # ---------------------------------------------------------------
  # Edge Cases
  # ---------------------------------------------------------------

  Scenario: Add workload with large duration stores correctly in MongoDB
    Given a workload request for trainer "mongo.large" with action "ADD"
    And the training date is "2024-10-01" with duration 480
    When I send the workload request
    Then the response status should be 200
    And the MongoDB monthly hours for "mongo.large" in year 2024 month 10 should be 480

  Scenario: Add then delete workload leaves correct remainder in MongoDB
    Given trainer "mongo.add.delete" already has 100 minutes recorded in MongoDB for "2024-08-01"
    When I send an ADD workload request for "mongo.add.delete" with 50 minutes on "2024-08-15"
    Then the response status should be 200
    When I send a DELETE workload request for "mongo.add.delete" with 30 minutes on "2024-08-01"
    Then the response status should be 200
    And the MongoDB monthly hours for "mongo.add.delete" in year 2024 month 8 should be 120
