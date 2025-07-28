package com.seek.candidatosmanagementapi.service;


import com.seek.candidatosmanagementapi.dto.CandidateResponse;
import com.seek.candidatosmanagementapi.dto.CreateCandidateRequest;
import com.seek.candidatosmanagementapi.dto.MetricsResponse;

import java.util.List;

public interface CandidateService {
    CandidateResponse createCandidate(CreateCandidateRequest request);
    List<CandidateResponse> getAllCandidates();
    MetricsResponse getMetrics();
}