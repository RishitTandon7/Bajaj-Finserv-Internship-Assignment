package com.bajaj.quiz.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the response from POST /quiz/submit.
 * Handles both response formats from the API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmitResponse {

    private String regNo;
    private int totalPollsMade;
    private int submittedTotal;
    private int attemptCount;

    @JsonProperty("isCorrect")
    private Boolean isCorrect;

    @JsonProperty("isIdempotent")
    private Boolean isIdempotent;

    private Integer expectedTotal;
    private String message;

    // Default constructor for Jackson
    public SubmitResponse() {}

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public int getTotalPollsMade() {
        return totalPollsMade;
    }

    public void setTotalPollsMade(int totalPollsMade) {
        this.totalPollsMade = totalPollsMade;
    }

    public int getSubmittedTotal() {
        return submittedTotal;
    }

    public void setSubmittedTotal(int submittedTotal) {
        this.submittedTotal = submittedTotal;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public Boolean getIsIdempotent() {
        return isIdempotent;
    }

    public void setIsIdempotent(Boolean isIdempotent) {
        this.isIdempotent = isIdempotent;
    }

    public Integer getExpectedTotal() {
        return expectedTotal;
    }

    public void setExpectedTotal(Integer expectedTotal) {
        this.expectedTotal = expectedTotal;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "SubmitResponse{regNo='" + regNo + "', totalPollsMade=" + totalPollsMade +
                ", submittedTotal=" + submittedTotal + ", attemptCount=" + attemptCount +
                ", message='" + message + "'}";
    }
}
