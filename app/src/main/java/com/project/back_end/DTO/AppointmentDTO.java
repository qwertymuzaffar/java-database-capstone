package com.project.back_end.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AppointmentDTO {
    // 1. 'id' field:
    //    - Type: private Long
    //    - Description:
    //      - Represents the unique identifier for the appointment.
    //      - This is the primary key for identifying the appointment in the system.
    private Long id;

    // 2. 'doctorId' field:
    //    - Type: private Long
    //    - Description:
    //      - Represents the ID of the doctor associated with the appointment.
    //      - This is a simplified field, capturing only the ID of the doctor (not the full Doctor object).
    private Long doctorId;

    // 3. 'doctorName' field:
    //    - Type: private String
    //    - Description:
    //      - Represents the name of the doctor associated with the appointment.
    //      - This is a simplified field for displaying the doctor's name.
    private String doctorName;

    // 4. 'patientId' field:
    //    - Type: private Long
    //    - Description:
    //      - Represents the ID of the patient associated with the appointment.
    //      - This is a simplified field, capturing only the ID of the patient (not the full Patient object).
    private Long patientId;

    // 5. 'patientName' field:
    //    - Type: private String
    //    - Description:
    //      - Represents the name of the patient associated with the appointment.
    //      - This is a simplified field for displaying the patient's name.
    private String patientName;

    // 6. 'patientEmail' field:
    //    - Type: private String
    //    - Description:
    //      - Represents the email of the patient associated with the appointment.
    //      - This is a simplified field for displaying the patient's email.
    private String patientEmail;

    // 7. 'patientPhone' field:
    //    - Type: private String
    //    - Description:
    //      - Represents the phone number of the patient associated with the appointment.
    //      - This is a simplified field for displaying the patient's phone number.
    private String patientPhone;

    // 8. 'patientAddress' field:
    //    - Type: private String
    //    - Description:
    //      - Represents the address of the patient associated with the appointment.
    //      - This is a simplified field for displaying the patient's address.
    private String patientAddress;

    // 9. 'appointmentTime' field:
    //    - Type: private LocalDateTime
    //    - Description:
    //      - Represents the scheduled date and time of the appointment.
    //      - The time when the appointment is supposed to happen, stored as a LocalDateTime object.
    private LocalDateTime appointmentTime;

    // 10. 'status' field:
    //    - Type: private int
    //    - Description:
    //      - Represents the status of the appointment.
    //      - Status can indicate if the appointment is "Scheduled:0", "Completed:1", or other statuses (e.g., "Canceled") as needed.
    private int status;

    // 11. 'appointmentDate' field (Custom Getter):
    //    - Type: private LocalDate
    //    - Description:
    //      - A derived field representing only the date part of the appointment (without the time).
    //      - Extracted from the 'appointmentTime' field.
    private LocalDate appointmentDate;

    // 12. 'appointmentTimeOnly' field (Custom Getter):
    //    - Type: private LocalTime
    //    - Description:
    //      - A derived field representing only the time part of the appointment (without the date).
    //      - Extracted from the 'appointmentTime' field.
    private LocalTime appointmentTimeOnly;

    // 13. 'endTime' field (Custom Getter):
    //    - Type: private LocalDateTime
    //    - Description:
    //      - A derived field representing the end time of the appointment.
    //      - Calculated by adding 1 hour to the 'appointmentTime' field.
    private LocalDateTime endTime;

    // 14. Constructor:
    //    - The constructor accepts all the relevant fields for the AppointmentDTO,
    //    including simplified fields for the doctor and patient (ID, name, etc.).
    //    - It also calculates custom fields: 'appointmentDate', 'appointmentTimeOnly', and 'endTime' based on the 'appointmentTime' field.

    public AppointmentDTO() {
    }

    public AppointmentDTO(int status, LocalDateTime appointmentTime, String patientAddress, String patientPhone, String patientEmail, String patientName, long patientId, String doctorName, long doctorId, long id) {
        this.endTime = appointmentTime.plusHours(1);
        this.appointmentTimeOnly = appointmentTime.toLocalTime();
        this.appointmentDate = appointmentTime.toLocalDate();
        this.status = status;
        this.appointmentTime = appointmentTime;
        this.patientAddress = patientAddress;
        this.patientPhone = patientPhone;
        this.patientEmail = patientEmail;
        this.patientName = patientName;
        this.patientId = patientId;
        this.doctorName = doctorName;
        this.doctorId = doctorId;
        this.id = id;
    }

    // 15. Getters:
    //    - Standard getter methods are provided for all fields: id, doctorId, doctorName, patientId, patientName, patientEmail, patientPhone, patientAddress, appointmentTime, status, appointmentDate, appointmentTimeOnly, and endTime.
    //    - These methods allow access to the values of the fields in the AppointmentDTO object.


    public long getId() {
        return id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public int getStatus() {
        return status;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getAppointmentTimeOnly() {
        return appointmentTimeOnly;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    // Setters

    public void setDoctorId(long doctorId) {
        this.doctorId = doctorId;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public void setPatientId(long patientId) {
        this.patientId = patientId;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public void setAppointmentTimeOnly(LocalTime appointmentTimeOnly) {
        this.appointmentTimeOnly = appointmentTimeOnly;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
