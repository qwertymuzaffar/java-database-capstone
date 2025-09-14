package com.project.back_end.services;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.*;
import java.util.function.Supplier;

@Service
public class PatientService {
    // 1. **Add @Service Annotation**:
    //    - The `@Service` annotation is used to mark this class as a Spring service component.
    //    - It will be managed by Spring's container and used for business logic related to patients and appointments.
    //    - Instruction: Ensure that the `@Service` annotation is applied above the class declaration.

    // 2. **Constructor Injection for Dependencies**:
    //    - The `PatientService` class has dependencies on `PatientRepository`, `AppointmentRepository`, and `TokenService`.
    //    - These dependencies are injected via the constructor to maintain good practices of dependency injection and testing.
    //    - Instruction: Ensure constructor injection is used for all the required dependencies.

    private PatientRepository patientRepository;
    private AppointmentRepository appointmentRepository;
    private TokenService tokenService;

    public PatientService(
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }


    // 3. **createPatient Method**:
    //    - Creates a new patient in the database. It saves the patient object using the `PatientRepository`.
    //    - If the patient is successfully saved, the method returns `1`; otherwise, it logs the error and returns `0`.
    //    - Instruction: Ensure that error handling is done properly and exceptions are caught and logged appropriately.

    public int createPatient(Patient patient) {
        try {
            if (patient == null) return 0;
            // Ensure insert
            try { patient.setId(null); } catch (Throwable ignored) {}
            Patient saved = patientRepository.save(patient);
            return (saved != null && getId(saved) != null) ? 1 : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // 4. **getPatientAppointment Method**:
    //    - Retrieves a list of appointments for a specific patient, based on their ID.
    //    - The appointments are then converted into `AppointmentDTO` objects for easier consumption by the API client.
    //    - This method is marked as `@Transactional` to ensure database consistency during the transaction.
    //    - Instruction: Ensure that appointment data is properly converted into DTOs and the method handles errors gracefully.

    @Transactional(readOnly = true)
    public ResponseEntity<List<AppointmentDTO>> getPatientAppointment(Long patientId) {
        try {
            if (patientId == null) {
                return ResponseEntity.badRequest().body(List.of());
            }
            List<Appointment> list = appointmentRepository
                    .findByPatientId(patientId);
            List<AppointmentDTO> dto = list.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    // 5. **filterByCondition Method**:
    //    - Filters appointments for a patient based on the condition (e.g., "past" or "future").
    //    - Retrieves appointments with a specific status (0 for future, 1 for past) for the patient.
    //    - Converts the appointments into `AppointmentDTO` and returns them in the response.
    //    - Instruction: Ensure the method correctly handles "past" and "future" conditions, and that invalid conditions are caught and returned as errors.

    @Transactional(readOnly = true)
    public ResponseEntity<List<AppointmentDTO>> filterByCondition(Long patientId, String condition) {
        try {
            Integer status = mapConditionToStatus(condition);
            if (patientId == null || status == null) {
                return ResponseEntity.badRequest().body(List.of());
            }
            List<Appointment> list = appointmentRepository
                    .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status);
            List<AppointmentDTO> dto = list.stream().map(this::toDto).collect(Collectors.toList());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    // 6. **filterByDoctor Method**:
    //    - Filters appointments for a patient based on the doctor's name.
    //    - It retrieves appointments where the doctor’s name matches the given value, and the patient ID matches the provided ID.
    //    - Instruction: Ensure that the method correctly filters by doctor's name and patient ID and handles any errors or invalid cases.

    @Transactional(readOnly = true)
    public ResponseEntity<List<AppointmentDTO>> filterByDoctor(Long patientId, String doctorName) {
        try {
            if (patientId == null || doctorName == null || doctorName.isBlank()) {
                return ResponseEntity.badRequest().body(List.of());
            }
            List<Appointment> list = appointmentRepository
                    .findByDoctor_NameContainingIgnoreCaseAndPatient_Id(doctorName, patientId);
            List<AppointmentDTO> dto = list.stream().map(this::toDto).collect(Collectors.toList());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    // 7. **filterByDoctorAndCondition Method**:
    //    - Filters appointments based on both the doctor's name and the condition (past or future) for a specific patient.
    //    - This method combines filtering by doctor name and appointment status (past or future).
    //    - Converts the appointments into `AppointmentDTO` objects and returns them in the response.
    //    - Instruction: Ensure that the filter handles both doctor name and condition properly, and catches errors for invalid input.

    @Transactional(readOnly = true)
    public ResponseEntity<List<AppointmentDTO>> filterByDoctorAndCondition(Long patientId, String doctorName, String condition) {
        try {
            Integer status = mapConditionToStatus(condition);
            if (patientId == null || status == null || doctorName == null || doctorName.isBlank()) {
                return ResponseEntity.badRequest().body(List.of());
            }
            List<Appointment> list = appointmentRepository
                    .findByDoctor_NameContainingIgnoreCaseAndPatient_IdAndStatusOrderByAppointmentTimeAsc(doctorName, patientId, status);
            List<AppointmentDTO> dto = list.stream().map(this::toDto).collect(Collectors.toList());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    // 8. **getPatientDetails Method**:
    //    - Retrieves patient details using the `tokenService` to extract the patient's email from the provided token.
    //    - Once the email is extracted, it fetches the corresponding patient from the `patientRepository`.
    //    - It returns the patient's information in the response body.
    //    - Instruction: Make sure that the token extraction process works correctly and patient details are fetched properly based on the extracted email.

    @Transactional(readOnly = true)
    public ResponseEntity<Patient> getPatientDetails(String token) {
        try {
            if (token == null || token.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String email = tokenService.extractEmail(token);
            if (email == null || email.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Optional<Patient> opt = patientRepository.findByEmailIgnoreCase(email);
            if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            Patient p = opt.get();
            return ResponseEntity.ok(toDto(p));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 9. **Handling Exceptions and Errors**:
    //    - The service methods handle exceptions using try-catch blocks and log any issues that occur. If an error occurs during database operations, the service responds with appropriate HTTP status codes (e.g., `500 Internal Server Error`).
    //    - Instruction: Ensure that error handling is consistent across the service, with proper logging and meaningful error messages returned to the client.

    // 10. **Use of DTOs (Data Transfer Objects)**:
    //    - The service uses `AppointmentDTO` to transfer appointment-related data between layers. This ensures that sensitive or unnecessary data (e.g., password or private patient information) is not exposed in the response.
    //    - Instruction: Ensure that DTOs are used appropriately to limit the exposure of internal data and only send the relevant fields to the client.

    private Integer mapConditionToStatus(String condition) {
        if (condition == null) return null;
        String c = condition.trim().toLowerCase();
        if ("future".equals(c)) return 0;
        if ("past".equals(c)) return 1;
        return null;
    }

    private AppointmentDTO toDto(Appointment a) {
        if (a == null) return null;
        AppointmentDTO dto = new AppointmentDTO();
        BeanUtils.copyProperties(a, dto);
        return dto;
    }

    private Patient toDto(Patient p) {
        if (p == null) return null;
        Patient dto = new Patient();
        BeanUtils.copyProperties(p, dto);
        return dto;
    }

    private static <T> T safe(Supplier<T> s) {
        try { return s.get(); } catch (RuntimeException e) { return null; }
    }

    private Long getId(Patient p) {
        try { return p.getId(); } catch (Throwable t) { return null; }
    }
}
