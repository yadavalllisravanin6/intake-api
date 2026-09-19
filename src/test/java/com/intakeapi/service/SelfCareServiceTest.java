package com.intakeapi.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SelfCareServiceTest {

    private final SelfCareService selfCareService = new SelfCareService();

    @Test
    void returnsNoteForLowUrgency() {
        String note = selfCareService.getNoteForUrgency("LOW");
        assertNotNull(note, "LOW urgency should return a self-care note");
        assertTrue(note.contains("seek medical care"),
                "Note should still point back to professional care");
    }

    @Test
    void returnsNullForMediumUrgency() {
        assertNull(selfCareService.getNoteForUrgency("MEDIUM"),
                "MEDIUM urgency must never get a self-care note");
    }

    @Test
    void returnsNullForHighUrgency() {
        assertNull(selfCareService.getNoteForUrgency("HIGH"),
                "HIGH urgency must never get a self-care note");
    }

    @Test
    void returnsNullForCriticalUrgency() {
        assertNull(selfCareService.getNoteForUrgency("CRITICAL"),
                "CRITICAL urgency must never get a self-care note");
    }

    @Test
    void returnsNullForUnexpectedValue() {
        // defensive: if the LLM ever returns something outside the expected
        // vocabulary, we should fail safe (no remedy shown) rather than guess
        assertNull(selfCareService.getNoteForUrgency("banana"));
    }
}