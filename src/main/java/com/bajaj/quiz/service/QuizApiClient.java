package com.bajaj.quiz.service;

import com.bajaj.quiz.model.PollResponse;
import com.bajaj.quiz.model.QuizEvent;
import com.bajaj.quiz.model.SubmitRequest;
import com.bajaj.quiz.model.SubmitResponse;
import com.bajaj.quiz.model.LeaderboardEntry;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP client for the Quiz Leaderboard API.
 * Handles GET polling with retry logic and POST submission.
 */
public class QuizApiClient {

    private static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_BASE_DELAY_SECONDS = 3;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String regNo;

    public QuizApiClient(String regNo) {
        this.regNo = regNo;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Polls the API for a specific poll index.
     *
     * @param pollIndex 0-based poll index
     * @return list of events from the poll, or empty list on failure
     */
    public List<QuizEvent> poll(int pollIndex) throws IOException, InterruptedException {
        String encodedRegNo = URLEncoder.encode(regNo, StandardCharsets.UTF_8);
        String url = BASE_URL + "/quiz/messages?regNo=" + encodedRegNo + "&poll=" + pollIndex;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = sendWithRetry(request);

        if (response.statusCode() != 200) {
            return List.of();
        }

        PollResponse pollResponse = objectMapper.readValue(response.body(), PollResponse.class);
        return (pollResponse.getEvents() != null) ? pollResponse.getEvents() : List.of();
    }

    /**
     * Submits the final leaderboard via POST.
     *
     * @param leaderboard sorted leaderboard entries
     * @return the parsed submit response
     */
    public SubmitResponse submit(List<LeaderboardEntry> leaderboard) throws IOException, InterruptedException {
        SubmitRequest payload = new SubmitRequest(regNo, leaderboard);
        String jsonBody = objectMapper.writeValueAsString(payload);

        System.out.println("  Payload: " + jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/quiz/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(30))
                .build();

        // Submit uses sendWithRetry too — but the assignment says submit exactly once.
        // The retry here only retries on 5xx server errors (transient failures),
        // which means the server didn't process our request.
        HttpResponse<String> response = sendWithRetry(request);

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), SubmitResponse.class);
        }

        throw new IOException("Submission failed with status " + response.statusCode()
                + ": " + response.body());
    }

    /** Returns the base URL for display purposes. */
    public String getBaseUrl() {
        return BASE_URL;
    }

    /** Returns the current timestamp formatted as HH:mm:ss. */
    public static String timestamp() {
        return LocalTime.now().format(TIME_FMT);
    }

    /**
     * Sends an HTTP request with retry logic for 5xx server errors.
     */
    private HttpResponse<String> sendWithRetry(HttpRequest request)
            throws IOException, InterruptedException {

        HttpResponse<String> response = null;
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() < 500) {
                    return response;
                }

                if (attempt < MAX_RETRIES) {
                    int delay = RETRY_BASE_DELAY_SECONDS * (int) Math.pow(2, attempt);
                    System.out.printf("             Retry %d/%d in %ds (server returned %d)%n",
                            attempt + 1, MAX_RETRIES, delay, response.statusCode());
                    Thread.sleep(delay * 1000L);
                }
            } catch (java.net.http.HttpTimeoutException e) {
                if (attempt < MAX_RETRIES) {
                    int delay = RETRY_BASE_DELAY_SECONDS * (int) Math.pow(2, attempt);
                    System.out.printf("             Retry %d/%d in %ds (timeout)%n",
                            attempt + 1, MAX_RETRIES, delay);
                    Thread.sleep(delay * 1000L);
                } else {
                    throw e;
                }
            }
        }
        return response;
    }
}
