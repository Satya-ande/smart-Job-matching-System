package com.smartjob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SmartJob Application — Version 2
 *
 * Spring Boot entry point for the Smart Job Matching & Skill Gap Analysis System.
 *
 * V2 Evolution:
 *   V1: Console-based Java application with CSV file persistence
 *   V2: Spring Boot REST API with PostgreSQL, JWT authentication, and role-based authorization
 *
 * The V1 matching algorithm (weighted scoring + PriorityQueue ranking) is preserved
 * and integrated into the Spring Boot service layer.
 */
@SpringBootApplication
public class SmartJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartJobApplication.class, args);
    }
}
