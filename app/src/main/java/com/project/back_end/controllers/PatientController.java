package com.project.back_end.controllers;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Patient;
import com.project.back_end.services.CommonService;
import com.project.back_end.services.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    // 1. Set Up the Controller Class:
    //    - Annotate the class with `@RestController` to define it as a REST API controller for patient-related operations.
    //    - Use `@RequestMapping("/patient")` to prefix all endpoints with `/patient`, grouping all patient functionalities under a common route.


    // 2. Autowire Dependencies:
    //    - Inject `PatientService` to handle patient-specific logic such as creation, retrieval, and appointments.
    //    - Inject the shared `Service` class for tasks like token validation and login authentication.

    private final PatientService patientService;
    private final CommonService commonService;

    public PatientController(PatientService patientService, CommonService commonService) {
        this.patientService = patientService;
        this.commonService = commonService;
    }


    // 3. Define the `getPatient` Method:
    //    - Handles HTTP GET requests to retrieve patient details using a token.
    //    - Validates the token for the `"patient"` role using the shared service.
    //    - If the token is valid, returns patient information; otherwise, returns an appropriate error message.

    @GetMapping("/{token}")
    public ResponseEntity<?> getPatient(@PathVariable String token) {
        String error = commonService.validateToken(token, "patient"); // null if valid
        if (error != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", error));
        }
        return patientService.getPatientDetails(token); // returns ResponseEntity<PatientDTO>
    }

    // 4. Define the `createPatient` Method:
    //    - Handles HTTP POST requests for patient registration.
    //    - Accepts a validated `Patient` object in the request body.
    //    - First checks if the patient already exists using the shared service.
    //    - If validation passes, attempts to create the patient and returns success or error messages based on the outcome.

    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@Valid @RequestBody Patient patient) {
        Map<String, String> body = new HashMap<>();
        try {
            String email = patient.getEmail();
            String phone = patient.getPhone();
            // Uniqueness validation via CommonService
            boolean ok = commonService.validatePatient(email, phone);
            if (!ok) {
                body.put("message", "Patient with given email/phone already exists.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
            }
            int res = patientService.createPatient(patient); // 1 success, 0 error
            if (res == 1) {
                body.put("message", "Patient created successfully.");
                return ResponseEntity.status(HttpStatus.CREATED).body(body);
            }
            body.put("message", "Failed to create patient.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        } catch (Exception e) {
            body.put("message", "Internal error while creating patient.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

    // 5. Define the `login` Method:
    //    - Handles HTTP POST requests for patient login.
    //    - Accepts a `Login` DTO containing email/username and password.
    //    - Delegates authentication to the `validatePatientLogin` method in the shared service.
    //    - Returns a response with a token or an error message depending on login success.

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Login req) {
        if (req == null || isBlank(req.email) || isBlank(req.password)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required."));
        }
        return commonService.validatePatientLogin(req.email, req.password);
    }

    // 6. Define the `getPatientAppointment` Method:
    //    - Handles HTTP GET requests to fetch appointment details for a specific patient.
    //    - Requires the patient ID, token, and user role as path variables.
    //    - Validates the token using the shared service.
    //    - If valid, retrieves the patient's appointment data from `PatientService`; otherwise, returns a validation error.

    @GetMapping("/appointments/{user}/{patientId}/{token:.+}")
    public ResponseEntity<List<AppointmentDTO>> getPatientAppointment(@PathVariable String user,
                                                                      @PathVariable Long patientId,
                                                                      @PathVariable String token) {
        String error = commonService.validateToken(token, user);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return patientService.getPatientAppointment(patientId);
    }

    // 7. Define the `filterPatientAppointment` Method:
    //    - Handles HTTP GET requests to filter a patient's appointments based on specific conditions.
    //    - Accepts filtering parameters: `condition`, `name`, and a token.
    //    - Token must be valid for a `"patient"` role.
    //    - If valid, delegates filtering logic to the shared service and returns the filtered result.

    @GetMapping("/appointments/filter/{condition}/{name}/{token:.+}")
    public ResponseEntity<List<AppointmentDTO>> filterPatientAppointment(@PathVariable String condition,
                                                                                        @PathVariable String name,
                                                                                        @PathVariable String token) {
        String normalizedCondition = normalize(condition);
        String normalizedName = normalize(name);
        String error = commonService.validateToken(token, "patient");
        if (error != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return commonService.filterPatient(token, normalizedCondition, normalizedName);
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }


    private String normalize(String v) {
        return (v == null || v.isBlank() || "null".equalsIgnoreCase(v) || "-".equals(v)) ? null : v.trim();
    }


    // Simple DTO for login
    public static class Login {
        public String email;
        public String password;
    }

}


