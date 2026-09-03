package com.smartjob.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for adding a skill (used for candidate skill management and skill creation).
 */
public class SkillRequest {

    @NotBlank(message = "Skill name is required")
    private String name;

    public SkillRequest() {}

    public SkillRequest(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
