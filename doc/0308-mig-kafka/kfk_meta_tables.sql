-- ==============================================================================
-- mig-kafka Meta Tables DDL (MariaDB)
-- Description: Schema definitions synchronized with Java Manager classes
-- ==============================================================================

-- 1. KFK_TEMPLATE: Kafka Connector Templates
CREATE TABLE KFK_TEMPLATE (
    template_id VARCHAR(50) NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    template_type VARCHAR(20) NOT NULL,         -- PRODUCER / CONSUMER
    connector_class VARCHAR(255),               -- Java class for Kafka Connector
    description VARCHAR(500),
    use_yn VARCHAR(1) DEFAULT 'Y',
    ordering INT DEFAULT 0,
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (template_id)
);

-- 2. KFK_PARAM: Connector Execution Parameters (Linked to Templates)
CREATE TABLE KFK_PARAM (
    template_id VARCHAR(50) NOT NULL,
    param_key VARCHAR(100) NOT NULL,            -- e.g., 'topic.name', 'bootstrap.servers'
    param_value VARCHAR(500),
    description VARCHAR(500),
    ordering INT DEFAULT 0,
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (template_id, param_key),
    FOREIGN KEY (template_id) REFERENCES KFK_TEMPLATE(template_id) ON DELETE CASCADE
);

-- 3. KFK_MAPPING: Dynamic Data Transformation Rules (Linked to Migration List)
CREATE TABLE KFK_MAPPING (
    mig_list_seq VARCHAR(50) NOT NULL,          -- Linked to MIGRATION_LIST.MIG_LIST_SEQ
    mapping_name VARCHAR(100),
    transformation_json TEXT,                    -- SpEL based transformation rules
    description VARCHAR(500),
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (mig_list_seq)
);

-- 4. KFK_OFFSET: Offset Tracking (Linked to Migration List Partitioning)
CREATE TABLE KFK_OFFSET (
    mig_list_seq VARCHAR(50) NOT NULL,
    topic_name VARCHAR(100) NOT NULL,
    partition_id INT NOT NULL DEFAULT 0,
    current_offset BIGINT DEFAULT 0,
    last_timestamp VARCHAR(50),                 -- Max timestamp synced
    last_pk VARCHAR(100),                       -- Last processed PK value
    consumer_group VARCHAR(100),
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (mig_list_seq, topic_name, partition_id)
);

-- Note: These tables are for the 'mig-controller' MariaDB database.
