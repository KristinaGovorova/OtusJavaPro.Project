CREATE SEQUENCE IF NOT EXISTS passport_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS student_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS transcript_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE passport
(
    id              BIGINT       NOT NULL DEFAULT nextval('passport_seq'),
    passport_number VARCHAR(255) NOT NULL,
    issue_date      date         NOT NULL,
    CONSTRAINT pk_passport PRIMARY KEY (id)
);

CREATE TABLE student
(
    id              BIGINT       NOT NULL DEFAULT nextval('student_seq'),
    first_name      VARCHAR(255) NOT NULL,
    last_name       VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    enrollment_date date         NOT NULL,
    avatar          OID,
    version         BIGINT,
    passport_id     BIGINT       NOT NULL,
    CONSTRAINT pk_student PRIMARY KEY (id)
);

CREATE TABLE transcript
(
    id         BIGINT       NOT NULL DEFAULT nextval('transcript_seq'),
    subject    VARCHAR(255) NOT NULL,
    grade      INTEGER      NOT NULL,
    student_id BIGINT       NOT NULL,
    CONSTRAINT pk_transcript PRIMARY KEY (id)
);

ALTER TABLE student
    ADD CONSTRAINT uc_student_passport UNIQUE (passport_id);

ALTER TABLE student
    ADD CONSTRAINT FK_STUDENT_ON_PASSPORT FOREIGN KEY (passport_id) REFERENCES passport (id);

ALTER TABLE transcript
    ADD CONSTRAINT FK_TRANSCRIPT_ON_STUDENT FOREIGN KEY (student_id) REFERENCES student (id);