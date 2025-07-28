package com.seek.candidatosmanagementapi.controller;



import com.seek.candidatosmanagementapi.dto.CandidateResponse;
import com.seek.candidatosmanagementapi.dto.CreateCandidateRequest;
import com.seek.candidatosmanagementapi.dto.MetricsResponse;
import com.seek.candidatosmanagementapi.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("candidatos")
@RequiredArgsConstructor
@Validated
public class CandidateController {
    private final CandidateService service;

    @Operation(summary = "Create a new candidate")
    @PostMapping
    public ResponseEntity<CandidateResponse> create(@Valid @RequestBody CreateCandidateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCandidate(request));
    }

    @Operation(summary = "Get all candidates")
    @GetMapping
    public ResponseEntity<List<CandidateResponse>> getAll() {
        return ResponseEntity.ok(service.getAllCandidates());
    }

    @Operation(summary = "Get candidate age metrics")
    @GetMapping("/metrics")
    public ResponseEntity<MetricsResponse> getMetrics() {
        return ResponseEntity.ok(service.getMetrics());
    }
}