package com.bajaj.quiz.model;

/**
 * Represents a single entry in the final leaderboard.
 * Contains the participant name and their aggregated total score.
 */
public class LeaderboardEntry {

    private String participant;
    private int totalScore;

    // Default constructor for Jackson
    public LeaderboardEntry() {}

    public LeaderboardEntry(String participant, int totalScore) {
        this.participant = participant;
        this.totalScore = totalScore;
    }

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    @Override
    public String toString() {
        return "LeaderboardEntry{participant='" + participant + "', totalScore=" + totalScore + '}';
    }
}
