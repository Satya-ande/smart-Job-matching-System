package com.smartjob.service;

import com.smartjob.model.Candidate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service managing candidate profiles and skills with O(1) in-memory HashMap lookups.
 */
public class CandidateService {

    // In-memory candidate storage indexed by ID: O(1) lookup time complexity
    private final Map<String, Candidate> candidateMap;

    public CandidateService() {
        this.candidateMap = new LinkedHashMap<>();
    }

    public CandidateService(List<Candidate> initialCandidates) {
        this.candidateMap = new LinkedHashMap<>();
        if (initialCandidates != null) {
            for (Candidate c : initialCandidates) {
                if (c != null && c.getId() != null) {
                    this.candidateMap.put(c.getId(), c);
                }
            }
        }
    }

    /**
     * Retrieves a candidate by unique ID in O(1) time.
     */
    public Candidate getCandidateById(String id) {
        if (id == null) return null;
        return candidateMap.get(id.trim());
    }

    /**
     * Returns a list of all candidates.
     */
    public List<Candidate> getAllCandidates() {
        return new ArrayList<>(candidateMap.values());
    }

    /**
     * Checks if a candidate exists.
     */
    public boolean exists(String id) {
        return id != null && candidateMap.containsKey(id.trim());
    }

    /**
     * Adds or registers a candidate. Auto-generates ID (e.g. C001) if not provided.
     */
    public Candidate addCandidate(Candidate candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("Candidate cannot be null");
        }
        if (candidate.getId() == null || candidate.getId().trim().isEmpty()) {
            candidate.setId(generateNextCandidateId());
        }
        candidateMap.put(candidate.getId(), candidate);
        return candidate;
    }

    /**
     * Updates an existing candidate profile.
     */
    public boolean updateCandidate(Candidate candidate) {
        if (candidate == null || candidate.getId() == null) {
            return false;
        }
        if (!candidateMap.containsKey(candidate.getId())) {
            return false;
        }
        candidateMap.put(candidate.getId(), candidate);
        return true;
    }

    /**
     * Deletes a candidate by ID.
     */
    public boolean deleteCandidate(String id) {
        if (id == null) return false;
        return candidateMap.remove(id.trim()) != null;
    }

    /**
     * Adds a skill to candidate profile.
     *
     * @return true if added, false if already exists or candidate not found
     */
    public boolean addSkill(String candidateId, String skill) {
        Candidate candidate = getCandidateById(candidateId);
        if (candidate == null || skill == null || skill.trim().isEmpty()) {
            return false;
        }
        return candidate.addSkill(skill.trim());
    }

    /**
     * Removes a skill from candidate profile.
     *
     * @return true if removed, false if not found
     */
    public boolean removeSkill(String candidateId, String skill) {
        Candidate candidate = getCandidateById(candidateId);
        if (candidate == null || skill == null || skill.trim().isEmpty()) {
            return false;
        }
        return candidate.removeSkill(skill.trim());
    }

    /**
     * Returns candidate skills.
     */
    public Set<String> getSkills(String candidateId) {
        Candidate candidate = getCandidateById(candidateId);
        if (candidate == null) {
            return Set.of();
        }
        return candidate.getSkills();
    }

    /**
     * Generates sequential Candidate ID (e.g., C001, C002, ...).
     */
    public synchronized String generateNextCandidateId() {
        int maxId = 0;
        for (String id : candidateMap.keySet()) {
            if (id.toUpperCase().startsWith("C")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("C%03d", maxId + 1);
    }

    public int getCandidateCount() {
        return candidateMap.size();
    }
}
