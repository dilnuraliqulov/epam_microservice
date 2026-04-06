package com.example.workload.component.h2.steps;

import com.example.workload.dto.WorkloadRequest;
import com.example.workload.enums.ActionType;
import com.example.workload.repository.TrainerWorkloadRepository;
import com.example.workload.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class H2WorkloadSteps {


    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    private TrainerWorkloadRepository trainerWorkloadRepository;

    private JwtTokenProvider jwtTokenProvider;

    private ObjectMapper objectMapper;

    public H2WorkloadSteps(TestRestTemplate restTemplate,
                             TrainerWorkloadRepository trainerWorkloadRepository,
                             JwtTokenProvider jwtTokenProvider,
                             ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.trainerWorkloadRepository = trainerWorkloadRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    // ----------------------------------------------------------------
    // State shared across steps within one scenario
    // ----------------------------------------------------------------

    private String validToken;
    private WorkloadRequest currentRequest;
    private ResponseEntity<String> lastResponse;
    private Map<String, Object> invalidRequestBody;

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/workload";
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(validToken);
        return headers;
    }

    private HttpHeaders noAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private WorkloadRequest buildRequest(String username, ActionType action, LocalDate date, int duration) {
        return WorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(true)
                .trainingDate(date)
                .trainingDuration(duration)
                .actionType(action)
                .build();
    }

    private void sendWorkload(WorkloadRequest request, boolean withAuth) {
        HttpEntity<WorkloadRequest> entity = new HttpEntity<>(
                request,
                withAuth ? authHeaders() : noAuthHeaders()
        );
        lastResponse = restTemplate.exchange(baseUrl(), HttpMethod.POST, entity, String.class);
    }

    // ----------------------------------------------------------------
    // Background Steps
    // ----------------------------------------------------------------

    @Before
    public void cleanDatabase() {
        trainerWorkloadRepository.deleteAll();
    }

    @Given("the H2 workload service is running")
    public void theH2WorkloadServiceIsRunning() {
        // Spring Boot starts automatically; just verify health
        ResponseEntity<String> health = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health", String.class);
        assertThat(health.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Given("a valid JWT token is available")
    public void aValidJwtTokenIsAvailable() {
        validToken = jwtTokenProvider.generateToken("test-user");
        assertThat(validToken).isNotBlank();
    }

    // ----------------------------------------------------------------
    // Given Steps - Building Requests
    // ----------------------------------------------------------------

    @Given("a workload request for trainer {string} with action {string}")
    public void aWorkloadRequestForTrainerWithAction(String username, String action) {
        currentRequest = WorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(true)
                .trainingDate(LocalDate.of(2024, 3, 15))
                .trainingDuration(60)
                .actionType(ActionType.valueOf(action))
                .build();
    }

    @Given("the training date is {string} with duration {int}")
    public void theTrainingDateIsWithDuration(String date, int duration) {
        currentRequest.setTrainingDate(LocalDate.parse(date));
        currentRequest.setTrainingDuration(duration);
    }

    @Given("the training duration is {int}")
    public void theTrainingDurationIs(int duration) {
        currentRequest.setTrainingDuration(duration);
    }

    @Given("a workload request for trainer {string} without action type")
    public void aWorkloadRequestWithoutActionType(String username) {
        // Build valid request then null out the action type
        currentRequest = WorkloadRequest.builder()
                .trainerUsername(username)
                .trainerFirstName("Test")
                .trainerLastName("Trainer")
                .isActive(true)
                .trainingDate(LocalDate.of(2024, 3, 15))
                .trainingDuration(60)
                .actionType(null)
                .build();
        // Wrap as raw map for invalid request path
        invalidRequestBody = new HashMap<>();
        invalidRequestBody.put("trainerUsername", username);
        invalidRequestBody.put("trainerFirstName", "Test");
        invalidRequestBody.put("trainerLastName", "Trainer");
        invalidRequestBody.put("isActive", true);
        invalidRequestBody.put("trainingDate", "2024-03-15");
        invalidRequestBody.put("trainingDuration", 60);
        // deliberately omit actionType
    }

    @Given("a workload request with missing {string}")
    public void aWorkloadRequestWithMissing(String missingField) {
        invalidRequestBody = new HashMap<>();
        invalidRequestBody.put("trainerUsername", "test.trainer");
        invalidRequestBody.put("trainerFirstName", "Test");
        invalidRequestBody.put("trainerLastName", "Trainer");
        invalidRequestBody.put("isActive", true);
        invalidRequestBody.put("trainingDate", "2024-03-15");
        invalidRequestBody.put("trainingDuration", 60);
        invalidRequestBody.put("actionType", "ADD");
        // Remove the specified field to trigger validation error
        invalidRequestBody.remove(missingField);
    }

    @Given("a clean workload state for trainer {string}")
    public void aCleanWorkloadStateForTrainer(String username) {
        trainerWorkloadRepository.findByUsername(username)
                .ifPresent(trainerWorkloadRepository::delete);
    }

    @Given("trainer {string} already has {int} minutes recorded for {string}")
    public void trainerAlreadyHasMinutesRecordedFor(String username, int minutes, String dateStr) {
        // Seed initial data via REST so service logic runs correctly
        WorkloadRequest seed = buildRequest(
                username, ActionType.ADD,
                LocalDate.parse(dateStr), minutes
        );
        sendWorkload(seed, true);
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Given("trainer {string} has workload data:")
    public void trainerHasWorkloadData(String username, DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            int year = Integer.parseInt(row.get("year"));
            int month = Integer.parseInt(row.get("month"));
            int duration = Integer.parseInt(row.get("duration"));
            WorkloadRequest seed = buildRequest(
                    username, ActionType.ADD,
                    LocalDate.of(year, month, 1), duration
            );
            sendWorkload(seed, true);
            assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    // ----------------------------------------------------------------
    // When Steps - Sending Requests
    // ----------------------------------------------------------------

    @When("I send the workload request")
    public void iSendTheWorkloadRequest() {
        sendWorkload(currentRequest, true);
    }

    @When("I send the workload request without authentication")
    public void iSendTheWorkloadRequestWithoutAuthentication() {
        sendWorkload(currentRequest, false);
    }

    @When("I send the invalid workload request")
    public void iSendTheInvalidWorkloadRequest() {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(invalidRequestBody, authHeaders());
        lastResponse = restTemplate.exchange(baseUrl(), HttpMethod.POST, entity, String.class);
    }

    @When("I send an ADD workload request for {string} with {int} minutes on {string}")
    public void iSendAnAddWorkloadRequestForWithMinutesOn(String username, int minutes, String dateStr) {
        WorkloadRequest req = buildRequest(username, ActionType.ADD, LocalDate.parse(dateStr), minutes);
        sendWorkload(req, true);
    }

    @When("I send a DELETE workload request for {string} with {int} minutes on {string}")
    public void iSendADeleteWorkloadRequestForWithMinutesOn(String username, int minutes, String dateStr) {
        WorkloadRequest req = buildRequest(username, ActionType.DELETE, LocalDate.parse(dateStr), minutes);
        sendWorkload(req, true);
    }

    @When("I request the trainer summary for {string}")
    public void iRequestTheTrainerSummaryFor(String username) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        lastResponse = restTemplate.exchange(
                baseUrl() + "/" + username, HttpMethod.GET, entity, String.class);
    }

    @When("I request the monthly hours for {string} in year {int} month {int}")
    public void iRequestTheMonthlyHoursForInYearMonth(String username, int year, int month) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        lastResponse = restTemplate.exchange(
                baseUrl() + "/" + username + "/years/" + year + "/months/" + month,
                HttpMethod.GET, entity, String.class);
    }

    // ----------------------------------------------------------------
    // Then Steps - Assertions
    // ----------------------------------------------------------------

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(lastResponse.getStatusCode().value())
                .as("Expected HTTP status %d but got %d. Body: %s",
                        expectedStatus, lastResponse.getStatusCode().value(), lastResponse.getBody())
                .isEqualTo(expectedStatus);
    }

    @Then("trainer {string} should exist in the H2 database")
    public void trainerShouldExistInTheH2Database(String username) {
        assertThat(trainerWorkloadRepository.existsByUsername(username))
                .as("Trainer '%s' should exist in H2 DB", username)
                .isTrue();
    }

    @Then("the monthly hours for {string} in year {int} month {int} should be {int}")
    public void theMonthlyHoursForInYearMonthShouldBe(String username, int year, int month, int expectedHours) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<Integer> response = restTemplate.exchange(
                baseUrl() + "/" + username + "/years/" + year + "/months/" + month,
                HttpMethod.GET, entity, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .as("Monthly hours for %s in %d/%d", username, year, month)
                .isEqualTo(expectedHours);
    }

    @Then("the summary response contains trainer username {string}")
    public void theSummaryResponseContainsTrainerUsername(String username) {
        assertThat(lastResponse.getBody())
                .as("Response body should contain trainer username '%s'", username)
                .contains(username);
    }

    @Then("the summary contains year {int} with {int} months")
    public void theSummaryContainsYearWithMonths(int year, int expectedMonthCount) throws Exception {
        var body = objectMapper.readTree(lastResponse.getBody());
        var years = body.get("years");
        assertThat(years).isNotNull();

        boolean yearFound = false;
        for (var yearNode : years) {
            if (yearNode.get("year").asInt() == year) {
                yearFound = true;
                int monthCount = yearNode.get("months").size();
                assertThat(monthCount)
                        .as("Year %d should have %d months, but had %d", year, expectedMonthCount, monthCount)
                        .isEqualTo(expectedMonthCount);
                break;
            }
        }
        assertThat(yearFound).as("Year %d not found in summary response", year).isTrue();
    }

    @Then("the response body contains integer {int}")
    public void theResponseBodyContainsInteger(int expected) {
        assertThat(lastResponse.getBody()).isNotNull();
        assertThat(Integer.parseInt(lastResponse.getBody().trim())).isEqualTo(expected);
    }
}