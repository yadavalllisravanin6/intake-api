package com.intakeapi.service;

// Simple holder for what the LLM gives back
public class ClassificationResult {
    private String department;
    private String urgency;

    public ClassificationResult() {}

    public ClassificationResult(String department, String urgency) {
        this.department = department;
        this.urgency = urgency;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }
}