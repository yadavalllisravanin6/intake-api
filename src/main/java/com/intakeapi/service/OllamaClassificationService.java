package com.intakeapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OllamaClassificationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.model}")
    private String model;

    public ClassificationResult classify(String symptomText) {
        String prompt = """
            You are a medical intake triage assistant.
            Given the patient symptom description below, respond with ONLY
            a JSON object, no other text, in exactly this format:
            {"department": "...", "urgency": "..."}

            department must be one of: Cardiology, Emergency, GeneralMedicine,
            Dermatology, Orthopedics, ENT, Psychiatry, Pediatrics, Ophthalmology

            urgency must be one of: LOW, MEDIUM, HIGH, CRITICAL

            Symptom description: "%s"
            """.formatted(symptomText);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "prompt", prompt,
                "format", "json",
                "stream", false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String rawResponse = restTemplate.postForObject(
                baseUrl + "/api/generate", request, String.class);

        try {
            // Ollama wraps the model's output inside a "response" field as a string
            JsonNode outer = objectMapper.readTree(rawResponse);
            String innerJson = outer.get("response").asText();
            return objectMapper.readValue(innerJson, ClassificationResult.class);
        } catch (Exception e) {
            // If parsing fails, return a safe fallback instead of crashing the request
            return new ClassificationResult("GeneralMedicine", "MEDIUM");
        }
    }
}