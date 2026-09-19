package com.intakeapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Profile("cloud")
public class GroqClassificationService implements ClassificationService {

    private final RestTemplate restTemplate;

    @Value("${groq.api-key}")
    private String apiKey;

    @Value("${groq.model}")
    private String model;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    public GroqClassificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
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
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "response_format", Map.of("type", "json_object")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            String rawResponse = restTemplate.postForObject(GROQ_URL, request, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawResponse);
            String content = root.at("/choices/0/message/content").asText();
            return mapper.readValue(content, ClassificationResult.class);
        } catch (Exception e) {
            return new ClassificationResult("GeneralMedicine", "MEDIUM");
        }
    }
}