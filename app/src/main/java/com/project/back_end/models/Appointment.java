package com.project.back_end.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class Appointment {

    // ---- Status codes ----
    public static final int STATUS_SCHEDULED = 0;
    public static final int STATUS_COMPLETED = 1;


    // @Entity annotation:
    //    - Marks the class as a JPA entity, meaning it represents a table in the database.
    //    - Required for persistence frameworks (e.g., Hibernate) to map the class to a database table.

    // 1. 'id' field:
    //    - Type: private Long
    //    - Description:
    //      - Represents the unique identifier for each appointment.
    //      - The @Id annotation marks it as the primary key.
    //      - The @GeneratedValue(strategy = GenerationType.IDENTITY) annotation auto-generates the ID value when a new record is inserted into the database.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 2. 'doctor' field:
    //    - Type: private Doctor
    //    - Description:
    //      - Represents the doctor assigned to this appointment.
    //      - The @ManyToOne annotation defines the relationship, indicating many appointments can be linked to one doctor.
    //      - The @NotNull annotation ensures that an appointment must be associated with a doctor when created.

    @NotNull(message = "Doctor is required")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // 3. 'patient' field:
    //    - Type: private Patient
    //    - Description:
    //      - Represents the patient assigned to this appointment.
    //      - The @ManyToOne annotation defines the relationship, indicating many appointments can be linked to one patient.
    //      - The @NotNull annotation ensures that an appointment must be associated with a patient when created.

    @NotNull(message = "Patient is required")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // 4. 'appointmentTime' field:
    //    - Type: private LocalDateTime
    //    - Description:
    //      - Represents the date and time when the appointment is scheduled to occur.
    //      - The @Future annotation ensures that the appointment time is always in the future when the appointment is created.
    //      - It uses LocalDateTime, which includes both the date and time for the appointment.

    @NotNull
    @Future(message = "Appointment time must be in the future")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime appointmentTime;

    // 5. 'status' field:
    //    - Type: private int
    //    - Description:
    //      - Represents the current status of the appointment. It is an integer where:
    //        - 0 means the appointment is scheduled.
    //        - 1 means the appointment has been completed.
    //      - The @NotNull annotation ensures that the status field is not null.

    @NotNull
    @Min(value = STATUS_SCHEDULED, message = "Invalid status")
    @Max(value = STATUS_COMPLETED, message = "Invalid status")
    @Column(nullable = false)
    private Integer status; // 0 = Scheduled, 1 = Completed


    // 6. 'getEndTime' method:
    //    - Type: private LocalDateTime
    //    - Description:
    //      - This method is a transient field (not persisted in the database).
    //      - It calculates the end time of the appointment by adding one hour to the start time (appointmentTime).
    //      - It is used to get an estimated appointment end time for display purposes.

    @Transient
    public LocalDateTime getEndTime() {
        return appointmentTime != null ? appointmentTime.plusHours(1) : null;
    }

    // 7. 'getAppointmentDate' method:
    //    - Type: private LocalDate
    //    - Description:
    //      - This method extracts only the date part from the appointmentTime field.
    //      - It returns a LocalDate object representing just the date (without the time) of the scheduled appointment.

    @Transient
    public LocalDate getAppointmentDate() {
        return appointmentTime != null ? appointmentTime.toLocalDate() : null;
    }

    // 8. 'getAppointmentTimeOnly' method:
    //    - Type: private LocalTime
    //    - Description:
    //      - This method extracts only the time part from the appointmentTime field.
    //      - It returns a LocalTime object representing just the time (without the date) of the scheduled appointment.

    @Transient
    public LocalTime getAppointmentTimeOnly() {
        return appointmentTime != null ? appointmentTime.toLocalTime() : null;
    }

    @Size(max = 200)
    @Column(name = "reason_for_visit", length = 200)
    private String reasonForVisit;

    @Size(max = 500)
    @Column(name = "notes", length = 500)
    private String notes;

    // 9. Constructor(s):
    //    - A no-argument constructor is implicitly provided by JPA for entity creation.
    //    - A parameterized constructor can be added as needed to initialize fields.

    protected Appointment() {
    }

    public Appointment(Doctor doctor, Patient patient, LocalDateTime appointmentTime, int status) {
        this.doctor = doctor;
        this.patient = patient;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    // 10. Getters and Setters:
    //    - Standard getter and setter methods are provided for accessing and modifying the fields: id, doctor, patient, appointmentTime, status, etc.

    public Long getId() {
        return id;
    }

    public @NotNull Doctor getDoctor() {
        return doctor;
    }

    public @Future(message = "Appointment time must be in the future") LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    @NotNull
    public int getStatus() {
        return status;
    }

    public @NotNull(message = "Patient is required") Patient getPatient() {
        return patient;
    }

    public @Size(max = 200) String getReasonForVisit() {
        return reasonForVisit;
    }

    public @Size(max = 500) String getNotes() {
        return notes;
    }

    // Setters

    public void setDoctor(@NotNull Doctor doctor) {
        this.doctor = doctor;
    }

    public void setAppointmentTime(@Future(message = "Appointment time must be in the future") LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public void setStatus(@NotNull int status) {
        this.status = status;
    }

    public void setNotes(@Size(max = 500) String notes) {
        this.notes = notes;
    }

    public void setReasonForVisit(@Size(max = 200) String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }

    public void setStatus(@NotNull @Min(value = STATUS_SCHEDULED, message = "Invalid status") @Max(value = STATUS_COMPLETED, message = "Invalid status") Integer status) {
        this.status = status;
    }

    public void setPatient(@NotNull(message = "Patient is required") Patient patient) {
        this.patient = patient;
    }

    @Transient
    public boolean isCompleted() {
        return Integer.valueOf(STATUS_COMPLETED).equals(status);
    }

    // True if the appointment is scheduled (not completed).
    @Transient
    public boolean isScheduled() {
        return Integer.valueOf(STATUS_SCHEDULED).equals(status);
    }
}

