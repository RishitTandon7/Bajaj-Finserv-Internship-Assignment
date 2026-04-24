# Quiz Leaderboard System

**Bajaj Finserv Health - Java Qualifier | SRM | April 2026**

A Java 17 application that polls a quiz validator API, deduplicates repeated quiz events with a composite key, aggregates participant scores, sorts the leaderboard, and submits the final result exactly once.

## Architecture

```text
QuizLeaderboard (orchestrator)
|-- QuizApiClient         -> GET polling with retry + one-shot POST submission
|-- EventDeduplicator     -> Composite key deduplication (roundId | participant)
`-- ScoreAggregator       -> Score totals + descending leaderboard
```

## Project Structure

```text
pom.xml
README.md
src/main/java/com/bajaj/quiz/
|-- QuizLeaderboard.java
|-- model/
|   |-- QuizEvent.java
|   |-- PollResponse.java
|   |-- LeaderboardEntry.java
|   |-- SubmitRequest.java
|   `-- SubmitResponse.java
`-- service/
    |-- QuizApiClient.java
    |-- EventDeduplicator.java
    `-- ScoreAggregator.java
src/test/java/com/bajaj/quiz/service/
|-- EventDeduplicatorTest.java
`-- ScoreAggregatorTest.java
```

## Prerequisites

- Java 17+
- Apache Maven 3.8+

```bash
java -version
mvn -version
```

## Build, Test, Run

Build the fat JAR:

```bash
mvn clean package
```

Run unit tests:

```bash
mvn test
```

Run the app:

```bash
java -jar target/quiz-leaderboard-1.0.0.jar <YOUR_REG_NO>
```

Example:

```bash
java -jar target/quiz-leaderboard-1.0.0.jar RA2311003010587
```

## Runtime Flow

1. Polls `GET /quiz/messages` exactly 10 times with poll indexes `0` through `9`.
2. Waits 5 seconds between poll requests.
3. Deduplicates events by `roundId + participant`.
4. Aggregates score totals per participant.
5. Sorts leaderboard descending by `totalScore`.
6. Submits once to `POST /quiz/submit`.

## Sample Output

```text
[STEP 2/5] Collected 15 raw events
[STEP 3/5] Deduplicated: 10 unique, 5 duplicates removed
[STEP 4/5] Aggregated scores for 3 participants

RANK   PARTICIPANT           TOTAL SCORE
#1     George                        795
#2     Hannah                        750
#3     Ivan                          745

Grand Total: 2290
Status     : COMPLETE
```

## Tests

The service layer has focused JUnit 5 coverage:

- `EventDeduplicatorTest` verifies composite-key duplicate rejection, valid key differentiation, insertion order, and reset behavior.
- `ScoreAggregatorTest` verifies score summation, event aggregation, empty state, grand total, and descending leaderboard sort.

## Design Decisions

| Decision | Rationale |
|---|---|
| Service layer | Keeps API, deduplication, and scoring independently testable. |
| Composite key `roundId|participant` | Matches the assignment rule and prevents duplicate scoring. |
| `LinkedHashSet` / `LinkedHashMap` | Preserves deterministic insertion order. |
| URL-encoded `regNo` | Keeps GET query construction safe. |
| GET retry with exponential backoff | Handles transient polling failures. |
| One-shot POST submission | Preserves the exact-once submission requirement. |
| Maven Shade plugin | Produces a single executable fat JAR. |
| Jackson `@JsonIgnoreProperties` | Allows the app to tolerate extra API fields. |

## Dependencies

| Dependency | Version | Purpose |
|---|---:|---|
| `jackson-databind` | 2.15.3 | JSON serialization/deserialization |
| `junit-jupiter` | 5.10.2 | Unit tests |
| `maven-compiler-plugin` | 3.11.0 | Java 17 compilation |
| `maven-surefire-plugin` | 3.2.5 | JUnit 5 test execution |
| `maven-shade-plugin` | 3.5.1 | Fat JAR packaging |

## Author

Rishit Tandon - RA2311003010587 (SRM)
