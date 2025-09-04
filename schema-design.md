## MySQL Database Design

This document defines the **relational schema** for the clinic system.  
It covers the **core operational data** including patients, doctors, appointments, administrators, clinic locations, and payments.  

---

## 1. Patients Table

```sql
CREATE TABLE patients (
  patient_id       BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  first_name       VARCHAR(80)       NOT NULL,
  last_name        VARCHAR(80)       NOT NULL,
  email            VARCHAR(255)      NOT NULL,
  phone            VARCHAR(30),
  date_of_birth    DATE              NOT NULL,
  gender           ENUM('M','F','X') NULL,
  address_line1    VARCHAR(200),
  address_line2    VARCHAR(200),
  city             VARCHAR(120),
  state_province   VARCHAR(120),
  postal_code      VARCHAR(20),
  created_at       TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_patients_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

## 2. Doctors Table

CREATE TABLE doctors (
  doctor_id        BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  first_name       VARCHAR(80)       NOT NULL,
  last_name        VARCHAR(80)       NOT NULL,
  email            VARCHAR(255)      NOT NULL,
  phone            VARCHAR(30),
  specialization   VARCHAR(120)      NOT NULL,
  license_number   VARCHAR(100)      NOT NULL,
  clinic_location_id BIGINT UNSIGNED NULL,
  is_active        TINYINT(1)        NOT NULL DEFAULT 1,
  created_at       TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_doctors_email UNIQUE (email),
  CONSTRAINT uq_doctors_license UNIQUE (license_number),
  CONSTRAINT fk_doctors_location
    FOREIGN KEY (clinic_location_id) REFERENCES clinic_locations(clinic_location_id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

## 3. Appointments Table

CREATE TABLE appointments (
  appointment_id   BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  patient_id       BIGINT UNSIGNED  NOT NULL,
  doctor_id        BIGINT UNSIGNED  NOT NULL,
  start_time_utc   DATETIME         NOT NULL,
  status           ENUM('BOOKED','CANCELLED','COMPLETED','NO_SHOW') NOT NULL DEFAULT 'BOOKED',
  reason           VARCHAR(500),
  created_at       TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_appt_patient
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_appt_doctor
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  -- Prevent double booking of doctors
  CONSTRAINT uq_doctor_slot UNIQUE (doctor_id, start_time_utc),

  INDEX idx_appt_patient_time (patient_id, start_time_utc),
  INDEX idx_appt_doctor_time (doctor_id, start_time_utc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

## 4. Admin Users Table

CREATE TABLE admin_users (
  admin_id         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  username         VARCHAR(80)       NOT NULL,
  email            VARCHAR(255)      NOT NULL,
  password_hash    VARCHAR(255)      NOT NULL,
  role             ENUM('ADMIN','SUPER_ADMIN') NOT NULL DEFAULT 'ADMIN',
  last_login_at    DATETIME          NULL,
  is_active        TINYINT(1)        NOT NULL DEFAULT 1,
  created_at       TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_admin_username UNIQUE (username),
  CONSTRAINT uq_admin_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 

## MongoDB Collection Design
