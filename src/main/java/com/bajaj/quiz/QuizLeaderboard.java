package com.bajaj.quiz;

import com.bajaj.quiz.model.LeaderboardEntry;
import com.bajaj.quiz.model.QuizEvent;
import com.bajaj.quiz.model.SubmitResponse;
import com.bajaj.quiz.service.EventDeduplicator;
import com.bajaj.quiz.service.QuizApiClient;
import com.bajaj.quiz.service.ScoreAggregator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Quiz Leaderboard System — Bajaj Finserv Health Java Qualifier
 *
 * Architecture:
 *   QuizLeaderboard (orchestrator)
 *   ├── QuizApiClient      — HTTP polling + submission with retry
 *   ├── EventDeduplicator   — composite key (roundId|participant)
 *   └── ScoreAggregator     — per-participant totals + leaderboard builder
 */
public class QuizLeaderboard {

    private static final int POLL_COUNT = 10;
    private static final int POLL_DELAY_SECONDS = 5;

    private final QuizApiClient apiClient;
    private final EventDeduplicator deduplicator;
    private final ScoreAggregator aggregator;

    public QuizLeaderboard(String regNo) {
        this.apiClient = new QuizApiClient(regNo);
        this.deduplicator = new EventDeduplicator();
        this.aggregator = new ScoreAggregator();
    }

    public static void main(String[] args) {
        // ── CLI Validation ──────────────────────────────────────
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

        new QuizLeaderboard(regNo).run(regNo);
    }

    private void run(String regNo) {
        Instant startTime = Instant.now();

        // ── Header ──────────────────────────────────────────────
        line('=');
        System.out.println("  QUIZ LEADERBOARD SYSTEM");
        System.out.println("  Bajaj Finserv Health - Java Qualifier (SRM)");
        line('=');
        System.out.println();
        System.out.println("  Registration No : " + regNo);
        System.out.println("  API Endpoint    : " + apiClient.getBaseUrl());
        System.out.println("  Poll Count      : " + POLL_COUNT + " (5s interval)");
        System.out.println("  Dedup Key       : roundId + participant");
        System.out.println();
        line('-');

        try {
            // ── STEP 1: Poll ────────────────────────────────────
            System.out.println();
            System.out.println("  [STEP 1/5] Polling API...");
            System.out.println();

            List<QuizEvent> allEvents = pollAll();

            if (allEvents.isEmpty()) {
                System.err.println("  ERROR: No events collected. The API server may be down.");
                System.err.println("  Please try again later.");
                System.exit(1);
            }

            // ── STEP 2: Collect ─────────────────────────────────
            System.out.println();
            line('-');
            System.out.println();
            System.out.println("  [STEP 2/5] Collected " + allEvents.size() + " raw events");

            // ── STEP 3: Deduplicate ─────────────────────────────
            List<QuizEvent> uniqueEvents = deduplicator.deduplicate(allEvents);
            int dupes = allEvents.size() - uniqueEvents.size();
            System.out.println("  [STEP 3/5] Deduplicated: " + uniqueEvents.size()
                    + " unique, " + dupes + " duplicates removed");

            // ── STEP 4: Aggregate ───────────────────────────────
            aggregator.addAll(uniqueEvents);
            List<LeaderboardEntry> leaderboard = aggregator.buildLeaderboard();
            System.out.println("  [STEP 4/5] Aggregated scores for "
                    + aggregator.participantCount() + " participants");

            // ── Leaderboard ─────────────────────────────────────
            System.out.println();
            line('=');
            System.out.println("  FINAL LEADERBOARD");
            line('=');
            System.out.println();
            System.out.printf("  %-6s %-20s %12s%n", "RANK", "PARTICIPANT", "TOTAL SCORE");
            line('-');

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

            line('-');
            System.out.printf("  %-6s %-20s %12d%n", "", "GRAND TOTAL", aggregator.grandTotal());
            System.out.println();

            // ── STEP 5: Submit ──────────────────────────────────
            System.out.println("  [STEP 5/5] Submitting leaderboard...");
            System.out.println();
            line('-');
            System.out.println("  POST " + apiClient.getBaseUrl() + "/quiz/submit");

            SubmitResponse result = apiClient.submit(leaderboard);

            line('-');
            System.out.println();
            System.out.println("  STATUS  : 200 - ACCEPTED");
            System.out.println("  RegNo   : " + result.getRegNo());
            System.out.println("  Polls   : " + result.getTotalPollsMade());
            System.out.println("  Total   : " + result.getSubmittedTotal());
            System.out.println("  Attempt : " + result.getAttemptCount());
            if (result.getIsCorrect() != null) {
                System.out.println("  Correct : " + result.getIsCorrect());
            }
            if (result.getMessage() != null) {
                System.out.println("  Message : " + result.getMessage());
            }

            // ── Summary ─────────────────────────────────────────
            long elapsed = Duration.between(startTime, Instant.now()).getSeconds();
            System.out.println();
            line('=');
            System.out.println("  EXECUTION SUMMARY");
            line('=');
            System.out.println();
            System.out.println("  Registration     : " + regNo);
            System.out.println("  Raw Events       : " + allEvents.size());
            System.out.println("  Duplicates       : " + dupes);
            System.out.println("  Unique Events    : " + uniqueEvents.size());
            System.out.println("  Participants     : " + aggregator.participantCount());
            System.out.println("  Grand Total      : " + aggregator.grandTotal());
            System.out.println("  Elapsed Time     : " + elapsed + "s");
            System.out.println("  Status           : COMPLETE");
            System.out.println();
            line('=');

        } catch (Exception e) {
            System.err.println();
            System.err.println("  FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Polls the API 10 times with mandatory 5-second delays.
     * Shows a progress bar and timestamps for each poll.
     */
    private List<QuizEvent> pollAll() throws Exception {
        List<QuizEvent> allEvents = new ArrayList<>();

        for (int poll = 0; poll < POLL_COUNT; poll++) {
            if (poll > 0) {
                for (int s = POLL_DELAY_SECONDS; s > 0; s--) {
                    System.out.printf("\r  Waiting %ds...   ", s);
                    Thread.sleep(1000L);
                }
                System.out.print("\r                  \r");
            }

            // Progress bar
            int done = poll + 1;
            int barLen = 20;
            int filled = (done * barLen) / POLL_COUNT;
            String bar = "=".repeat(filled) + " ".repeat(barLen - filled);

            System.out.printf("  [%s] Poll %d/9  %d/%d  ", QuizApiClient.timestamp(), poll, done, POLL_COUNT);

            List<QuizEvent> events = apiClient.poll(poll);

            if (events.isEmpty()) {
                System.out.println("FAIL");
            } else {
                System.out.printf("OK  +%d event(s)%n", events.size());
                allEvents.addAll(events);
            }
        }

        return allEvents;
    }

    /** Prints a horizontal line. */
    private static void line(char ch) {
        System.out.println(String.valueOf(ch).repeat(60));
    }
}
