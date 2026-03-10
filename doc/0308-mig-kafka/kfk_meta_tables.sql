-- ==============================================================================
-- mig-kafka Meta Tables DDL
-- Description: Schema definitions for Kafka Connectors, Offsets, and Mappings
-- ==============================================================================

-- 1. KFK_TEMPLATE: Kafka Connector Templates
-- Stores the basic configuration templates for different types of sources/targets.
CREATE TABLE KFK_TEMPLATE (
    TPL_ID VARCHAR(50) NOT NULL,           -- Template ID (e.g., 'ORACLE_SRC', 'PG_SINK')
    TPL_NAME VARCHAR(100) NOT NULL,        -- Template Name
    TPL_TYPE VARCHAR(20) NOT NULL,         -- Type: 'PRODUCER' or 'CONSUMER'
    TPL_CLASS VARCHAR(255),                -- Java Class or Bean Name to execute
    TPL_DESC VARCHAR(500),                 -- Description
    USE_YN CHAR(1) DEFAULT 'Y',            -- Use Flag (Y/N)
    REG_DT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (TPL_ID)
);

-- 2. KFK_PARAM: Connector Execution Parameters
-- Stores the specific runtime parameters for a given migration task (MIG_ID)
CREATE TABLE KFK_PARAM (
    MIG_ID VARCHAR(50) NOT NULL,           -- Foreign Key to MIG_MASTER (existing table)
    PARAM_KEY VARCHAR(100) NOT NULL,       -- Configuration Key (e.g., 'topic.name', 'poll.interval')
    PARAM_VAL VARCHAR(500),                -- Configuration Value
    REG_DT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (MIG_ID, PARAM_KEY)
);

-- 3. KFK_MAPPING: Dynamic Data Transformation Rules
-- Stores the JSON rules used by SpelMessageTransformer to convert Source to Target.
CREATE TABLE KFK_MAPPING (
    MIG_ID VARCHAR(50) NOT NULL,           -- Foreign Key to MIG_MASTER
    TABLE_NAME VARCHAR(100) NOT NULL,      -- Target Table Name
    MAPPING_RULE TEXT NOT NULL,            -- JSON containing SpEL mapping rules
    REG_DT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (MIG_ID, TABLE_NAME)
);

-- 4. KFK_OFFSET: Offset Tracking for Zero-Downtime
-- Tracks the last read position (Producer) or last applied position (Consumer)
-- to ensure Exactly-Once or At-Least-Once delivery semantics upon restart.
CREATE TABLE KFK_OFFSET (
    MIG_ID VARCHAR(50) NOT NULL,           -- Foreign Key to MIG_MASTER
    TABLE_NAME VARCHAR(100) NOT NULL,      -- Source or Target Table Name
    COMPONENT_TYPE VARCHAR(20) NOT NULL,   -- 'PRODUCER' or 'CONSUMER'
    OFFSET_KEY VARCHAR(100) NOT NULL,      -- e.g., 'UPDATED_AT' or 'PK_VAL'
    OFFSET_VAL VARCHAR(255),               -- The actual offset value (timestamp or ID)
    LAST_SYNC_DT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (MIG_ID, TABLE_NAME, COMPONENT_TYPE)
);

-- Note: These tables should be created in the `mig-controller` database since
-- they are considered "Meta" configurations used to control the Kafka workers.
