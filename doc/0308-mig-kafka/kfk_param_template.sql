-- Kafka Parameter Template Management
DROP TABLE IF EXISTS KFK_MIG_PARAM;
DROP TABLE IF EXISTS KFK_MIG_LIST;
DROP TABLE IF EXISTS KFK_PARAM_TEMPLATE;

CREATE TABLE KFK_PARAM_TEMPLATE (
    id VARCHAR(50) NOT NULL,
    param_name VARCHAR(100),
    input_method VARCHAR(20),       -- TEXT, COMBO, AUTO, VIEWTEXT
    param_explain VARCHAR(500),
    param_key VARCHAR(100),         -- Resulting key in Connector Config
    par_class_id VARCHAR(255),      -- Connector class this param belongs to
    dp_level INT DEFAULT 0,         -- Level 0, 1, 2, 3
    dp_order INT DEFAULT 0,         -- Ordering in the UI
    hidden_yn VARCHAR(1) DEFAULT 'N',
    group_cd VARCHAR(50),           -- Common code group for COMBO
    par_param_id VARCHAR(50),       -- Parent template ID for dependencies
    par_column_key VARCHAR(50),     -- Value mapping (e.g. ec1, ec2)
    column_type VARCHAR(20),        -- string, int
    required_yn VARCHAR(1) DEFAULT 'N',
    default_value VARCHAR(500),
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- Results of the Wizard
CREATE TABLE KFK_MIG_LIST (
    mig_list_seq VARCHAR(50) NOT NULL,
    mig_master VARCHAR(50) NOT NULL,
    mig_name VARCHAR(100),
    registration_type VARCHAR(20) DEFAULT 'BOTH', -- BOTH, SOURCE_ONLY, SINK_ONLY
    source_connector VARCHAR(255),
    sink_connector VARCHAR(255),
    use_yn VARCHAR(1) DEFAULT 'Y',
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (mig_list_seq)
);

CREATE TABLE KFK_MIG_PARAM (
    mig_list_seq VARCHAR(50) NOT NULL,
    connector_type VARCHAR(10) NOT NULL, -- SOURCE, SINK, COMMON
    param_key VARCHAR(100) NOT NULL,
    param_value TEXT,
    dp_level INT,
    dp_order INT,
    PRIMARY KEY (mig_list_seq, connector_type, param_key)
);
