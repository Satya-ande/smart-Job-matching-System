# SmartJob — Smart Job Matching & Skill Gap Analysis System

> Version 1 — Core Java + DSA | Java 17+ | No Maven Required

---

## Description

SmartJob is a console-based Java application that matches candidates with jobs based on their skills.
It calculates a weighted match score, identifies missing skills, and gives a priority-ordered learning roadmap.
All data is saved in CSV files — no database required.

---

## Features

- Add and manage candidate profiles with skills
- Add and manage job postings (required + preferred skills)
- Search and filter jobs by title, skill, company, location, or salary
- Get a weighted match score for any candidate vs job
- View which skills are missing (required vs preferred)
- Get a learning roadmap sorted by priority
- Apply for jobs and track status (Applied → Shortlisted → Interview → Selected/Rejected)
- Duplicate application prevention
- Data auto-saved to CSV on exit

---

## Tech Stack

| Item | Detail |
|---|---|
| Language | Java 17+ |
| Build | No Maven needed — plain `javac` |
| Storage | CSV files (`candidates.csv`, `jobs.csv`, `applications.csv`) |
| Testing | Custom `TestRunner` + JUnit 5 compatible tests |

---

## Architecture

```
src/main/java/com/smartjob/
├── Main.java              ← App entry point & menu
├── model/                 ← Candidate, Job, Application, MatchResult
├── service/               ← CandidateService, JobService, JobMatcher, ApplicationService
├── repository/            ← FileRepository (CSV read/write)
└── util/                  ← InputValidator, MatchingUtils, ConsoleUtils

src/test/java/com/smartjob/
├── TestRunner.java
├── JobMatcherTest.java
├── CandidateServiceTest.java
├── ApplicationServiceTest.java
└── InputValidatorTest.java

data/
├── candidates.csv
├── jobs.csv
└── applications.csv
```

---

## Algorithms Used

| Algorithm / DSA | Where Used |
|---|---|
| `HashSet` | Stores candidate & job skills — O(1) lookup, prevents duplicates |
| `HashMap` | Stores candidates and jobs by ID — O(1) retrieval |
| `PriorityQueue` (Max-Heap) | Ranks top-K matching jobs without full sort |
| `Comparator` | Multi-level tie-breaking: score → salary → experience |
| Weighted Scoring | Required skill = 5 pts, Preferred skill = 2 pts |
| Composite Formula | Score = 70% skill + 20% experience + 10% location |

---

## Time & Space Complexity

| Operation | Time | Space |
|---|---|---|
| Skill lookup | O(1) avg | O(1) |
| Candidate / Job lookup by ID | O(1) avg | O(1) |
| Match one candidate to one job | O(R + P) | O(R + P) |
| Top-K job ranking | O(N log K) | O(N) |
| Search / Filter jobs | O(N) | O(M) |
| Duplicate application check | O(A) | O(1) |

> R = required skills count, P = preferred skills count, N = total jobs, K = top-K requested, A = total applications

---

## How to Run

### 1. Check Java is installed
```cmd
java --version
```
You need Java 17 or higher. Download: https://www.oracle.com/java/technologies/downloads/

### 2. Open the project folder in Command Prompt
```cmd
cd "C:\Users\hp\OneDrive\Desktop\Hema\Smart Job Matching System"
```

### 3. Compile
```cmd
compile.bat
```
✅ Expected: `[SUCCESS] Compilation finished successfully.`

### 4. Run Tests *(optional)*
```cmd
test.bat
```
✅ Expected: `TEST SUMMARY: 4 Suite(s) Passed, 0 Suite(s) Failed`

### 5. Run the App
```cmd
run.bat
```
The main menu will appear. Use number keys to navigate.

---

### Troubleshooting

| Problem | Fix |
|---|---|
| `java` not recognized | Install JDK 17+ and add to PATH |
| `compile.bat` fails | Make sure you are in the project root folder |
| App not starting | Run `compile.bat` first, then `run.bat` |
| Data not saving | Check that the `data/` folder exists |

---

*SmartJob v1 — Engineered by Satya & Team | MIT License*
