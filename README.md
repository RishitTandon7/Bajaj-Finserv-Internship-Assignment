# Quiz Leaderboard System

**Bajaj Finserv Health — Java Qualifier | SRM | April 2026**

A Java 17 application that polls a quiz validator API, deduplicates event data across multiple responses using a composite key, aggregates participant scores, and submits a final ranked leaderboard — exactly once.

---

## Architecture

```
QuizLeaderboard (orchestrator)
├── QuizApiClient         → HTTP GET polling + POST submission with retry & URL encoding
├── EventDeduplicator     → Composite key dedup (roundId | participant)
└── ScoreAggregator       → Per-participant score summation + leaderboard builder
```

### Project Structure

```
├── pom.xml
├── README.md
├── src/main/java/com/bajaj/quiz/
│   ├── QuizLeaderboard.java           ← Entry point & orchestrator
│   ├── model/
│   │   ├── QuizEvent.java             ← Event POJO (roundId, participant, score)
│   │   ├── PollResponse.java          ← GET /quiz/messages response
│   │   ├── LeaderboardEntry.java      ← Aggregated entry (participant, totalScore)
│   │   ├── SubmitRequest.java         ← POST /quiz/submit request body
│   │   └── SubmitResponse.java        ← POST /quiz/submit response
│   └── service/
│       ├── QuizApiClient.java         ← HTTP client with retry logic
│       ├── EventDeduplicator.java     ← Composite key deduplication
│       └── ScoreAggregator.java       ← Score aggregation & sorting
```

---

## Prerequisites

- **Java 17+** — [Download OpenJDK](https://adoptium.net/)
- **Apache Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)

```bash
java -version    # should show 17+
mvn -version     # should show 3.8+
```

---

## Build & Run

### Build the fat JAR

```bash
mvn clean package
```

### Run

```bash
java -jar target/quiz-leaderboard-1.0.0.jar <YOUR_REG_NO>
```

**Example:**

```bash
java -jar target/quiz-leaderboard-1.0.0.jar RA2311003010587
```

---

## Sample Output

```
============================================================
  QUIZ LEADERBOARD SYSTEM
  Bajaj Finserv Health - Java Qualifier (SRM)
============================================================

  Registration No : RA2311003010587
  API Endpoint    : https://devapigw.vidalhealthtpa.com/srm-quiz-task
  Poll Count      : 10 (5s interval)
  Dedup Key       : roundId + participant

------------------------------------------------------------

  [STEP 1/5] Polling API...

  [19:26:05] Poll 0/9  1/10  OK  +2 event(s)
  [19:26:10] Poll 1/9  2/10  OK  +1 event(s)
  [19:26:15] Poll 2/9  3/10  OK  +2 event(s)
  ...
  [19:26:50] Poll 9/9  10/10  OK  +1 event(s)

------------------------------------------------------------

  [STEP 2/5] Collected 15 raw events
  [STEP 3/5] Deduplicated: 10 unique, 5 duplicates removed
  [STEP 4/5] Aggregated scores for 3 participants

============================================================
  FINAL LEADERBOARD
============================================================

  RANK   PARTICIPANT           TOTAL SCORE
------------------------------------------------------------
  #1     George                        795 [1st]
  #2     Hannah                        750 [2nd]
  #3     Ivan                          745 [3rd]
------------------------------------------------------------
         GRAND TOTAL                  2290

  [STEP 5/5] Submitting leaderboard...

  STATUS  : 200 - ACCEPTED
  Total   : 2290

============================================================
  EXECUTION SUMMARY
============================================================

  Registration     : RA2311003010587
  Raw Events       : 15
  Duplicates       : 5
  Unique Events    : 10
  Participants     : 3
  Grand Total      : 2290
  Elapsed Time     : 46s
  Status           : COMPLETE

============================================================
```

---

## Design Decisions

| Decision | Rationale |
|---|---|
| **Service layer** (`service/`) | Clean separation — dedup, aggregation, and API logic are independently testable |
| **Composite key** `roundId\|participant` | As specified — `LinkedHashSet` preserves insertion order for determinism |
| **URL-encoded regNo** | Production safety — handles special characters in registration numbers |
| **Retry with exponential backoff** | Resilient to transient 5xx API errors (3 retries: 3s → 6s → 12s) |
| **Fat JAR via maven-shade** | Single executable, no classpath issues |
| **`java.net.http.HttpClient`** | Built-in Java 11+ — no extra dependencies needed |
| **`@JsonIgnoreProperties`** | Forward-compatible — ignores unknown API fields |
| **CLI argument for regNo** | No hardcoded values — portable and testable |
| **Timestamp logging** | Each poll shows `HH:mm:ss` — proves mandatory 5-second delay compliance |

---

## Dependencies

| Dependency | Version | Purpose |
|---|---|---|
| `jackson-databind` | 2.15.3 | JSON serialization/deserialization |
| `maven-shade-plugin` | 3.5.1 | Fat JAR packaging |
| `maven-compiler-plugin` | 3.11.0 | Java 17 compilation |

---

## Author

**Rishit Tandon** — RA2311003010587 (SRM)
