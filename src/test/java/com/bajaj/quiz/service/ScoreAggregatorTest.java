package com.bajaj.quiz.service;

import com.bajaj.quiz.model.LeaderboardEntry;
import com.bajaj.quiz.model.QuizEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreAggregatorTest {

    @Test
    void sumsScoresPerParticipant() {
        ScoreAggregator aggregator = new ScoreAggregator();

        aggregator.add("Alice", 10);
        aggregator.add("Alice", 25);
        aggregator.add("Bob", 30);

        assertEquals(35, aggregator.getTotal("Alice"));
        assertEquals(30, aggregator.getTotal("Bob"));
        assertEquals(0, aggregator.getTotal("Unknown"));
        assertEquals(2, aggregator.participantCount());
        assertEquals(65, aggregator.grandTotal());
    }

    @Test
    void addAllAggregatesQuizEvents() {
        ScoreAggregator aggregator = new ScoreAggregator();

        aggregator.addAll(List.of(
                new QuizEvent("R1", "Alice", 10),
                new QuizEvent("R2", "Alice", 15),
                new QuizEvent("R1", "Bob", 20)
        ));

        assertEquals(25, aggregator.getTotal("Alice"));
        assertEquals(20, aggregator.getTotal("Bob"));
        assertEquals(45, aggregator.grandTotal());
    }

    @Test
    void buildLeaderboardSortsByTotalScoreDescending() {
        ScoreAggregator aggregator = new ScoreAggregator();
        aggregator.add("Alice", 100);
        aggregator.add("Charlie", 85);
        aggregator.add("Bob", 120);

        List<LeaderboardEntry> leaderboard = aggregator.buildLeaderboard();

        assertEquals("Bob", leaderboard.get(0).getParticipant());
        assertEquals(120, leaderboard.get(0).getTotalScore());
        assertEquals("Alice", leaderboard.get(1).getParticipant());
        assertEquals("Charlie", leaderboard.get(2).getParticipant());
    }

    @Test
    void emptyAggregatorBuildsEmptyLeaderboard() {
        ScoreAggregator aggregator = new ScoreAggregator();

        assertTrue(aggregator.buildLeaderboard().isEmpty());
        assertEquals(0, aggregator.participantCount());
        assertEquals(0, aggregator.grandTotal());
    }
}
