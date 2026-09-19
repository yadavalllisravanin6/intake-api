package com.intakeapi.service;

public interface ClassificationService {
    ClassificationResult classify(String symptomText);
}