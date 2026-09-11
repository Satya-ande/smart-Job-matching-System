# SMARTJOB — Smart Job Matching & Skill Gap Analysis System
> **Version 1 — Core Java & DSA Foundation**

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![DSA](https://img.shields.io/badge/Data%20Structures-HashSet%20%7C%20HashMap%20%7C%20PriorityQueue-007396?style=for-the-badge)](https://en.wikipedia.org/wiki/Data_structure)
[![Status](https://img.shields.io/badge/Version-1.0.0%20(Production%20Ready)-success?style=for-the-badge)]()

---

## 1. Project Overview

**SmartJob** is a console-based, high-performance job matching and skill-gap diagnostic engine built with **Core Java** and **fundamental Data Structures & Algorithms (DSA)**.

In modern recruitment, candidates frequently face ambiguity regarding how well their profile matches a job posting and which specific skills they are missing. **SmartJob Version 1** solves this by:
1. Comparing candidate skill inventories against required and preferred job specifications in $O(1)$ lookup time.
2. Computing a transparent, reproducible weighted match score.
3. Diagnosing skill gaps and generating an actionable, priority-ordered learning roadmap.
4. Ranking the top job recommendations efficiently using a **Max-Heap PriorityQueue**.
5. Tracking recruitment applications while preventing duplicate submissions.
6. Persisting all data safely across restarts using fault-tolerant CSV repositories.

---

## 2. Core Features (Version 1)

* **Candidate Management**:
  * Create, view, update, switch, and list candidate profiles.
  * Normalized case-insensitive skill inventory management (preventing duplicates like `java`, `Java`, `JAVA`).
* **Job Management**:
  * Add, view, update, delete, and inspect detailed job postings.
  * Separate tracking of **Required Skills** (core) and **Preferred Skills** (bonus).
* **Multi-Field Search & Combinatorial Filtering**:
  * Case-insensitive search by Job Title, Skill, Company, or Location.
  * Combinatorial filters combining Location, Minimum Salary (LPA), Maximum Experience, and Job Type.
* **Weighted Matching Engine**:
  * Transparent point system: **5 points per Required Skill**, **2 points per Preferred Skill**.
  * Composite scoring formula incorporating skill compatibility ($70\%$), experience compatibility ($20\%$), and location suitability ($10\%$).
* **Top-K Ranking with PriorityQueue**:
  * Retrieves Top $K$ jobs utilizing a Binary Heap / PriorityQueue with multi-level tie-breaking (Score $\to$ Max Salary $\to$ Experience).
* **Skill Gap Diagnostic & Learning Roadmap**:
  * Distinct categorization of Matched Skills, Missing Required Skills (Critical Impact), and Missing Preferred Skills (Enhancement).
  * Priority-ordered roadmap guiding the candidate on what to learn next.
* **Application System & Status Tracking**:
  * Submit job applications with automatic duplicate application prevention.
  * Recruitment status lifecycle tracking (`APPLIED` $\to$ `SHORTLISTED` $\to$ `INTERVIEW` $\to$ `SELECTED` / `REJECTED`).
* **Robust File Persistence**:
  * CSV file storage for `candidates.csv`, `jobs.csv`, and `applications.csv`.
  * Fault-tolerant parsing that skips corrupted rows without crashing.

---

## 3. Technology Stack

* **Language**: Java 17+ (Fully compatible with Java 17, 21, and 26)
* **Build Tool**: Apache Maven (standard `pom.xml`)
* **Core Libraries**: Java Standard Library (`java.util`, `java.io`, `java.nio`, `java.time`, `java.math`)
* **Persistence**: CSV File Handling (`BufferedReader`, `BufferedWriter`, `Files`, `Path`)
* **Testing**: Automated Unit Test Suite (`TestRunner.java` + JUnit 5 compatible)

---

## 4. Architecture & Layered Design

The codebase strictly follows the **Separation of Concerns (SoC)** and **Single Responsibility Principle (SRP)**:

```
SmartJobMatcher/
│
├── pom.xml                                 # Maven configuration
├── README.md                               # Project documentation & interview guide
├── .gitignore                              # Git exclusion rules
├── compile.bat                             # Windows batch compile script
├── run.bat                                 # Windows batch run script
├── test.bat                                # Windows batch test runner
│
├── data/                                   # CSV Persistence Store
│   ├── candidates.csv                      # Candidate records
│   ├── jobs.csv                            # Job specifications
│   └── applications.csv                    # Application history
│
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── smartjob/
    │               ├── Main.java           # CLI Entry point & Menu Orchestrator
    │               │
    │               ├── model/              # Encapsulated Domain Models
    │               │   ├── Candidate.java
    │               │   ├── Job.java
    │               │   ├── Application.java
    │               │   ├── MatchResult.java
    │               │   └── ApplicationStatus.java (Enum)
    │               │
    │               ├── service/            # Core Business & Algorithmic Layer
    │               │   ├── CandidateService.java
    │               │   ├── JobService.java
    │               │   ├── JobMatcher.java
    │               │   └── ApplicationService.java
    │               │
    │               ├── repository/         # File I/O & Persistence
    │               │   └── FileRepository.java
    │               │
    │               └── util/               # Helper & Validation Utilities
    │                   ├── InputValidator.java
    │                   ├── MatchingUtils.java
    │                   └── ConsoleUtils.java
    │
    └── test/
        └── java/
            └── com/
                └── smartjob/               # Comprehensive Unit Test Suite
                    ├── TestRunner.java
                    ├── JobMatcherTest.java
                    ├── CandidateServiceTest.java
                    ├── ApplicationServiceTest.java
                    └── InputValidatorTest.java
```

---

## 5. Data Structures & Algorithm (DSA) Rationale

| Data Structure | Use Case in SmartJob | Algorithmic Rationale & Benefit |
| :--- | :--- | :--- |
| **`HashSet` / `LinkedHashSet`** | Candidate skills, Required skills, Preferred skills | Ensures **uniqueness** of skills, prevents duplicates, and provides **$O(1)$ average time complexity** for membership checks (`candidateSkills.contains(jobSkill)`). |
| **`HashMap` / `LinkedHashMap`** | `candidateMap` (ID $\to$ Candidate), `jobMap` (ID $\to$ Job), `applicationMap` | Provides **$O(1)$ instantaneous retrieval** of profiles and job listings by unique identifier. |
| **`PriorityQueue` (Max-Heap)** | Ranking Top $K$ best matching jobs | Enables finding top $K$ items in $O(N \log K)$ or $O(N + K \log N)$ without requiring full $O(N \log N)$ sorting of the entire dataset. |
| **`ArrayList`** | Search results, filter outputs, application lists | Contiguous memory allocation, fast $O(1)$ random access by index, low memory overhead. |
| **`Comparator`** | Multi-attribute sorting for `MatchResult` | Decouples ranking logic; handles primary match score comparison with deterministic tie-breaking (Max Salary $\to$ Experience). |

---

## 6. Algorithmic Formulations

### 6.1 Weighted Skill Matching Formula

Job skills are divided into **Required** (weight = 5) and **Preferred** (weight = 2):

$$\text{Total Possible Points} = (|R| \times 5) + (|P| \times 2)$$

$$\text{Points Earned} = (|\text{Matching } R| \times 5) + (|\text{Matching } P| \times 2)$$

$$\text{Skill Score (\%)} = \begin{cases} 100.0, & \text{if Total Possible Points} = 0 \\ \left( \frac{\text{Points Earned}}{\text{Total Possible Points}} \right) \times 100, & \text{otherwise} \end{cases}$$

### 6.2 Composite Match Score Formula

To mirror realistic hiring evaluations, SmartJob calculates a transparent composite score:

$$\text{Overall Score} = (0.70 \times \text{SkillScore}) + (0.20 \times \text{ExperienceScore}) + (0.10 \times \text{LocationScore})$$

* **Experience Compatibility**:
  $$\text{ExperienceScore} = \begin{cases} 100.0, & \text{if Candidate Experience} \ge \text{Job Required Experience} \\ \left( \frac{\text{Candidate Experience}}{\text{Job Required Experience}} \right) \times 100, & \text{otherwise} \end{cases}$$
* **Location Compatibility**:
  * $100\%$ if Candidate Location matches Job Location, or either is `Remote`.
  * $80\%$ if partial location match.
  * $0\%$ if mismatch.

### 6.3 Skill Gap Diagnostic Prioritization

Missing skills are automatically sequenced for candidate upskilling:
1. **Critical Priority**: All missing **Required Skills** (5 pts weight).
2. **Enhancement Priority**: All missing **Preferred Skills** (2 pts weight).

---

## 7. Time & Space Complexity Analysis

| Operation | Method / Class | Time Complexity | Space Complexity |
| :--- | :--- | :--- | :--- |
| **Skill Membership Check** | `MatchingUtils.setContainsIgnoreCase` / `Candidate.hasSkill` | $O(1)$ avg ($O(L)$ where $L$ is string length) | $O(1)$ |
| **Candidate / Job Lookup by ID** | `CandidateService.getCandidateById` / `JobService.getJobById` | $O(1)$ avg | $O(1)$ |
| **Candidate-Job Matching** | `JobMatcher.match` | $O(|R| + |P|)$ | $O(|R| + |P|)$ |
| **Top-K Job Recommendation** | `JobMatcher.getTopMatchingJobs` | $O(N + K \log N)$ or $O(N \log K)$ | $O(N)$ |
| **Multi-Criteria Search/Filter** | `JobService.searchByTitle` / `filterJobs` | $O(N)$ linear scan | $O(M)$ (where $M \le N$ matches) |
| **Duplicate Application Check** | `ApplicationService.hasAlreadyApplied` | $O(A)$ where $A$ is active applications | $O(1)$ |

---

## 8. Installation and Execution

### Prerequisites
* **Java Development Kit (JDK)**: Version 17 or higher (`javac` and `java` on PATH).
* **Maven** (Optional, if building via Maven CLI).

### Option A: Direct Build & Run (No Maven Required)

On Windows, use the included batch scripts:

```cmd
# 1. Compile all source and test files
compile.bat

# 2. Run automated test suite
test.bat

# 3. Launch the SmartJob application
run.bat
```

Or via standard Java commands:

```bash
# Compile
javac -d bin $(find src -name "*.java")

# Run Automated Tests
java -ea -cp bin com.smartjob.TestRunner

# Run Application
java -cp bin com.smartjob.Main
```

### Option B: Build & Run with Maven

```bash
# Compile and package
mvn clean package

# Run tests
mvn test

# Execute Application
mvn exec:java
```

---

## 9. Console Workflow & Sample Output

### 9.1 Main Menu
```text
================================================================================
                                  MAIN MENU                                     
  [Active Candidate: Satya Prakash (C001)]
================================================================================
  1. Candidate Profile & Skills
  2. Job Management
  3. Search & Filter Jobs
  4. Find Matching Jobs (Weighted DSA Scoring & PriorityQueue)
  5. Skill Gap Analysis & Learning Roadmap
  6. Apply for a Job
  7. My Applications & Tracking
  8. Save & Exit
================================================================================
```

### 9.2 Top-K Matching Output (PriorityQueue)
```text
--------------------------------------------------------------------------------
  TOP 5 MATCHING JOBS (PriorityQueue Max-Heap Evaluation)
--------------------------------------------------------------------------------

  #1. Junior Backend Engineer at CloudNest Solutions
      Job ID: J005  | Location: Remote       | Exp Req: Fresher (0 years)
      Salary: ₹4,50,000 - ₹6,50,000 (4.5 - 6.5 LPA)
      COMPOSITE MATCH SCORE : 92.63%
        * Skill Score       : 89.47% (17/19 pts earned)
        * Matching Skills   : Java, SQL, Git | (Pref) Spring Boot
        * Missing Req Skills: None

  #2. Java Backend Developer at ABC Technologies
      Job ID: J001  | Location: Hyderabad    | Exp Req: 1 year
      Salary: ₹6,00,000 - ₹9,00,000 (6.0 - 9.0 LPA)
      COMPOSITE MATCH SCORE : 79.58%
        * Skill Score       : 70.83% (17/24 pts earned)
        * Matching Skills   : Java, SQL, Spring Boot | (Pref) Git
        * Missing Req Skills: REST API
```

### 9.3 Skill Gap Diagnostic & Learning Roadmap
```text
================================================================================
                              SKILL GAP ANALYSIS                                
================================================================================
  Target Role : Java Backend Developer
  Company     : ABC Technologies
  Location    : Hyderabad
  Match Score : 79.58%
--------------------------------------------------------------------------------
  [MATCHED SKILLS]
    [MATCH] [Required]  Java
    [MATCH] [Required]  SQL
    [MATCH] [Required]  Spring Boot
    [MATCH] [Preferred] Git

  [MISSING REQUIRED SKILLS] (High Impact - 5 pts each)
    [MISSING] REST API

  [MISSING PREFERRED SKILLS] (Bonus Impact - 2 pts each)
    [MISSING] Docker
--------------------------------------------------------------------------------
  RECOMMENDED LEARNING ROADMAP (Priority Ordered):
     1. REST API                  [CRITICAL - Required]
     2. Docker                    [ENHANCEMENT - Preferred]
================================================================================
```

---

## 10. Placement Interview Q&A & Technical Deep-Dive

### Q1: Why did you choose `HashSet` instead of `ArrayList` for candidate and job skills?
> **Answer**: `ArrayList` allows duplicates and requires an $O(M)$ linear scan for every skill lookup (`contains`), resulting in $O(N \times M)$ matching complexity. `HashSet` guarantees uniqueness, normalizes case variations, and performs lookup in $O(1)$ average time through hashing, making the matching engine significantly faster and mathematically sound.

### Q2: How is `PriorityQueue` utilized in Top-K job ranking?
> **Answer**: Instead of collecting all job matches and running a full collection sort ($O(N \log N)$), we feed evaluated `MatchResult` objects into a `PriorityQueue` initialized with our custom descending `Comparator`. When extracting the top $K$ recommendations, we poll the heap $K$ times in $O(K \log N)$ time.

### Q3: How do you prevent duplicate job applications?
> **Answer**: `ApplicationService` enforces business uniqueness by verifying whether any existing record shares both the `candidateId` and `jobId`. If a match is found, an `IllegalStateException` is raised with a user-friendly message (`"You have already applied for this job."`).

### Q4: How is fault tolerance handled in the CSV persistence layer?
> **Answer**: In `FileRepository`, file reading uses try-with-resources (`BufferedReader`). Each row is validated for token count and type parsing. Malformed rows are caught in an isolated try-catch block, logged as non-fatal warnings, and skipped, ensuring corrupted records never crash the application.

---

## 11. Product Roadmap (Future Versions)

* **Version 2 (Full-Stack Enterprise Architecture)**:
  * Migration from CSV to **PostgreSQL** relational database.
  * Backend refactor to **Spring Boot** with **RESTful APIs** (`@RestController`, `@Service`, `@Repository`).
  * Web frontend built with **React** or modern UI.
  * Role-based access control (Admin, Recruiter, Candidate) with JWT authentication.
* **Version 3 (AI & NLP Intelligence Layer)**:
  * Machine learning & NLP resume parsing (spaCy, Python backend).
  * Vector embeddings and semantic similarity matching (e.g. recognizing that "Spring Framework" and "Spring Boot" share semantic proximity).
  * Automated resume-to-job recommendation engine.

---

## 12. License & Author

* **Project**: SmartJob Matcher — Version 1
* **Engineered by**: Satya & Development Team
* **License**: MIT Open Source License
