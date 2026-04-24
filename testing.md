# Testing Checklist

Use this before final submission to verify both the implementation and the assignment constraints.

## 1. Build Verification

```bash
mvn clean package
```

Expected result:
- Build finishes successfully.
- Fat JAR is created at `target/quiz-leaderboard-1.0.0.jar`.

## 2. CLI Validation

Run without arguments:

```bash
java -jar target/quiz-leaderboard-1.0.0.jar
```

Expected result:
- Program prints usage instructions.
- Program exits without polling or submitting.

Run with an empty/blank registration number if your shell allows it:

```bash
java -jar target/quiz-leaderboard-1.0.0.jar " "
```

Expected result:
- Program rejects the empty registration number.
- Program exits without polling or submitting.

## 3. End-to-End API Run

```bash
java -jar target/quiz-leaderboard-1.0.0.jar <YOUR_REG_NO>
```

Expected result:
- Exactly 10 GET polls are attempted: poll indexes `0` through `9`.
- A 5-second delay occurs between poll requests.
- Events are collected from successful responses.
- Duplicate events are removed using `roundId + participant`.
- Scores are aggregated per participant.
- Leaderboard is sorted by `totalScore` in descending order.
- `POST /quiz/submit` is called exactly once.

## 4. Output Review

Check the console summary:
- Raw event count is greater than or equal to unique event count.
- Duplicate count equals `raw events - unique events`.
- Grand total equals the sum of all leaderboard `totalScore` values.
- Submitted total, expected total, and correctness message are printed when returned by the API.

## 5. Edge Cases Covered

- Registration number is trimmed and validated before runtime.
- Registration number is URL-encoded in the GET query string.
- GET polling uses retry logic for transient server errors.
- Final POST submission does not retry, preserving the assignment requirement to submit exactly once.

## 6. Final Submission Readiness

- [ ] `mvn clean package` passes.
- [ ] End-to-end run completes with your real registration number.
- [ ] Console shows `Status : COMPLETE`.
- [ ] Submit response confirms the expected result.
- [ ] Public GitHub repository contains `pom.xml`, `README.md`, and `src/`.
- [ ] Submission form is completed on the Bajaj Finserv Health portal.
