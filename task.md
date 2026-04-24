# Quiz Leaderboard System — Bajaj Finserv Health Java Qualifier

## Overview
Build a Java application that polls a quiz validator API, correctly deduplicates event data across multiple responses, aggregates participant scores, and submits a final ranked leaderboard — exactly once.

- **Domain**: Distributed Systems / API Integration
- **Language**: Java 17+
- **Due**: 24 Apr 2026
- **Submission**: Public GitHub repo + form

## Problem Statement
This assignment simulates a real-world backend integration problem. Build an application that consumes API responses from an external validator system, processes them correctly, and produces a final result.

The validator represents a quiz show where **multiple participants receive scores across rounds**. Due to system behavior, the same API response data may appear across multiple calls. The challenge is not just aggregation — it is handling duplicate API response data correctly.

## Steps
1. Poll API 10 times
2. Collect all events
3. Deduplicate by composite key (`roundId + participant`)
4. Aggregate scores per participant
5. Sort leaderboard descending by totalScore & submit

## API Details
- **Base URL**: `https://devapigw.vidalhealthtpa.com/srm-quiz-task`
- **GET** `/quiz/messages?regNo=<REG_NO>&poll=<0-9>`
- **POST** `/quiz/submit`

## Requirements
| Requirement        | Detail                                        |
|--------------------|-----------------------------------------------|
| Language           | Java 17+                                      |
| Poll count         | Exactly 10 polls, index 0 through 9           |
| Poll delay         | 5 seconds mandatory between each request      |
| Deduplication key  | `roundId + participant`                        |
| Leaderboard sort   | Descending by totalScore                      |
| Submission         | POST `/quiz/submit` exactly once              |
| Build tool         | Maven (fat JAR preferred)                     |
| JSON library       | Jackson (jackson-databind 2.15+)              |

## Completion Checklist
- [x] Maven project with Java 17 and Jackson dependency
- [x] Poll API 10 times with 5-second delay
- [x] Deduplicate events using composite key (roundId + participant)
- [x] Aggregate scores per participant
- [x] Sort leaderboard descending by totalScore
- [x] Submit leaderboard via POST exactly once
- [x] Detailed README.md with setup, build, and run instructions
- [ ] Public GitHub repository with source code
- [ ] Submission form completed on the Bajaj Finserv Health portal
