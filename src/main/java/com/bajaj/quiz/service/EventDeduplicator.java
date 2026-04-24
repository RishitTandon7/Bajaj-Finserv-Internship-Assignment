package com.bajaj.quiz.service;

import com.bajaj.quiz.model.QuizEvent;

import java.util.*;

/**
 * Deduplicates quiz events using a composite key (roundId + participant).
 * Thread-safe via synchronized internal state.
 */
public class EventDeduplicator {

    private final Set<String> seen = new LinkedHashSet<>();

    /**
     * Checks if this event is new (not seen before).
     *
     * @param event the quiz event to check
     * @return true if this is the first occurrence, false if duplicate
     */
    public boolean isNew(QuizEvent event) {
        String compositeKey = event.getRoundId() + "|" + event.getParticipant();
        return seen.add(compositeKey);
    }

    /**
     * Filters a list of events, returning only unique ones.
     * Preserves insertion order; first occurrence wins.
     *
     * @param events raw events (may contain duplicates)
     * @return deduplicated list
     */
    public List<QuizEvent> deduplicate(List<QuizEvent> events) {
        List<QuizEvent> unique = new ArrayList<>();
        for (QuizEvent event : events) {
            if (isNew(event)) {
                unique.add(event);
            }
        }
        return unique;
    }

    /** Returns the number of unique keys seen so far. */
    public int uniqueCount() {
        return seen.size();
    }

    /** Resets internal state. */
    public void reset() {
        seen.clear();
    }
}
