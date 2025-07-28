package com.seek.candidatosmanagementapi.repository;


import com.seek.candidatosmanagementapi.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
}