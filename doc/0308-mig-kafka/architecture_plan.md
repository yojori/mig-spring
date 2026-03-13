# Hybrid Zero-Downtime Migration Architecture

This document outlines the architecture for a Zero-Downtime Database Migration platform, combining the strengths of Spring Batch (`mig-spring`) and Kafka (`mig-kafka`).

## 1. Core Strategy: The "Catch-up" Approach

To achieve zero-downtime without overwhelming the message broker, we separate the migration into **Initial Snapshot Load** and **Real-time Delta Sync**.

1. **Phase 1: Start Delta Capture**
   - Start `mig-kafka` Producer to begin capturing all *new* transactions (CUD) into the Kafka Topic.
2. **Phase 2: Initial Batch Load**
   - Run `mig-spring` to migrate all existing historical data directly to the Target DB at high speed.
3. **Phase 3: Catch-up**
   - Once `mig-spring` finishes, start `mig-kafka` Consumer. It reads the accumulated messages from the Kafka Topic and applies them to the Target DB (`UPSERT`).
4. **Phase 4: Sync & Cutover**
   - The Target DB is now fully synced in real-time. Application traffic can be routed to the new system.

## 2. Design Decisions
- **Project Structure**: `mig-kafka` will be implemented as a sub-module under the existing `mig-spring` multi-module project.
- **Metadata Storage**: Configuration templates (`KFK_TEMPLATE`), parameters (`KFK_PARAM`), consumer/producer offsets (`KFK_OFFSET`), and transformation rules (`KFK_MAPPING`) will be stored in the root `mig-controller` DB.
- **Metadata-Driven Transformation**: To clearly separate the roles of the Business PL (Defining Data Mappings) and the Migration Engineer (Platform Execution), data transformations will not be hard-coded. Instead, they will be dynamically applied at runtime based on metadata rules (e.g., dynamic SQL or JSON transformation rules) stored in the database.

## 3. Structural Components

### `mig-spring` (Batch/Initial Load)
- No immediate changes to the core migration logic. This remains the primary engine for high-throughput initial data loading.

### `mig-kafka` (Real-time Delta Sync) [NEW MODULE]
- **Producer (`mig-kafka-producer`)**:
  - Uses Spring `@Scheduled` to intermittently poll the Source DB based on `timestamp` columns (e.g., `updated_at`).
  - Read/Write offset tracking is managed in the `KFK_OFFSET` table within the `mig-controller` DB.
  - Serializes data (JSON) and publishes to Kafka.
- **Consumer (`mig-kafka-consumer`)**:
  - Uses Spring `@KafkaListener` to consume messages from the designated topics.
  - **Transformer**: Applies dynamic data transformation rules (loaded from `KFK_MAPPING`) to incoming messages, converting AS-IS schema payloads to TO-BE schema payloads without hard-coded logic.
  - Aggregates transformed messages into batches.
  - Executes dynamic SQL or `jdbcTemplate.batchUpdate()` to perform UPSERT commands into the Target DB.

### Control Plane (`mig-controller`)
- Hosts the meta-tables (`KFK_TEMPLATE`, `KFK_PARAM`, `KFK_OFFSET`, `KFK_MAPPING`).
- Provides UI/API for the Business PL to upload or map data transformation rules (e.g., via Excel import) directly into the `KFK_MAPPING` metadata table.
- Provides REST APIs to generate and manage migration pipelines.
- `mig-kafka` Producer/Consumer will query these APIs (or directly query the repository depending on module dependency resolution) on startup to dynamically configure their polling/listening logic and cache the transformation rules.

## 4. UI Integration & Execution Model (`pageCode.xml` & `mig-worker`)

The hybrid architecture integrates seamlessly with the existing migration management UI:

### UI Registration (`pageCode.xml`)
- A new migration type `<option value="KAFKA">Kafka 실시간 이관</option>` will be added to the UI selection.
- Users create `mig_master` and `mig_list` entries selecting the `KAFKA` type.

### Execution Paradigm Shift
Depending on the selected migration type, the execution engine differs fundamentally:

- **Batch Migration (`mig-worker`)**: 
  - **Type**: `NORMAL`, `THREAD`, `THREAD_IDX`
  - **Behavior**: One-off, high-speed data pumping. The controller spawns a specified number of threads that execute `SELECT/INSERT` batch jobs and terminate upon completion (The "Rocket").
- **Real-time Migration (`mig-kafka`)** [NEW]:
  - **Type**: `KAFKA`
  - **Behavior**: Continuous, scheduled synchronization. When triggered, the controller does not spawn data-pumping threads. Instead, it generates a Connector Configuration (based on `KFK_TEMPLATE`) and commands the `mig-kafka` module.
  - `mig-kafka` utilizes Spring `@Scheduled` or `Quartz Scheduler` to continuously poll the source (Producer) and upsert the target (Consumer) until manually stopped by the user (The "Satellite").

### Monitoring Dashboard
- Alongside the existing batch progress bars, a new **Kafka Connector Status Board** will be introduced.
- This board will display real-time metrics per connector: Status (Running/Failed/Stopped), Current Offset (timestamp/PK), and Replication Lag.

## 5. Data Transformation & Role Separation (R&R)

To prevent the Migration Engineer from becoming a bottleneck and to ensure clear Role & Responsibility (R&R) separation, the architecture adopts a **Metadata-Driven Rule Engine** approach for AS-IS to TO-BE data transformations.

### Workflow & Roles

1. **Business PL (Mapping Definition)**:
   - Analyzes AS-IS/TO-BE schemas and common code changes.
   - Defines data mapping rules (e.g., 1:1 mapped columns, merged columns, default values) using a standardized Excel template or a UI mapping tool.
   - *No coding required.*
2. **Migration Engineer (Platform Execution)**:
   - Uploads the Business PL's Excel mapping document into the `mig-controller` UI.
   - The system automatically parses this and stores it in the `KFK_MAPPING` meta-table as transformation specifications (e.g., Dynamic SQL templates, Spring Expressions, or JSON-to-JSON mappings).
   - *No Java streaming logic coding required for each table.*
3. **mig-kafka (Dynamic Execution)**:
   - During runtime, the `mig-kafka-consumer` reads the transformation rules from the DB.
   - Incoming Kafka messages (Source Schema) are automatically transformed to the Target Schema format based on these rules before being UPSERTed.

## 6. Current Progress (as of March 10, 2026)

### 6.1. Dynamic Transformer PoC (`mig-kafka` module)
- Created the **`mig-kafka`** sub-module in the `mig-spring` project.
- Implemented **`SpelMessageTransformer`**: Proved metadata-driven transformation without hard-coded Java.

### 6.2. Metadata Tables & DAO API (Option A Completed)
- **Database Schema**: Created `KFK_TEMPLATE`, `KFK_PARAM`, `KFK_MAPPING`, `KFK_OFFSET`.
- **Backend API**: Created POJO Models and JDBC-based DAO Managers in `mig-controller`.
- **UI Integration**: 
    - Updated `migration-list-proc.jsp` to persist JSON mapping rules.
    - Created **Kafka Template Management** UI for connector configuration.

## 7. Next Steps: Option B (Kafka Client Integration)

With the Control Plane (UI/API) established, the next phase focuses on the actual data pipeline implementation in the `mig-kafka` module.

- **Objective**: Implement the actual Spring Kafka Producer and Consumer.
- **Tasks**:
    1. Configure Kafka Producer for incremental polling (using `KFK_OFFSET`).
    2. Configure Kafka Consumer with `@KafkaListener`.
    3. Integrate `SpelMessageTransformer` into the Consumer to apply real-time mappings.
    4. Verify end-to-end sync from Source DB -> Kafka -> Target DB.
