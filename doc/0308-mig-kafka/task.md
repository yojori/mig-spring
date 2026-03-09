# Migration Platform Architecture & Implementation

## Phase 1: Architecture Design (Current)
- [x] Define the hybrid migration architecture (Catch-up strategy).
- [x] Decide on project structure: **`mig-spring` multi-module (`mig-kafka`)**.
- [x] Decide on metadata storage: **`mig-controller` base table**.

## Phase 2: `mig-kafka` Setup
- [ ] Create `mig-kafka` module under `mig-spring`.
- [ ] Configure `pom.xml` for Spring Boot, Spring Kafka, and dependencies.
- [ ] Set up basic application properties for Kafka broker connection.

## Phase 3: Custom Producer (`mig-kafka-producer`)
- [ ] Implement `PollingScheduler` to periodically query Source DB.
- [ ] Implement query logic based on `timestamp` columns + `KFK_OFFSET` resume logic.
- [ ] Implement `KafkaTemplate` publishing logic to designated topics.

## Phase 4: Custom Consumer (`mig-kafka-consumer`)
- [ ] Implement `@KafkaListener` to consume messages from topics.
- [ ] Extract and format payload data for the Target DB.
- [ ] Implement `jdbcTemplate.batchUpdate()` logic for high-throughput UPSERT operations.

## Phase 5: Control Plane 
- [ ] Design and create Entity classes for `KFK_TEMPLATE`, `KFK_PARAM`, and `KFK_OFFSET` within `mig-controller`.
- [ ] Create REST APIs to manage these configurations.
- [ ] Integrate Producer/Consumer to dynamically read these configurations instead of static properties.
