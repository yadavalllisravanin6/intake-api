package com.intakeapi.controller;

import com.intakeapi.dto.CreateIntakeNoteRequest;
import com.intakeapi.model.IntakeNote;
import com.intakeapi.repository.IntakeNoteRepository;
import com.intakeapi.service.OllamaClassificationService;
import com.intakeapi.service.ClassificationResult;
import com.intakeapi.service.SelfCareService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/intake-notes")
public class IntakeNoteController {

    private final IntakeNoteRepository intakeNoteRepository;
    private final OllamaClassificationService classificationService;
    private final SelfCareService selfCareService;

    // Constructor injection - Spring wires all three dependencies in automatically
    public IntakeNoteController(IntakeNoteRepository intakeNoteRepository,
                                OllamaClassificationService classificationService,
                                SelfCareService selfCareService) {
        this.intakeNoteRepository = intakeNoteRepository;
        this.classificationService = classificationService;
        this.selfCareService = selfCareService;
    }

    // POST /intake-notes - create a new note AND classify it automatically.
    // Uses CreateIntakeNoteRequest (not IntakeNote directly) so the API only
    // exposes the fields a client should actually send - not id, status,
    // department, urgency, createdAt, which are all server-controlled.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IntakeNote createIntakeNote(@Valid @RequestBody CreateIntakeNoteRequest request) {
        IntakeNote newNote = new IntakeNote(request.getPatientId(), request.getPatientAge(), request.getSymptomText());

        // Classify immediately based on the symptom text, before saving
        ClassificationResult result = classificationService.classify(newNote.getSymptomText());
        newNote.setDepartment(result.getDepartment());
        newNote.setUrgency(result.getUrgency());

        // Self-care note is generic and ONLY ever populated for LOW urgency -
        // see SelfCareService for why this is deliberately restricted.
        newNote.setSelfCareNote(selfCareService.getNoteForUrgency(result.getUrgency()));

        return intakeNoteRepository.save(newNote);
    }

    // GET /intake-notes - list all intake notes
    @GetMapping
    public List<IntakeNote> getAllIntakeNotes() {
        return intakeNoteRepository.findAll();
    }

    // GET /intake-notes/{id} - get a single intake note
    @GetMapping("/{id}")
    public IntakeNote getIntakeNote(@PathVariable Long id) {
        return intakeNoteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Intake note " + id + " not found"));
    }

    // PUT /intake-notes/{id} - update an existing intake note
    @PutMapping("/{id}")
    public IntakeNote updateIntakeNote(@PathVariable Long id, @Valid @RequestBody IntakeNote updated) {
        IntakeNote existing = intakeNoteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Intake note " + id + " not found"));

        existing.setPatientAge(updated.getPatientAge());
        existing.setPatientId(updated.getPatientId());
        existing.setSymptomText(updated.getSymptomText());
        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }
        return intakeNoteRepository.save(existing);
    }

    // DELETE /intake-notes/{id} - delete an intake note
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIntakeNote(@PathVariable Long id) {
        if (!intakeNoteRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Intake note " + id + " not found");
        }
        intakeNoteRepository.deleteById(id);
    }

    // POST /intake-notes/{id}/classify - runs the local LLM against this note
    @PostMapping("/{id}/classify")
    public IntakeNote classifyIntakeNote(@PathVariable Long id) {
        IntakeNote note = intakeNoteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Intake note " + id + " not found"));

        ClassificationResult result = classificationService.classify(note.getSymptomText());
        note.setDepartment(result.getDepartment());
        note.setUrgency(result.getUrgency());
        note.setSelfCareNote(selfCareService.getNoteForUrgency(result.getUrgency()));

        return intakeNoteRepository.save(note);
    }
}