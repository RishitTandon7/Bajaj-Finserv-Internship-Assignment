package com.bajaj.quiz.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Represents the JSON response from GET /quiz/messages.
 * Contains the registration number, set ID, poll index, and a list of events.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PollResponse {

    private String regNo;
    private String setId;
    private int pollIndex;
    private List<QuizEvent> events;

    // Default constructor for Jackson
    public PollResponse() {}

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public int getPollIndex() {
        return pollIndex;
    }

    public void setPollIndex(int pollIndex) {
        this.pollIndex = pollIndex;
    }

    public List<QuizEvent> getEvents() {
        return events;
    }

    public void setEvents(List<QuizEvent> events) {
        this.events = events;
    }

    @Override
    public String toString() {
        return "PollResponse{regNo='" + regNo + "', setId='" + setId +
                "', pollIndex=" + pollIndex + ", events=" + (events != null ? events.size() : 0) + '}';
    }
}
