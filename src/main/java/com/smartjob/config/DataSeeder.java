package com.smartjob.config;

import com.smartjob.entity.*;
import com.smartjob.entity.enums.JobType;
import com.smartjob.entity.enums.Role;
import com.smartjob.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Seeds the database with sample data on application startup.
 *
 * Only seeds if the database is empty (checks for existing users).
 * Creates sample skills, users, candidates, jobs, and applications
 * matching the V1 CSV data for continuity.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CandidateRepository candidateRepository,
                      SkillRepository skillRepository,
                      JobRepository jobRepository,
                      ApplicationRepository applicationRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.skillRepository = skillRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded — skipping.");
            return;
        }

        log.info("Seeding database with sample data...");

        // ===== 1. Create Skills (from V1 data) =====
        Skill java = createSkill("Java");
        Skill python = createSkill("Python");
        Skill sql = createSkill("SQL");
        Skill spring = createSkill("Spring Boot");
        Skill react = createSkill("React");
        Skill aws = createSkill("AWS");
        Skill docker = createSkill("Docker");
        Skill javascript = createSkill("JavaScript");
        Skill html = createSkill("HTML");
        Skill css = createSkill("CSS");
        Skill git = createSkill("Git");
        Skill kubernetes = createSkill("Kubernetes");
        Skill mongodb = createSkill("MongoDB");
        Skill typescript = createSkill("TypeScript");
        Skill nodejs = createSkill("Node.js");

        // ===== 2. Create Users =====
        // Recruiter
        User recruiter = createUser("Tech Recruiter", "recruiter@smartjob.com", "password123", Role.RECRUITER);

        // Candidates (matching V1 data)
        User satyaUser = createUser("Satya Ande", "satya@example.com", "password123", Role.CANDIDATE);
        User priyaUser = createUser("Priya Sharma", "priya@example.com", "password123", Role.CANDIDATE);
        User rahulUser = createUser("Rahul Verma", "rahul@example.com", "password123", Role.CANDIDATE);
        User ankitUser = createUser("Ankit Patel", "ankit@example.com", "password123", Role.CANDIDATE);
        User nehaaUser = createUser("Neha Reddy", "neha@example.com", "password123", Role.CANDIDATE);

        // ===== 3. Create Candidate Profiles =====
        Candidate satya = createCandidate(satyaUser, "B.Tech Computer Science", 2.0,
            "Java Developer", "Hyderabad", Set.of(java, python, sql, spring, git));

        Candidate priya = createCandidate(priyaUser, "M.Tech Software Engineering", 4.0,
            "Full Stack Developer", "Bengaluru", Set.of(java, react, javascript, html, css, spring));

        Candidate rahul = createCandidate(rahulUser, "B.Tech IT", 1.5,
            "Backend Developer", "Pune", Set.of(python, sql, docker, git));

        Candidate ankit = createCandidate(ankitUser, "MCA", 3.0,
            "Cloud Engineer", "Hyderabad", Set.of(aws, docker, kubernetes, python, sql));

        Candidate neha = createCandidate(nehaaUser, "B.Tech CSE", 5.0,
            "Tech Lead", "Bengaluru", Set.of(java, spring, react, aws, docker, sql, git));

        // ===== 4. Create Jobs (matching V1 data) =====
        Job job1 = createJob(recruiter, "Java Backend Developer", "TCS", "Hyderabad",
            3.0, 600000, 1000000, JobType.FULL_TIME,
            "Develop and maintain Java-based backend services",
            List.of(new SkillWeight(java, true), new SkillWeight(spring, true),
                    new SkillWeight(sql, true), new SkillWeight(docker, false),
                    new SkillWeight(git, false)));

        Job job2 = createJob(recruiter, "Full Stack Developer", "Infosys", "Bengaluru",
            2.0, 700000, 1200000, JobType.FULL_TIME,
            "Build full stack web applications using Java and React",
            List.of(new SkillWeight(java, true), new SkillWeight(react, true),
                    new SkillWeight(javascript, true), new SkillWeight(spring, false),
                    new SkillWeight(html, false), new SkillWeight(css, false)));

        Job job3 = createJob(recruiter, "Python Data Engineer", "Wipro", "Pune",
            1.0, 500000, 800000, JobType.FULL_TIME,
            "Design and implement data pipelines",
            List.of(new SkillWeight(python, true), new SkillWeight(sql, true),
                    new SkillWeight(aws, false)));

        Job job4 = createJob(recruiter, "DevOps Engineer", "Amazon", "Hyderabad",
            3.0, 1200000, 2000000, JobType.REMOTE,
            "Manage cloud infrastructure and CI/CD pipelines",
            List.of(new SkillWeight(aws, true), new SkillWeight(docker, true),
                    new SkillWeight(kubernetes, true), new SkillWeight(python, false),
                    new SkillWeight(git, false)));

        Job job5 = createJob(recruiter, "Frontend Developer", "Zoho", "Chennai",
            2.0, 600000, 900000, JobType.FULL_TIME,
            "Build modern web interfaces",
            List.of(new SkillWeight(react, true), new SkillWeight(javascript, true),
                    new SkillWeight(typescript, false), new SkillWeight(html, false),
                    new SkillWeight(css, false)));

        Job job6 = createJob(recruiter, "Backend Developer Intern", "Startup XYZ", "Remote",
            0.0, 300000, 500000, JobType.INTERNSHIP,
            "Learn and contribute to backend systems",
            List.of(new SkillWeight(java, true), new SkillWeight(sql, false),
                    new SkillWeight(git, false)));

        log.info("Database seeded successfully!");
        log.info("  Skills: {}", skillRepository.count());
        log.info("  Users: {}", userRepository.count());
        log.info("  Candidates: {}", candidateRepository.count());
        log.info("  Jobs: {}", jobRepository.count());
        log.info("");
        log.info("  Test accounts:");
        log.info("    Recruiter: recruiter@smartjob.com / password123");
        log.info("    Candidate: satya@example.com / password123");
        log.info("    Candidate: priya@example.com / password123");
    }

    // ===== Helper Methods =====

    private Skill createSkill(String name) {
        return skillRepository.save(new Skill(name));
    }

    private User createUser(String name, String email, String password, Role role) {
        return userRepository.save(new User(name, email, passwordEncoder.encode(password), role));
    }

    private Candidate createCandidate(User user, String education, double experience,
                                       String preferredRole, String preferredLocation,
                                       Set<Skill> skills) {
        Candidate candidate = new Candidate(user, education, experience, preferredRole, preferredLocation);
        candidate.setSkills(new HashSet<>(skills));
        return candidateRepository.save(candidate);
    }

    private Job createJob(User recruiter, String title, String company, String location,
                          double expRequired, double salaryMin, double salaryMax,
                          JobType jobType, String description, List<SkillWeight> skills) {
        Job job = new Job(title, company, location, expRequired, salaryMin, salaryMax,
                         jobType, description, recruiter);
        for (SkillWeight sw : skills) {
            JobSkill js = new JobSkill(job, sw.skill, sw.required);
            job.addJobSkill(js);
        }
        return jobRepository.save(job);
    }

    // Simple holder for skill + required flag
    private record SkillWeight(Skill skill, boolean required) {}
}
