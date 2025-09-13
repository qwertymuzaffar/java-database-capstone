package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {
    // 1. **Add @Service Annotation**:
    //    - To indicate that this class is a service layer class for handling business logic.
    //    - The `@Service` annotation should be added before the class declaration to mark it as a Spring service component.
    //    - Instruction: Add `@Service` above the class definition.

    // 2. **Constructor Injection for Dependencies**:
    //    - The `AppointmentService` class requires several dependencies like `AppointmentRepository`, `Service`, `TokenService`, `PatientRepository`, and `DoctorRepository`.
    //    - These dependencies should be injected through the constructor.
    //    - Instruction: Ensure constructor injection is used for proper dependency management in Spring.

    private AppointmentRepository appointmentRepository;
    private PatientRepository patientRepository;
    private DoctorRepository doctorRepository;
    private CommonService commonService; // shared role/token logic
    private TokenService tokenService;     // token utilities

    public AppointmentService() {
    }

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              CommonService commonService,
                              TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.commonService = commonService;
        this.tokenService = tokenService;
    }


    // 3. **Add @Transactional Annotation for Methods that Modify Database**:
    //    - The methods that modify or update the database should be annotated with `@Transactional` to ensure atomicity and consistency of the operations.
    //    - Instruction: Add the `@Transactional` annotation above methods that interact with the database, especially those modifying data.

    // 4. **Book Appointment Method**:
    //    - Responsible for saving the new appointment to the database.
    //    - If the save operation fails, it returns `0`; otherwise, it returns `1`.
    //    - Instruction: Ensure that the method handles any exceptions and returns an appropriate result code.
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            Appointment saved = appointmentRepository.save(appointment);
            return (saved != null && saved.getId() != null) ? 1 : 0;
        } catch (DataAccessException dae) {
            System.out.println("DB error while booking appointment" + dae);;
            return 0;
        } catch (Exception e) {
            System.out.println("Unexpected error while booking appointment" + e);
            return 0;
        }
    }

    // 5. **Update Appointment Method**:
    //    - This method is used to update an existing appointment based on its ID.
    //    - It validates whether the patient ID matches, checks if the appointment is available for updating, and ensures that the doctor is available at the specified time.
    //    - If the update is successful, it saves the appointment; otherwise, it returns an appropriate error message.
    //    - Instruction: Ensure proper validation and error handling is included for appointment updates.

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> resp = new HashMap<>();

        // 1) Basic payload checks
        if (appointment == null || appointment.getId() == null) {
            resp.put("message", "Invalid payload: appointment id is required.");
            return ResponseEntity.badRequest().body(resp);
        }

        // 2) Check existence
        var existingOpt = appointmentRepository.findById(appointment.getId());
        if (existingOpt.isEmpty()) {
            resp.put("message", "Appointment not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }

        // 3) Validate requested update (ownership, time policy, doctor availability, etc.)
        // Assume validateAppointment returns a non-empty error message when invalid, otherwise null/empty

        Long appointmentId = appointment.getId();
        LocalDateTime requestedTime = appointment.getAppointmentTime();

        int validation = commonService.validateAppointment(appointmentId, requestedTime);
        if (validation == 0) {
            resp.put("message", "Requested slot is unavailable.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resp); // 409 is appropriate
        }

        // If your service returns boolean instead:
        // if (!service.validateAppointment(appointment)) { resp.put("message","Invalid appointment update."); return ResponseEntity.badRequest().body(resp); }

        // 4) Merge allowed fields & save
        var existing = existingOpt.get();
        existing.setAppointmentTime(appointment.getAppointmentTime());
        existing.setDoctor(appointment.getDoctor());
        existing.setPatient(appointment.getPatient());
        existing.setStatus(appointment.getStatus());
        existing.setNotes(appointment.getNotes());

        try {
            appointmentRepository.save(existing);
            resp.put("message", "Appointment updated successfully.");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("message", "Failed to update appointment. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    // 6. **Cancel Appointment Method**:
    //    - This method cancels an appointment by deleting it from the database.
    //    - It ensures the patient who owns the appointment is trying to cancel it and handles possible errors.
    //    - Instruction: Make sure that the method checks for the patient ID match before deleting the appointment.
    public ResponseEntity<Map<String, String>> cancelAppointment(Long appointmentId, Long patientId) {
        Map<String, String> resp = new HashMap<>();
        try {
            // Try to delete only if this appointment belongs to the patient
            int rows = appointmentRepository.deleteByIdAndPatient_Id(appointmentId, patientId);
            if (rows > 0) {
                resp.put("message", "Appointment cancelled successfully.");
                return ResponseEntity.ok(resp);
            }

            // Not deleted: either not found, or not owned by this patient
            if (!appointmentRepository.existsById(appointmentId)) {
                resp.put("message", "Appointment not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
            }

            resp.put("message", "You can only cancel your own appointment.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);

        } catch (Exception e) {
            resp.put("message", "Failed to cancel appointment. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    // 7. **Get Appointments Method**:
    //    - This method retrieves a list of appointments for a specific doctor on a particular day, optionally filtered by the patient's name.
    //    - It uses `@Transactional` to ensure that database operations are consistent and handled in a single transaction.
    //    - Instruction: Ensure the correct use of transaction boundaries, especially when querying the database for appointments.

    @Transactional
    public List<Appointment> getAppointmentsForDoctorOnDate(
            Long doctorId, LocalDate date, String patientName) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end   = date.plusDays(1).atStartOfDay();

        boolean hasName = patientName != null
                && !patientName.isBlank()
                && !"null".equalsIgnoreCase(patientName);

        return hasName
                ? appointmentRepository
                .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                        doctorId, patientName.trim(), start, end)
                : appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);
    }

    // 8. **Change Status Method**:
    //    - This method updates the status of an appointment by changing its value in the database.
    //    - It should be annotated with `@Transactional` to ensure the operation is executed in a single transaction.
    //    - Instruction: Add `@Transactional` before this method to ensure atomicity when updating appointment status.

    @Transactional
    public boolean changeStatus(long id, int status) {
        int updated = appointmentRepository.updateStatus(status, id);
        return updated > 0;
    }

}
