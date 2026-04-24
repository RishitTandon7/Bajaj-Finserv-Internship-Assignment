package com.bajaj.quiz;

import com.bajaj.quiz.model.LeaderboardEntry;
import com.bajaj.quiz.model.PollResponse;
import com.bajaj.quiz.model.QuizEvent;
import com.bajaj.quiz.model.SubmitRequest;
import com.bajaj.quiz.model.SubmitResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Quiz Leaderboard System — Bajaj Finserv Health Java Qualifier
 *
 * Polls the quiz validator API 10 times (with 5-second delays),
 * deduplicates events by (roundId + participant), aggregates scores,
 * sorts descending by totalScore, and submits exactly once.
 */
public class QuizLeaderboard {

    private static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    private static final int POLL_COUNT = 10;
    private static final int POLL_DELAY_SECONDS = 5;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_BASE_DELAY_SECONDS = 3;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String regNo;

    public QuizLeaderboard(String regNo) {
        this.regNo = regNo;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar quiz-leaderboard.jar <regNo>");
            System.err.println("Example: java -jar quiz-leaderboard.jar RA2311003010587");
            System.exit(1);
        }

        String regNo = args[0].trim();
        if (regNo.isEmpty()) {
            System.err.println("Error: Registration number cannot be blank.");
            System.exit(1);
        }

        Instant startTime = Instant.now();

        // ── Header ──────────────────────────────────────────────
        printLine('=', 60);
        System.out.println("  QUIZ LEADERBOARD SYSTEM");
        System.out.println("  Bajaj Finserv Health - Java Qualifier (SRM)");
        printLine('=', 60);
        System.out.println();
        System.out.println("  Registration No : " + regNo);
        System.out.println("  API Endpoint    : " + BASE_URL);
        System.out.println("  Poll Count      : " + POLL_COUNT + " (5s interval)");
        System.out.println("  Dedup Key       : roundId + participant");
        System.out.println();
        printLine('-', 60);

        QuizLeaderboard app = new QuizLeaderboard(regNo);

        try {
            // ── STEP 1: Poll API ────────────────────────────────
            System.out.println();
            System.out.println("  [STEP 1/5] Polling API...");
            System.out.println();

            List<QuizEvent> allEvents = app.pollAllEvents();

            if (allEvents.isEmpty()) {
                System.err.println();
                System.err.println("  ERROR: No events collected. The API server may be down.");
                System.err.println("  Please try again later.");
                System.exit(1);
            }

            // ── STEP 2: Collect ─────────────────────────────────
            System.out.println();
            printLine('-', 60);
            System.out.println();
            System.out.println("  [STEP 2/5] Collected " + allEvents.size() + " raw events");

            // ── STEP 3: Deduplicate ─────────────────────────────
            List<QuizEvent> uniqueEvents = app.deduplicateEvents(allEvents);
            int duplicates = allEvents.size() - uniqueEvents.size();
            System.out.println("  [STEP 3/5] Deduplicated: " + uniqueEvents.size()
                    + " unique, " + duplicates + " duplicates removed");

            // ── STEP 4: Aggregate ───────────────────────────────
            List<LeaderboardEntry> leaderboard = app.aggregateScores(uniqueEvents);
            System.out.println("  [STEP 4/5] Aggregated scores for "
                    + leaderboard.size() + " participants");

            // ── Display Leaderboard ─────────────────────────────
            System.out.println();
            printLine('=', 60);
            System.out.println("  FINAL LEADERBOARD");
            printLine('=', 60);
            System.out.println();
            System.out.printf("  %-6s %-20s %12s%n", "RANK", "PARTICIPANT", "TOTAL SCORE");
            printLine('-', 60);

            for (int i = 0; i < leaderboard.size(); i++) {
                LeaderboardEntry e = leaderboard.get(i);
                String medal = switch (i) {
                    case 0 -> " [1st]";
                    case 1 -> " [2nd]";
                    case 2 -> " [3rd]";
                    default -> "";
                };
                System.out.printf("  %-6s %-20s %12d%s%n",
                        "#" + (i + 1), e.getParticipant(), e.getTotalScore(), medal);
            }

            printLine('-', 60);
            int grandTotal = leaderboard.stream().mapToInt(LeaderboardEntry::getTotalScore).sum();
            System.out.printf("  %-6s %-20s %12d%n", "", "GRAND TOTAL", grandTotal);
            System.out.println();

            // ── STEP 5: Submit ──────────────────────────────────
            System.out.println("  [STEP 5/5] Submitting leaderboard...");
            app.submitLeaderboard(leaderboard);

            // ── Summary ─────────────────────────────────────────
            long elapsed = Duration.between(startTime, Instant.now()).getSeconds();
            System.out.println();
            printLine('=', 60);
            System.out.println("  EXECUTION SUMMARY");
            printLine('=', 60);
            System.out.println();
            System.out.println("  Registration     : " + regNo);
            System.out.println("  Raw Events       : " + allEvents.size());
            System.out.println("  Duplicates       : " + duplicates);
            System.out.println("  Unique Events    : " + uniqueEvents.size());
            System.out.println("  Participants     : " + leaderboard.size());
            System.out.println("  Grand Total      : " + grandTotal);
            System.out.println("  Elapsed Time     : " + elapsed + "s");
            System.out.println("  Status           : COMPLETE");
            System.out.println();
            printLine('=', 60);

        } catch (Exception e) {
            System.err.println();
            System.err.println("  FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /** Prints a horizontal line of the given character and length. */
    private static void printLine(char ch, int length) {
        System.out.println(String.valueOf(ch).repeat(length));
    }

    /**
     * Sends an HTTP request with retry logic for 5xx server errors.
     * Retries up to MAX_RETRIES times with exponential backoff.
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

    /**
     * Polls the quiz API 10 times (index 0-9) with a mandatory
     * 5-second delay between each request. Retries on server errors.
     */
    private List<QuizEvent> pollAllEvents() throws IOException, InterruptedException {
        List<QuizEvent> allEvents = new ArrayList<>();

        for (int poll = 0; poll < POLL_COUNT; poll++) {
            if (poll > 0) {
                for (int s = POLL_DELAY_SECONDS; s > 0; s--) {
                    System.out.printf("\r  Waiting %ds...   ", s);
                    Thread.sleep(1000L);
                }
                System.out.print("\r                  \r");
            }

            // Progress bar: [=====     ] 5/10
            int done = poll + 1;
            int barLen = 20;
            int filled = (done * barLen) / POLL_COUNT;
            String bar = "=".repeat(filled) + " ".repeat(barLen - filled);
            System.out.printf("  Poll %d/9  [%s] %d/%d  ", poll, bar, done, POLL_COUNT);

            String encodedRegNo = URLEncoder.encode(regNo, StandardCharsets.UTF_8);
            String url = BASE_URL + "/quiz/messages?regNo=" + encodedRegNo + "&poll=" + poll;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = sendWithRetry(request);

            if (response.statusCode() != 200) {
                System.out.printf("FAIL (%d)%n", response.statusCode());
                continue;
            }

            PollResponse pollResponse = objectMapper.readValue(response.body(), PollResponse.class);
            List<QuizEvent> events = pollResponse.getEvents();
            int count = (events != null) ? events.size() : 0;
            System.out.printf("OK  +%d event(s)%n", count);

            if (events != null) {
                allEvents.addAll(events);
            }
        }

        return allEvents;
    }

    /**
     * Deduplicates events using the composite key: roundId + participant.
     * Only the first occurrence of each unique key is kept.
     */
    private List<QuizEvent> deduplicateEvents(List<QuizEvent> events) {
        Set<String> seen = new LinkedHashSet<>();
        List<QuizEvent> unique = new ArrayList<>();

        for (QuizEvent event : events) {
            String compositeKey = event.getRoundId() + "|" + event.getParticipant();
            if (seen.add(compositeKey)) {
                unique.add(event);
            }
        }

        return unique;
    }

    /**
     * Aggregates scores per participant and returns the leaderboard
     * sorted in descending order by totalScore.
     */
    private List<LeaderboardEntry> aggregateScores(List<QuizEvent> uniqueEvents) {
        Map<String, Integer> scoreMap = new LinkedHashMap<>();

        for (QuizEvent event : uniqueEvents) {
            scoreMap.merge(event.getParticipant(), event.getScore(), Integer::sum);
        }

        return scoreMap.entrySet().stream()
                .map(e -> new LeaderboardEntry(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingInt(LeaderboardEntry::getTotalScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Submits the final leaderboard via POST /quiz/submit — exactly once.
     */
    private void submitLeaderboard(List<LeaderboardEntry> leaderboard)
            throws IOException, InterruptedException {

        SubmitRequest payload = new SubmitRequest(regNo, leaderboard);
        String jsonBody = objectMapper.writeValueAsString(payload);

        System.out.println();
        printLine('-', 60);
        System.out.println("  POST " + BASE_URL + "/quiz/submit");
        System.out.println("  Payload: " + jsonBody);
        printLine('-', 60);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/quiz/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println();
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            SubmitResponse result = objectMapper.readValue(response.body(), SubmitResponse.class);
            System.out.println("  STATUS  : " + response.statusCode() + " - ACCEPTED");
            System.out.println("  RegNo   : " + result.getRegNo());
            System.out.println("  Polls   : " + result.getTotalPollsMade());
            System.out.println("  Total   : " + result.getSubmittedTotal());
            System.out.println("  Attempt : " + result.getAttemptCount());
            if (result.getIsCorrect() != null) {
                System.out.println("  Correct : " + result.getIsCorrect());
            }
            if (result.getExpectedTotal() != null) {
                System.out.println("  Expected: " + result.getExpectedTotal());
            }
            if (result.getMessage() != null) {
                System.out.println("  Message : " + result.getMessage());
            }
        } else {
            System.err.println("  STATUS  : " + response.statusCode() + " - FAILED");
            System.err.println("  Response: " + response.body());
        }
    }
}
