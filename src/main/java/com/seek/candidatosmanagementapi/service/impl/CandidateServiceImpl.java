package com.seek.candidatosmanagementapi.service.impl;


import com.seek.candidatosmanagementapi.dto.CandidateResponse;
import com.seek.candidatosmanagementapi.dto.CreateCandidateRequest;
import com.seek.candidatosmanagementapi.dto.MetricsResponse;
import com.seek.candidatosmanagementapi.entity.Candidate;
import com.seek.candidatosmanagementapi.repository.CandidateRepository;
import com.seek.candidatosmanagementapi.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CandidateServiceImpl implements CandidateService {
    private final CandidateRepository repository;

    @Override
    public CandidateResponse createCandidate(CreateCandidateRequest request) {
        Candidate entity = Candidate.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .age(request.getAge())
                .birthDate(request.getBirthDate())
                .build();
        Candidate saved = repository.save(entity);
        return mapToResponse(saved);
    }

    @Override
    public List<CandidateResponse> getAllCandidates() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MetricsResponse getMetrics() {
        List<Integer> ages = repository.findAll().stream()
                .map(Candidate::getAge)
                .collect(Collectors.toList());
        double avg = ages.stream().mapToInt(Integer::intValue).average().orElse(0);
        double std = Math.sqrt(
                ages.stream()
                        .mapToDouble(a -> Math.pow(a - avg, 2))
                        .average()
                        .orElse(0)
        );
        return MetricsResponse.builder()
                .averageAge(avg)
                .ageStdDeviation(std)
                .build();
    }

    private CandidateResponse mapToResponse(Candidate c) {
        LocalDate today = LocalDate.now();
        LocalDate estDate = c.getBirthDate().plusYears(75);

        LocalDate nextBday = c.getBirthDate().withYear(today.getYear());
        if (nextBday.isBefore(today) || nextBday.isEqual(today)) {
            nextBday = nextBday.plusYears(1);
        }

        long daysToBday = ChronoUnit.DAYS.between(today, nextBday);
        long months = ChronoUnit.MONTHS.between(c.getBirthDate(), today);

        return CandidateResponse.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .age(c.getAge())
                .birthDate(c.getBirthDate())
                .estimatedEventDate(estDate)
                .nextBirthday(nextBday)
                .daysToNextBirthday(daysToBday)
                .ageInMonths(months)
                .build();
    }
}
