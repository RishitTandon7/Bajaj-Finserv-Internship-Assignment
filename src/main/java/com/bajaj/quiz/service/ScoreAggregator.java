package com.bajaj.quiz.service;

import com.bajaj.quiz.model.LeaderboardEntry;
import com.bajaj.quiz.model.QuizEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregates quiz event scores per participant.
 */
public class ScoreAggregator {

    private final Map<String, Integer> scoreMap = new LinkedHashMap<>();

    /**
     * Adds a score for a participant.
     *
     * @param participant participant name
     * @param score       score to add
     */
    public void add(String participant, int score) {
        scoreMap.merge(participant, score, Integer::sum);
    }

    /**
     * Adds all events' scores.
     *
     * @param events deduplicated quiz events
     */
    public void addAll(List<QuizEvent> events) {
        for (QuizEvent event : events) {
            add(event.getParticipant(), event.getScore());
        }
    }

    /**
     * Returns the total score for a participant.
     *
     * @param participant participant name
     * @return total score, or 0 if unknown
     */
    public int getTotal(String participant) {
        return scoreMap.getOrDefault(participant, 0);
    }

    /**
     * Builds the leaderboard sorted descending by totalScore.
     *
     * @return sorted leaderboard entries
     */
    public List<LeaderboardEntry> buildLeaderboard() {
        return scoreMap.entrySet().stream()
                .map(e -> new LeaderboardEntry(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingInt(LeaderboardEntry::getTotalScore).reversed())
                .collect(Collectors.toList());
    }

    /** Returns the number of unique participants. */
    public int participantCount() {
        return scoreMap.size();
    }

    /** Returns the grand total of all scores. */
    public int grandTotal() {
        return scoreMap.values().stream().mapToInt(Integer::intValue).sum();
    }
}
