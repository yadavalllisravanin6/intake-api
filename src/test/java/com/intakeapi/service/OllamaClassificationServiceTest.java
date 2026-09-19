package com.intakeapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaClassificationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OllamaClassificationService classificationService;

    @Test
    void parsesValidOllamaResponseCorrectly() {
        ReflectionTestUtils.setField(classificationService, "baseUrl", "http://localhost:11434");
        ReflectionTestUtils.setField(classificationService, "model", "llama3.2:1b");

        // Simulate exactly what Ollama's /api/generate wraps its answer in
        String fakeOllamaResponse = "{\"response\": \"{\\\"department\\\": \\\"Cardiology\\\", \\\"urgency\\\": \\\"HIGH\\\"}\"}";

        when(restTemplate.postForObject(eq("http://localhost:11434/api/generate"), any(), eq(String.class)))
                .thenReturn(fakeOllamaResponse);

        ClassificationResult result = classificationService.classify("chest pain radiating to jaw");

        assertEquals("Cardiology", result.getDepartment());
        assertEquals("HIGH", result.getUrgency());
    }

    @Test
    void fallsBackSafelyWhenResponseIsMalformed() {
        ReflectionTestUtils.setField(classificationService, "baseUrl", "http://localhost:11434");
        ReflectionTestUtils.setField(classificationService, "model", "llama3.2:1b");

        // Simulate a broken/unparseable response from the model
        when(restTemplate.postForObject(any(String.class), any(), eq(String.class)))
                .thenReturn("not valid json at all");

        ClassificationResult result = classificationService.classify("some symptom");

        // Should not throw - should fall back to the safe default
        assertEquals("GeneralMedicine", result.getDepartment());
        assertEquals("MEDIUM", result.getUrgency());
    }
}