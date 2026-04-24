package com.bajaj.quiz.service;

import com.bajaj.quiz.model.QuizEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventDeduplicatorTest {

    @Test
    void rejectsDuplicateRoundAndParticipantPair() {
        EventDeduplicator deduplicator = new EventDeduplicator();

        assertTrue(deduplicator.isNew(new QuizEvent("R1", "Alice", 10)));
        assertFalse(deduplicator.isNew(new QuizEvent("R1", "Alice", 99)));
        assertEquals(1, deduplicator.uniqueCount());
    }

    @Test
    void keepsSameParticipantInDifferentRoundsAndDifferentParticipantsInSameRound() {
        EventDeduplicator deduplicator = new EventDeduplicator();

        assertTrue(deduplicator.isNew(new QuizEvent("R1", "Alice", 10)));
        assertTrue(deduplicator.isNew(new QuizEvent("R2", "Alice", 15)));
        assertTrue(deduplicator.isNew(new QuizEvent("R1", "Bob", 20)));
        assertEquals(3, deduplicator.uniqueCount());
    }

    @Test
    void deduplicatePreservesFirstOccurrenceOrder() {
        EventDeduplicator deduplicator = new EventDeduplicator();
        QuizEvent first = new QuizEvent("R1", "Alice", 10);
        QuizEvent second = new QuizEvent("R2", "Bob", 20);
        QuizEvent duplicate = new QuizEvent("R1", "Alice", 99);

        List<QuizEvent> unique = deduplicator.deduplicate(List.of(first, second, duplicate));

        assertEquals(2, unique.size());
        assertSame(first, unique.get(0));
        assertSame(second, unique.get(1));
    }

    @Test
    void resetClearsSeenKeys() {
        EventDeduplicator deduplicator = new EventDeduplicator();
        QuizEvent event = new QuizEvent("R1", "Alice", 10);

        assertTrue(deduplicator.isNew(event));
        deduplicator.reset();

        assertEquals(0, deduplicator.uniqueCount());
        assertTrue(deduplicator.isNew(event));
    }
}
