@integration
Feature: JMS Integration and End-to-End Workload Flow
  As a Training Workload Service
  I want to consume JMS messages published by Gym CRM
  So that trainer workload data is persisted and queryable via REST

  Background:
    Given the integration test environment is running
    And the JMS queue "workload.queue.test" is available
    And a valid JWT token is available

  # ---------------------------------------------------------------
  # JMS - Basic Message Consumption
  # ---------------------------------------------------------------

  Scenario: JMS ADD message is consumed and workload is persisted
    Given Gym CRM publishes an ADD workload message for trainer "jms.trainer" with 60 minutes on "2024-03-15"
    When the Training Workload Service processes the JMS message
    Then trainer "jms.trainer" workload is persisted in the database
    And the monthly hours for "jms.trainer" in year 2024 month 3 should be 60

  Scenario: JMS ADD message is retrievable via REST after processing
    Given Gym CRM publishes an ADD workload message for trainer "jms.rest.trainer" with 90 minutes on "2024-05-10"
    When the Training Workload Service processes the JMS message
    Then trainer "jms.rest.trainer" workload is persisted in the database
    And the workload is retrievable via REST GET for "jms.rest.trainer"

  Scenario: JMS DELETE message reduces trainer workload correctly
    Given trainer "jms.delete.trainer" already has 120 minutes recorded for "2024-04-01"
    And Gym CRM publishes a DELETE workload message for trainer "jms.delete.trainer" with 40 minutes on "2024-04-01"
    When the Training Workload Service processes the JMS message
    Then the monthly hours for "jms.delete.trainer" in year 2024 month 4 should be 80

  # ---------------------------------------------------------------
  # JMS - Multiple Messages
  # ---------------------------------------------------------------

  Scenario: Multiple JMS ADD messages accumulate minutes for the same trainer
    Given Gym CRM publishes an ADD workload message for trainer "jms.multi" with 30 minutes on "2024-06-01"
    And Gym CRM publishes an ADD workload message for trainer "jms.multi" with 45 minutes on "2024-06-15"
    And Gym CRM publishes an ADD workload message for trainer "jms.multi" with 60 minutes on "2024-06-28"
    When all messages are processed by the Training Workload Service
    Then the monthly hours for "jms.multi" in year 2024 month 6 should be 135

  Scenario: JMS messages for different trainers are processed independently
    Given Gym CRM publishes an ADD workload message for trainer "jms.trainer.a" with 60 minutes on "2024-07-01"
    And Gym CRM publishes an ADD workload message for trainer "jms.trainer.b" with 90 minutes on "2024-07-01"
    When all messages are processed by the Training Workload Service
    Then trainer "jms.trainer.a" workload is persisted in the database
    And trainer "jms.trainer.b" workload is persisted in the database
    And the monthly hours for "jms.trainer.a" in year 2024 month 7 should be 60
    And the monthly hours for "jms.trainer.b" in year 2024 month 7 should be 90

  Scenario: JMS messages for different months are stored separately
    Given Gym CRM publishes an ADD workload message for trainer "jms.months" with 60 minutes on "2024-01-10"
    And Gym CRM publishes an ADD workload message for trainer "jms.months" with 75 minutes on "2024-03-20"
    And Gym CRM publishes an ADD workload message for trainer "jms.months" with 90 minutes on "2024-08-05"
    When all messages are processed by the Training Workload Service
    Then the monthly hours for "jms.months" in year 2024 month 1 should be 60
    And the monthly hours for "jms.months" in year 2024 month 3 should be 75
    And the monthly hours for "jms.months" in year 2024 month 8 should be 90

  # ---------------------------------------------------------------
  # JMS - Transaction ID Tracking
  # ---------------------------------------------------------------

  Scenario: JMS message with transaction ID is processed successfully
    Given Gym CRM publishes a workload message with transaction ID "TXN-20240315-001" for trainer "jms.txn.trainer" with 60 minutes on "2024-03-15"
    When the Training Workload Service processes the JMS message
    Then trainer "jms.txn.trainer" workload is persisted in the database
    And the monthly hours for "jms.txn.trainer" in year 2024 month 3 should be 60

  # ---------------------------------------------------------------
  # JMS - Error Handling and Dead Letter Queue
  # ---------------------------------------------------------------

  Scenario: Malformed JMS message is routed to dead letter queue
    Given Gym CRM publishes a malformed JMS message to the workload queue
    When the Training Workload Service attempts to process the message
    Then the message should be moved to the dead letter queue "workload.dlq.test"
    And no workload data is persisted for malformed message

  # ---------------------------------------------------------------
  # REST - Direct HTTP Integration
  # ---------------------------------------------------------------

  Scenario: REST POST workload request is processed and persisted end-to-end
    Given a workload request for trainer "rest.e2e.trainer" with action "ADD"
    And the training date is "2024-09-01" with duration 75
    When I send the workload request
    Then the response status should be 200
    And the workload is retrievable via REST GET for "rest.e2e.trainer"

  Scenario: REST GET summary returns correct data after multiple operations
    Given trainer "rest.summary.trainer" already has 60 minutes recorded for "2024-02-01"
    When I send an ADD workload request for "rest.summary.trainer" with 30 minutes on "2024-02-15"
    Then the response status should be 200
    When I request the trainer summary for "rest.summary.trainer"
    Then the response status should be 200
    And the summary response contains trainer username "rest.summary.trainer"

  # ---------------------------------------------------------------
  # Security - JWT Authentication
  # ---------------------------------------------------------------

  Scenario: REST request with invalid JWT token is rejected
    Given a workload request for trainer "jwt.invalid.trainer" with action "ADD"
    And the training date is "2024-03-15" with duration 60
    When I send the workload request with an invalid JWT token
    Then the response status should be 401

  # ---------------------------------------------------------------
  # End-to-End: JMS → DB → REST
  # ---------------------------------------------------------------

  Scenario: Full flow - Gym CRM publishes JMS, data stored, REST returns correct summary
    Given Gym CRM publishes an ADD workload message for trainer "e2e.full.trainer" with 60 minutes on "2024-10-01"
    And Gym CRM publishes an ADD workload message for trainer "e2e.full.trainer" with 90 minutes on "2024-11-01"
    When all messages are processed by the Training Workload Service
    Then trainer "e2e.full.trainer" workload is persisted in the database
    And the monthly hours for "e2e.full.trainer" in year 2024 month 10 should be 60
    And the monthly hours for "e2e.full.trainer" in year 2024 month 11 should be 90
    And the workload is retrievable via REST GET for "e2e.full.trainer"

  Scenario: Full flow - JMS ADD then REST DELETE leaves correct final state
    Given Gym CRM publishes an ADD workload message for trainer "e2e.add.delete" with 150 minutes on "2024-12-01"
    When all messages are processed by the Training Workload Service
    Then the monthly hours for "e2e.add.delete" in year 2024 month 12 should be 150
    When I send a DELETE workload request for "e2e.add.delete" with 50 minutes on "2024-12-01"
    Then the response status should be 200
    And the monthly hours for "e2e.add.delete" in year 2024 month 12 should be 100
