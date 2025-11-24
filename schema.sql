-- ============================================================
-- PostgreSQL DDL Generated from JPA Entity Classes
-- Caring Note API Server
-- Generated: 2025-11-18
-- ============================================================

-- Drop tables if exists (in reverse dependency order)
DROP TABLE IF EXISTS medication_contraindications CASCADE;
DROP TABLE IF EXISTS prompt_learnings CASCADE;
DROP TABLE IF EXISTS prompt_templates CASCADE;
DROP TABLE IF EXISTS ai_counsel_summarys CASCADE;
DROP TABLE IF EXISTS tus_file_info CASCADE;
DROP TABLE IF EXISTS session_record CASCADE;
DROP TABLE IF EXISTS counselee_consents CASCADE;
DROP TABLE IF EXISTS medication_counsels CASCADE;
DROP TABLE IF EXISTS waste_medication_records CASCADE;
DROP TABLE IF EXISTS waste_medication_disposals CASCADE;
DROP TABLE IF EXISTS medication_records_hist CASCADE;
DROP TABLE IF EXISTS medication_records CASCADE;
DROP TABLE IF EXISTS counsel_cards CASCADE;
DROP TABLE IF EXISTS counsel_sessions CASCADE;
DROP TABLE IF EXISTS medications CASCADE;
DROP TABLE IF EXISTS counselees CASCADE;
DROP TABLE IF EXISTS counselors CASCADE;

-- ============================================================
-- Core Tables
-- ============================================================

-- Counselors (상담사)
CREATE TABLE counselors (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- Counselor specific fields
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    phone_number VARCHAR(50) UNIQUE,
    username VARCHAR(255) UNIQUE,
    profile_image_url VARCHAR(500),
    google_sso_id VARCHAR(255) UNIQUE,
    apple_sso_id VARCHAR(255) UNIQUE,
    registration_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, INACTIVE
    role_type VARCHAR(50),  -- ROLE_ADMIN, ROLE_USER, ROLE_ASSISTANT, ROLE_NONE
    medication_counseling_count INTEGER DEFAULT 0 CHECK (medication_counseling_count >= 0),
    counseled_counselee_count INTEGER DEFAULT 0 CHECK (counseled_counselee_count >= 0),
    participation_days INTEGER DEFAULT 0 CHECK (participation_days >= 0),
    description TEXT,

    CONSTRAINT chk_phone_number_format CHECK (phone_number ~ '^\d{2,3}-\d{3,4}-\d{4}$')
);

CREATE INDEX idx_counselor_status ON counselors(status);
CREATE INDEX idx_counselor_email ON counselors(email);

-- Counselees (내담자)
CREATE TABLE counselees (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- Counselee specific fields
    name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    phone_number VARCHAR(50),
    counsel_count INTEGER DEFAULT 0 CHECK (counsel_count >= 0),
    last_counsel_date DATE,
    registration_date DATE NOT NULL,
    affiliated_welfare_institution VARCHAR(255),
    note TEXT,
    gender_type VARCHAR(50),  -- MALE, FEMALE, ELSE
    health_insurance_type VARCHAR(50),  -- HEALTH_INSURANCE, MEDICAL_AID, VETERANS_BENEFITS, NON_COVERED
    address VARCHAR(500),
    is_disability BOOLEAN,
    care_manager_name VARCHAR(255),

    CONSTRAINT chk_counselee_phone_format CHECK (phone_number ~ '^\d{2,3}-\d{3,4}-\d{4}$')
);

CREATE INDEX idx_counselee_name ON counselees(name);
CREATE INDEX idx_counselee_phone ON counselees(phone_number);

-- Medications (약물)
CREATE TABLE medications (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- Medication specific fields
    item_name VARCHAR(255) NOT NULL,
    item_name_chosung VARCHAR(255) NOT NULL,
    item_seq INTEGER NOT NULL,
    entp_name VARCHAR(255),
    item_image VARCHAR(3000),
    chart VARCHAR(3000),
    print_front VARCHAR(255),
    print_back VARCHAR(255),
    drug_shape VARCHAR(255),
    color_class1 VARCHAR(255),
    color_class2 VARCHAR(255),
    line_front VARCHAR(255),
    line_back VARCHAR(255),
    leng_long REAL,
    leng_short REAL,
    thick REAL,
    img_regist_ts DATE,
    class_no INTEGER,
    class_name VARCHAR(255),
    etc_otc_name VARCHAR(255),
    form_code_name VARCHAR(255),
    mark_code_front_anal VARCHAR(255),
    mark_code_back_anal VARCHAR(255),
    mark_code_front VARCHAR(255),
    mark_code_back VARCHAR(255),
    mark_code_front_img VARCHAR(500),
    mark_code_back_img VARCHAR(500),
    item_eng_name VARCHAR(255),
    item_permit_date DATE,
    edi_code VARCHAR(255),
    days_until_discard INTEGER CHECK (days_until_discard >= 0)
);

CREATE INDEX idx_item_name ON medications(item_name);
CREATE INDEX idx_item_name_chosung ON medications(item_name_chosung);

-- Medication Contraindications (약물 금기 사항 - 다대다)
CREATE TABLE medication_contraindications (
    medication_id VARCHAR(26) NOT NULL,
    contraindicated_medication_id VARCHAR(26) NOT NULL,

    PRIMARY KEY (medication_id, contraindicated_medication_id),

    CONSTRAINT fk_medication_contraindications_med1
        FOREIGN KEY (medication_id)
        REFERENCES medications(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_medication_contraindications_med2
        FOREIGN KEY (contraindicated_medication_id)
        REFERENCES medications(id)
        ON DELETE CASCADE
);

-- ============================================================
-- Counsel Session Tables
-- ============================================================

-- Counsel Sessions (상담 세션)
CREATE TABLE counsel_sessions (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- CounselSession specific fields
    counselor_id VARCHAR(26),
    counselee_id VARCHAR(26),
    scheduled_start_datetime TIMESTAMP NOT NULL,
    start_datetime TIMESTAMP,
    end_datetime TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',  -- SCHEDULED, IN_PROGRESS, COMPLETED, CANCELED
    session_number INTEGER,

    CONSTRAINT fk_counsel_session_counselor
        FOREIGN KEY (counselor_id)
        REFERENCES counselors(id),

    CONSTRAINT fk_counsel_session_counselee
        FOREIGN KEY (counselee_id)
        REFERENCES counselees(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_counsel_session_counselee_datetime
        UNIQUE (counselee_id, scheduled_start_datetime)
);

CREATE INDEX idx_counsel_session_status ON counsel_sessions(status);
CREATE INDEX idx_counsel_session_counselor ON counsel_sessions(counselor_id);
CREATE INDEX idx_counsel_session_counselee ON counsel_sessions(counselee_id);

-- Counsel Cards (상담 카드)
CREATE TABLE counsel_cards (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- CounselCard specific fields
    counsel_session_id VARCHAR(26) NOT NULL,
    card_record_status VARCHAR(50),  -- NOT_STARTED, IN_PROGRESS, COMPLETED

    -- Embedded: CounselPurposeAndNote
    counsel_purpose TEXT,
    counsel_note TEXT,

    -- Embedded: Allergy
    allergy_has BOOLEAN,
    allergy_detail TEXT,

    -- Embedded: DiseaseInfo
    disease_has BOOLEAN,
    disease_detail TEXT,

    -- Embedded: MedicationSideEffect
    medication_side_effect_has BOOLEAN,
    medication_side_effect_detail TEXT,

    -- Embedded: Drinking
    drinking_has BOOLEAN,
    drinking_detail TEXT,

    -- Embedded: Exercise
    exercise_has BOOLEAN,
    exercise_detail TEXT,

    -- Embedded: MedicationManagement
    medication_management_has BOOLEAN,
    medication_management_detail TEXT,

    -- Embedded: Nutrition
    nutrition_has BOOLEAN,
    nutrition_detail TEXT,

    -- Embedded: Smoking
    smoking_has BOOLEAN,
    smoking_detail TEXT,

    -- Embedded: Communication
    communication_type VARCHAR(50),
    communication_detail TEXT,

    -- Embedded: Evacuation
    evacuation_type VARCHAR(50),
    evacuation_detail TEXT,

    -- Embedded: Walking
    walking_type VARCHAR(50),
    walking_detail TEXT,

    CONSTRAINT fk_counsel_card_session
        FOREIGN KEY (counsel_session_id)
        REFERENCES counsel_sessions(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_counsel_card_session ON counsel_cards(counsel_session_id);

-- Medication Records (약물 기록)
CREATE TABLE medication_records (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- MedicationRecord specific fields
    counselee_id VARCHAR(26) NOT NULL,
    medication_id VARCHAR(26) NOT NULL,
    dosage VARCHAR(255),
    administration_date_time TIMESTAMP,
    route_of_administration VARCHAR(255),
    prescribing_counselor_id VARCHAR(26),
    notes TEXT,
    prescription_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    remaining_quantity INTEGER DEFAULT 0 CHECK (remaining_quantity >= 0),

    CONSTRAINT fk_medication_record_counselee
        FOREIGN KEY (counselee_id)
        REFERENCES counselees(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_medication_record_medication
        FOREIGN KEY (medication_id)
        REFERENCES medications(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_medication_record_counselor
        FOREIGN KEY (prescribing_counselor_id)
        REFERENCES counselors(id)
);

CREATE INDEX idx_medication_record_counselee ON medication_records(counselee_id);
CREATE INDEX idx_medication_record_medication ON medication_records(medication_id);

-- Medication Records History (약물 기록 이력)
CREATE TABLE medication_records_hist (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- MedicationRecordHist specific fields
    counsel_session_id VARCHAR(26) NOT NULL,
    medication_division VARCHAR(50),  -- PRESCRIPTION, OTC
    medication_id VARCHAR(26),
    name VARCHAR(255),
    usage_object VARCHAR(255),
    prescription_date DATE,
    prescription_days INTEGER,
    unit VARCHAR(50),
    usage_status VARCHAR(50),  -- REGULAR, AS_NEEDED, STOPPED

    CONSTRAINT fk_medication_record_hist_session
        FOREIGN KEY (counsel_session_id)
        REFERENCES counsel_sessions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_medication_record_hist_medication
        FOREIGN KEY (medication_id)
        REFERENCES medications(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_medication_record_hist_session ON medication_records_hist(counsel_session_id);

-- Waste Medication Records (폐의약품 기록)
CREATE TABLE waste_medication_records (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- WasteMedicationRecord specific fields
    cousel_session_id VARCHAR(26) NOT NULL,
    medication_id VARCHAR(26),
    medication_name VARCHAR(255),
    unit INTEGER,
    disposal_reason TEXT,

    CONSTRAINT fk_waste_medication_session
        FOREIGN KEY (cousel_session_id)
        REFERENCES counsel_sessions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_waste_medication_medication
        FOREIGN KEY (medication_id)
        REFERENCES medications(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_waste_medication_session ON waste_medication_records(cousel_session_id);

-- Waste Medication Disposals (폐의약품 처리)
CREATE TABLE waste_medication_disposals (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- WasteMedicationDisposal specific fields
    counsel_session_id VARCHAR(26) NOT NULL,
    unused_reasons TEXT,  -- LIST<String> stored as TEXT (JSON or comma-separated)
    unused_reason_detail VARCHAR(500),
    drug_remain_action_type VARCHAR(50),  -- DOCTOR_OR_PHARMACIST, SELF_DECISION, NONE
    drug_remain_action_detail VARCHAR(500),
    recovery_agreement_type VARCHAR(50),  -- AGREE, DISAGREE
    waste_medication_gram INTEGER,

    CONSTRAINT fk_waste_disposal_session
        FOREIGN KEY (counsel_session_id)
        REFERENCES counsel_sessions(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_waste_disposal_session ON waste_medication_disposals(counsel_session_id);

-- Medication Counsels (복약 상담)
CREATE TABLE medication_counsels (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- MedicationCounsel specific fields
    counsel_session_id VARCHAR(26) NOT NULL,
    counsel_record TEXT,

    CONSTRAINT fk_medication_counsel_session
        FOREIGN KEY (counsel_session_id)
        REFERENCES counsel_sessions(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_medication_counsel_session ON medication_counsels(counsel_session_id);

-- Counselee Consents (내담자 동의)
CREATE TABLE counselee_consents (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- CounseleeConsent specific fields
    counsel_session_id VARCHAR(26) NOT NULL,
    counselee_id VARCHAR(26) NOT NULL,
    consent_date_time TIMESTAMP,
    is_consent BOOLEAN NOT NULL DEFAULT false,

    CONSTRAINT fk_counselee_consent_session
        FOREIGN KEY (counsel_session_id)
        REFERENCES counsel_sessions(id),

    CONSTRAINT fk_counselee_consent_counselee
        FOREIGN KEY (counselee_id)
        REFERENCES counselees(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_counselee_consent_session_counselee
        UNIQUE (counsel_session_id, counselee_id)
);

CREATE INDEX idx_counselee_consent_session ON counselee_consents(counsel_session_id);

-- ============================================================
-- TUS File Upload Tables
-- ============================================================

-- Session Record (녹음 세션 기록)
CREATE TABLE session_record (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- SessionRecord specific fields
    counsel_session_id VARCHAR(26) NOT NULL,
    duration BIGINT,

    CONSTRAINT fk_session_record_session
        FOREIGN KEY (counsel_session_id)
        REFERENCES counsel_sessions(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_session_record_session ON session_record(counsel_session_id);

-- TUS File Info (TUS 파일 정보)
CREATE TABLE tus_file_info (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- TusFileInfo specific fields
    session_record_id VARCHAR(26) NOT NULL,
    content_offset BIGINT,
    saved_name VARCHAR(26),

    CONSTRAINT fk_tus_file_session_record
        FOREIGN KEY (session_record_id)
        REFERENCES session_record(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_tus_file_session_record ON tus_file_info(session_record_id);

-- ============================================================
-- AI & Prompt Tables
-- ============================================================

-- AI Counsel Summary (AI 상담 요약)
CREATE TABLE ai_counsel_summarys (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- AICounselSummary specific fields
    counsel_session_id VARCHAR(26) NOT NULL,
    stt_result TEXT,  -- JSON stored as TEXT
    ta_result TEXT,  -- JSON stored as TEXT
    ai_counsel_summary_status VARCHAR(50),  -- STT_PROGRESS, STT_FAILED, STT_COMPLETE, GPT_PROGRESS, GPT_COMPLETE, GPT_FAILED
    speakers TEXT,  -- LIST<String> stored as TEXT

    CONSTRAINT fk_ai_summary_session
        FOREIGN KEY (counsel_session_id)
        REFERENCES counsel_sessions(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_ai_summary_session ON ai_counsel_summarys(counsel_session_id);
CREATE INDEX idx_ai_summary_status ON ai_counsel_summarys(ai_counsel_summary_status);

-- Prompt Templates (프롬프트 템플릿)
CREATE TABLE prompt_templates (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- PromptTemplate specific fields
    name VARCHAR(255),
    description TEXT,
    prompt_template_type VARCHAR(50) NOT NULL,  -- USER, SYSTEM
    prompt_text TEXT NOT NULL
);

CREATE INDEX idx_prompt_template_type ON prompt_templates(prompt_template_type);

-- Prompt Learnings (프롬프트 학습 데이터)
CREATE TABLE prompt_learnings (
    -- BaseEntity fields
    id VARCHAR(26) PRIMARY KEY,
    created_datetime TIMESTAMP,
    updated_datetime TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    -- PromptLearning specific fields
    prompt_template_id VARCHAR(26) NOT NULL,
    learning_input_text TEXT NOT NULL,
    learning_output_text TEXT NOT NULL,

    CONSTRAINT fk_prompt_learning_template
        FOREIGN KEY (prompt_template_id)
        REFERENCES prompt_templates(id)
);

CREATE INDEX idx_prompt_learning_template ON prompt_learnings(prompt_template_id);

-- ============================================================
-- Comments and Documentation
-- ============================================================

COMMENT ON TABLE counselors IS '상담사 테이블';
COMMENT ON TABLE counselees IS '내담자 테이블';
COMMENT ON TABLE medications IS '약물 정보 테이블';
COMMENT ON TABLE counsel_sessions IS '상담 세션 테이블';
COMMENT ON TABLE counsel_cards IS '상담 카드 테이블';
COMMENT ON TABLE medication_records IS '약물 복용 기록 테이블';
COMMENT ON TABLE medication_records_hist IS '약물 복용 이력 테이블';
COMMENT ON TABLE waste_medication_records IS '폐의약품 기록 테이블';
COMMENT ON TABLE waste_medication_disposals IS '폐의약품 처리 테이블';
COMMENT ON TABLE medication_counsels IS '복약 상담 기록 테이블';
COMMENT ON TABLE counselee_consents IS '내담자 동의 테이블';
COMMENT ON TABLE session_record IS '상담 세션 녹음 기록 테이블';
COMMENT ON TABLE tus_file_info IS 'TUS 파일 업로드 정보 테이블';
COMMENT ON TABLE ai_counsel_summarys IS 'AI 상담 요약 테이블';
COMMENT ON TABLE prompt_templates IS 'AI 프롬프트 템플릿 테이블';
COMMENT ON TABLE prompt_learnings IS 'AI 프롬프트 학습 데이터 테이블';

-- ============================================================
-- BaseEntity Common Fields Info
-- ============================================================
-- All tables include the following common fields from BaseEntity:
--   id                 VARCHAR(26)  - ULID generated primary key
--   created_datetime   TIMESTAMP    - Record creation timestamp
--   updated_datetime   TIMESTAMP    - Record last update timestamp
--   created_by         VARCHAR(255) - User who created the record
--   updated_by         VARCHAR(255) - User who last updated the record
-- ============================================================
