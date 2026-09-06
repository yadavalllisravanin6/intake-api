package com.intakeapi.service;

import org.springframework.stereotype.Service;

/**
 * Provides a generic, non-diagnostic self-care note - deliberately NOT
 * matched to specific symptoms, and only ever surfaced for LOW urgency.
 * This is not medical advice and must never be shown for MEDIUM/HIGH/CRITICAL
 * cases, where the priority is directing the person to seek care, not
 * offering home tips.
 */
@Service
public class SelfCareService {

    private static final String GENERIC_NOTE =
            "This appears to be lower urgency based on the description provided. " +
                    "General self-care while you monitor how you feel: rest, stay hydrated, " +
                    "and keep an eye on your symptoms. This is not medical advice - if symptoms " +
                    "worsen, don't improve, or you're ever unsure, seek medical care.";

    public String getNoteForUrgency(String urgency) {
        if ("LOW".equals(urgency)) {
            return GENERIC_NOTE;
        }
        return null;
    }
}