package com.smartjob.service;

import com.smartjob.repository.CandidateRepository;
import com.smartjob.repository.JobRepository;
import org.springframework.stereotype.Service;

/**
 * Spring-aware MatchingService adapter wrapping the core matching algorithms.
 * Fulfills Section 18 of V2 specification.
 */
@Service
public class MatchingService extends MatchingServiceV2 {

    public MatchingService(CandidateRepository candidateRepository, JobRepository jobRepository) {
        super(candidateRepository, jobRepository);
    }
}
