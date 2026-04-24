# Quiz Leaderboard System

**Bajaj Finserv Health — Java Qualifier (SRM)**

A Java 17 application that polls a quiz validator API, correctly deduplicates event data across multiple responses, aggregates participant scores, and submits a final ranked leaderboard — exactly once.

---

## Architecture

```
QuizLeaderboard (main)
├── pollAllEvents()       → GET /quiz/messages × 10 (5s delay)
├── deduplicateEvents()   → composite key: roundId + participant
├── aggregateScores()     → sum scores per participant
└── submitLeaderboard()   → POST /quiz/submit (exactly once)
```

### Model Classes

| Class              | Purpose                                       |
|--------------------|-----------------------------------------------|
| `QuizEvent`        | Single event: roundId, participant, score      |
| `PollResponse`     | API response: regNo, setId, pollIndex, events  |
| `LeaderboardEntry` | Aggregated: participant, totalScore            |
| `SubmitRequest`    | POST body: regNo + leaderboard                |
| `SubmitResponse`   | POST result: isCorrect, totals, message        |

---

## Prerequisites

- **Java 17+** — [Download](https://adoptium.net/)
- **Apache Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)

Verify installation:
```bash
java -version    # should show 17+
mvn -version     # should show 3.8+
```

---

## Setup, Build & Run

### 1. Clone the repository
```bash
git clone https://github.com/your-username/your-repo.git
cd your-repo
```

### 2. Build the fat JAR
```bash
mvn clean package
```
This produces `target/quiz-leaderboard-1.0.0.jar` (fat JAR with all dependencies).

### 3. Run
```bash
java -jar target/quiz-leaderboard-1.0.0.jar <YOUR_REG_NO>
```

**Example:**
```bash
java -jar target/quiz-leaderboard-1.0.0.jar 2024CS101
```

---

## What Happens at Runtime

1. **Polls** the API 10 times (index 0–9) with a **5-second mandatory delay** between each
2. **Collects** all quiz events across all polls
3. **Deduplicates** using composite key `roundId + participant` — only first occurrence kept
4. **Aggregates** scores per participant via summation
5. **Sorts** leaderboard in **descending order** by `totalScore`
6. **Submits** via `POST /quiz/submit` — **exactly once**
7. Prints the final result (correct/incorrect) with score comparison

---

## Project Structure

```
├── pom.xml                                          # Maven config (Java 17, Jackson, Shade)
├── README.md                                        # This file
├── task.md                                          # Assignment requirements
└── src/main/java/com/bajaj/quiz/
    ├── QuizLeaderboard.java                         # Main entry point
    └── model/
        ├── QuizEvent.java                           # Event POJO
        ├── PollResponse.java                        # GET response POJO
        ├── LeaderboardEntry.java                    # Leaderboard entry POJO
        ├── SubmitRequest.java                       # POST request POJO
        └── SubmitResponse.java                      # POST response POJO
```

---

## Key Design Decisions

| Decision | Rationale |
|---|---|
| **Composite key deduplication** | Uses `roundId + participant` as specified — `LinkedHashSet` preserves insertion order |
| **Fat JAR via maven-shade** | Single executable JAR, no classpath issues |
| **`java.net.http.HttpClient`** | Built-in Java 11+ HTTP client — no extra dependencies |
| **`@JsonIgnoreProperties`** | Resilient to API changes — ignores unknown fields |
| **CLI argument for regNo** | No hardcoded values — portable and testable |

---

## Dependencies

| Dependency | Version | Purpose |
|---|---|---|
| `jackson-databind` | 2.15.3 | JSON serialization/deserialization |
| `maven-shade-plugin` | 3.5.1 | Fat JAR packaging |
| `maven-compiler-plugin` | 3.11.0 | Java 17 compilation |
