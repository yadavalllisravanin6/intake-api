package com.intakeapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateIntakeNoteRequest {
    @NotBlank(message = "patientId is required")
    private String patientId;

    @NotNull(message = "patientAge is required")
    @Min(value = 0, message = "patientAge must be >= 0")
    private Integer patientAge;

    @NotBlank(message = "symptomText is required")
    private String symptomText;

    // getters and setters
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public Integer getPatientAge() { return patientAge; }
    public void setPatientAge(Integer patientAge) { this.patientAge = patientAge; }
    public String getSymptomText() { return symptomText; }
    public void setSymptomText(String symptomText) { this.symptomText = symptomText; }
}