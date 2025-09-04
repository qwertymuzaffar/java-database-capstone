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
```

## 2. Doctors Table
```sql
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
```

## 3. Appointments Table
```sql
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
```

## 4. Admin Users Table
```sql
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
```

## MongoDB Collection Design

### Collection: prescriptions

```json
{
  "_id": { "$oid": "676a9e4f8e6c1c00125e8a11" },
  "patient_id": 1023,             // references MySQL patients.id
  "doctor_id": 87,                // references MySQL doctors.id
  "appointment_id": 5519,         // references MySQL appointments.id
  "issued_at_utc": "2025-09-01T13:45:00Z",
  "notes": "Start with a low dose. Reassess in 2 weeks. Watch for dizziness.",
  "items": [
    {
      "drug_name": "Amoxicillin",
      "dosage": "500 mg",
      "route": "oral",
      "frequency": "TID",
      "duration_days": 7,
      "instructions": "Take with food",
      "substitutions_allowed": true
    },
    {
      "drug_name": "Ibuprofen",
      "dosage": "200 mg",
      "route": "oral",
      "frequency": "PRN",
      "duration_days": 5,
      "max_per_day": 4
    }
  ],
  "tags": ["upper-respiratory", "antibiotic", "adult"],
  "status": {
    "is_fulfilled": false,
    "fulfilled_at_utc": null,
    "pharmacy": null,
    "refills_remaining": 1
  },
  "metadata": {
    "version": 3,
    "created_by": "doctor:87",
    "updated_by": "doctor:87",
    "updated_at_utc": "2025-09-01T13:45:00Z",
    "source": "web-portal"
  },
  "attachments": [
    {
      "type": "pdf",
      "title": "patient-instructions.pdf",
      "gridfs_id": "66a9e4f8e6c1c00125e8b22",
      "size_bytes": 184532
    }
  ],
  "audit": [
    {
      "at_utc": "2025-09-01T13:45:00Z",
      "actor": "doctor:87",
      "action": "CREATED"
    }
  ]
}
