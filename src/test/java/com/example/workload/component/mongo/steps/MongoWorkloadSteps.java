package com.example.workload.component.mongo.steps;

import com.example.workload.dto.WorkloadRequest;
import com.example.workload.enums.ActionType;
import com.example.workload.repository.mongo.TrainerWorkloadMongoRepository;
import com.example.workload.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for MongoDB component tests.
 *
 * Location: src/test/java/com/example/workload/component/mongo/steps/MongoWorkloadSteps.java
 *
 * Uses TestRestTemplate (full HTTP stack) + TrainerWorkloadMongoRepository for DB assertions.
 */
public class MongoWorkloadSteps {

    // ----------------------------------------------------------------
    // Injected beans
    // ----------------------------------------------------------------

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    private TrainerWorkloadMongoRepository mongoRepository;

    private JwtTokenProvider jwtTokenProvider;

    private ObjectMapper objectMapper;

    public MongoWorkloadSteps(TestRestTemplate restTemplate,
                             TrainerWorkloadMongoRepository mongoRepository,
                             JwtTokenProvider jwtTokenProvider,
                             ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.mongoRepository = mongoRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    // ----------------------------------------------------------------
    // Per-scenario state
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
                .trainerFirstName("Mongo")
                .trainerLastName("Trainer")
                .isActive(true)
                .trainingDate(date)
                .trainingDuration(duration)
                .actionType(action)
                .build();
    }

    private void postWorkload(WorkloadRequest request, boolean withAuth) {
        HttpEntity<WorkloadRequest> entity = new HttpEntity<>(
                request, withAuth ? authHeaders() : noAuthHeaders());
        lastResponse = restTemplate.exchange(baseUrl(), HttpMethod.POST, entity, String.class);
    }

    // ----------------------------------------------------------------
    // Background / Setup
    // ----------------------------------------------------------------

    @Before
    public void cleanMongoDatabase() {
        mongoRepository.deleteAll();
    }

    @Given("the MongoDB workload service is running")
    public void theMongoDbWorkloadServiceIsRunning() {
        ResponseEntity<String> health = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health", String.class);
        assertThat(health.getStatusCode().is2xxSuccessful()).isTrue();
    }

    // Note: "a valid JWT token is available" is shared step — if running in same glue package,
    // declare it here too or extract to shared steps class.
    @Given("a valid JWT token is available")
    public void aValidJwtTokenIsAvailable() {
        validToken = jwtTokenProvider.generateToken("test-mongo-user");
        assertThat(validToken).isNotBlank();
    }

    // ----------------------------------------------------------------
    // Given - Request building
    // ----------------------------------------------------------------

    @Given("a workload request for trainer {string} with action {string}")
    public void aWorkloadRequestForTrainerWithAction(String username, String action) {
        currentRequest = buildRequest(username, ActionType.valueOf(action),
                LocalDate.of(2024, 3, 15), 60);
    }

    @Given("the training date is {string} with duration {int}")
    public void theTrainingDateIsWithDuration(String date, int duration) {
        currentRequest.setTrainingDate(LocalDate.parse(date));
        currentRequest.setTrainingDuration(duration);
    }

    @Given("a workload request with missing {string}")
    public void aWorkloadRequestWithMissing(String missingField) {
        invalidRequestBody = new HashMap<>();
        invalidRequestBody.put("trainerUsername", "mongo.test");
        invalidRequestBody.put("trainerFirstName", "Mongo");
        invalidRequestBody.put("trainerLastName", "Trainer");
        invalidRequestBody.put("isActive", true);
        invalidRequestBody.put("trainingDate", "2024-03-15");
        invalidRequestBody.put("trainingDuration", 60);
        invalidRequestBody.put("actionType", "ADD");
        invalidRequestBody.remove(missingField);
    }

    @Given("trainer {string} already has {int} minutes recorded in MongoDB for {string}")
    public void trainerAlreadyHasMinutesInMongoFor(String username, int minutes, String dateStr) {
        WorkloadRequest seed = buildRequest(username, ActionType.ADD, LocalDate.parse(dateStr), minutes);
        postWorkload(seed, true);
        assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Given("trainer {string} has MongoDB workload data:")
    public void trainerHasMongoWorkloadData(String username, DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            int year = Integer.parseInt(row.get("year"));
            int month = Integer.parseInt(row.get("month"));
            int duration = Integer.parseInt(row.get("duration"));
            WorkloadRequest seed = buildRequest(username, ActionType.ADD,
                    LocalDate.of(year, month, 1), duration);
            postWorkload(seed, true);
            assertThat(lastResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    // ----------------------------------------------------------------
    // When - Actions
    // ----------------------------------------------------------------

    @When("I send the workload request")
    public void iSendTheWorkloadRequest() {
        postWorkload(currentRequest, true);
    }

    @When("I send the workload request without authentication")
    public void iSendTheWorkloadRequestWithoutAuthentication() {
        postWorkload(currentRequest, false);
    }

    @When("I send the invalid workload request")
    public void iSendTheInvalidWorkloadRequest() {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(invalidRequestBody, authHeaders());
        lastResponse = restTemplate.exchange(baseUrl(), HttpMethod.POST, entity, String.class);
    }

    @When("I send an ADD workload request for {string} with {int} minutes on {string}")
    public void iSendAnAddWorkloadRequestForWithMinutesOn(String username, int minutes, String date) {
        WorkloadRequest req = buildRequest(username, ActionType.ADD, LocalDate.parse(date), minutes);
        postWorkload(req, true);
    }

    @When("I send a DELETE workload request for {string} with {int} minutes on {string}")
    public void iSendADeleteWorkloadRequestForWithMinutesOn(String username, int minutes, String date) {
        WorkloadRequest req = buildRequest(username, ActionType.DELETE, LocalDate.parse(date), minutes);
        postWorkload(req, true);
    }

    @When("I request the trainer summary for {string}")
    public void iRequestTheTrainerSummaryFor(String username) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        lastResponse = restTemplate.exchange(
                baseUrl() + "/" + username, HttpMethod.GET, entity, String.class);
    }

    @When("I request the monthly hours for {string} in year {int} month {int}")
    public void iRequestTheMonthlyHoursFor(String username, int year, int month) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        lastResponse = restTemplate.exchange(
                baseUrl() + "/" + username + "/years/" + year + "/months/" + month,
                HttpMethod.GET, entity, String.class);
    }

    // ----------------------------------------------------------------
    // Then - Assertions
    // ----------------------------------------------------------------

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(lastResponse.getStatusCode().value())
                .as("Expected HTTP %d but got %d. Body: %s",
                        expectedStatus, lastResponse.getStatusCode().value(), lastResponse.getBody())
                .isEqualTo(expectedStatus);
    }

    @Then("trainer {string} should exist in the MongoDB database")
    public void trainerShouldExistInMongoDB(String username) {
        assertThat(mongoRepository.existsByUsername(username))
                .as("Trainer '%s' should exist in MongoDB", username)
                .isTrue();
    }

    @Then("the MongoDB monthly hours for {string} in year {int} month {int} should be {int}")
    public void mongoMonthlyHoursShouldBe(String username, int year, int month, int expected) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<Integer> resp = restTemplate.exchange(
                baseUrl() + "/" + username + "/years/" + year + "/months/" + month,
                HttpMethod.GET, entity, Integer.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody())
                .as("MongoDB monthly hours for %s %d/%d", username, year, month)
                .isEqualTo(expected);
    }

    @Then("only one MongoDB document exists for trainer {string}")
    public void onlyOneMongoDocumentExistsForTrainer(String username) {
        long count = mongoRepository.findAll().stream()
                .filter(d -> d.getUsername().equals(username))
                .count();
        assertThat(count)
                .as("Expected exactly 1 MongoDB document for trainer '%s', found %d", username, count)
                .isEqualTo(1L);
    }

    @Then("the summary response contains trainer username {string}")
    public void theSummaryResponseContainsTrainerUsername(String username) {
        assertThat(lastResponse.getBody()).contains(username);
    }

    @Then("the summary contains year {int} with {int} months")
    public void theSummaryContainsYearWithMonths(int year, int expectedMonthCount) throws Exception {
        var body = objectMapper.readTree(lastResponse.getBody());
        var years = body.get("years");
        assertThat(years).isNotNull();
        boolean found = false;
        for (var yearNode : years) {
            if (yearNode.get("year").asInt() == year) {
                found = true;
                assertThat(yearNode.get("months").size()).isEqualTo(expectedMonthCount);
                break;
            }
        }
        assertThat(found).as("Year %d not in summary", year).isTrue();
    }

    @Then("the response body contains integer {int}")
    public void theResponseBodyContainsInteger(int expected) {
        assertThat(Integer.parseInt(lastResponse.getBody().trim())).isEqualTo(expected);
    }
}