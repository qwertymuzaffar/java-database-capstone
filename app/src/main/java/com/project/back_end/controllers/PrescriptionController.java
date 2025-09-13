package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.CommonService;
import com.project.back_end.services.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {
    
    // 1. Set Up the Controller Class:
    //    - Annotate the class with `@RestController` to define it as a REST API controller.
    //    - Use `@RequestMapping("${api.path}prescription")` to set the base path for all prescription-related endpoints.
    //    - This controller manages creating and retrieving prescriptions tied to appointments.


    // 2. Autowire Dependencies:
    //    - Inject `PrescriptionService` to handle logic related to saving and fetching prescriptions.
    //    - Inject the shared `Service` class for token validation and role-based access control.
    //    - Inject `AppointmentService` to update appointment status after a prescription is issued.

    private final PrescriptionService prescriptionService;
    private final CommonService commonService; // token validation
    private final AppointmentService appointmentService; // to update appointment status after issuing Rx

    private static final int STATUS_AFTER_PRESCRIPTION = 1;

    public PrescriptionController(PrescriptionService prescriptionService, CommonService commonService, AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.commonService = commonService;
        this.appointmentService = appointmentService;
    }

    // 3. Define the `savePrescription` Method:
    //    - Handles HTTP POST requests to save a new prescription for a given appointment.
    //    - Accepts a validated `Prescription` object in the request body and a doctor’s token as a path variable.
    //    - Validates the token for the `"doctor"` role.
    //    - If the token is valid, updates the status of the corresponding appointment to reflect that a prescription has been added.
    //    - Delegates the saving logic to `PrescriptionService` and returns a response indicating success or failure.

    @PostMapping("/save/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(@PathVariable String token,
                                                                @Valid @RequestBody Prescription prescription) {
        // Validate token for doctor role
        String error = commonService.validateToken(token, "doctor"); // null when valid
        if (error != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", error));
        }


        // Validate payload
        if (prescription == null || prescription.getAppointmentId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "appointmentId is required."));
        }
        Long appointmentId = prescription.getAppointmentId();


        // Delegate save to service (enforces one-Rx-per-appointment)
        ResponseEntity<Map<String, String>> saveResp = prescriptionService.savePrescription(prescription);


        // If saved successfully (201), update appointment status (best-effort)
        if (saveResp.getStatusCode().is2xxSuccessful() || saveResp.getStatusCode() == HttpStatus.CREATED) {
            try {
                appointmentService.changeStatus(appointmentId, STATUS_AFTER_PRESCRIPTION);
            } catch (Exception ignored) {
            // Keep original successful response even if status update fails
            }
        }


        return saveResp;
    }

    // 4. Define the `getPrescription` Method:
    //    - Handles HTTP GET requests to retrieve a prescription by its associated appointment ID.
    //    - Accepts the appointment ID and a doctor’s token as path variables.
    //    - Validates the token for the `"doctor"` role using the shared service.
    //    - If the token is valid, fetches the prescription using the `PrescriptionService`.
    //    - Returns the prescription details or an appropriate error message if validation fails.

    @GetMapping("/{appointmentId}/{token:.+}")
    public ResponseEntity<Map<String, Object>> getPrescription(@PathVariable Long appointmentId,
                                                               @PathVariable String token) {
        // Validate token for doctor role
        String error = commonService.validateToken(token, "doctor");
        if (error != null) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", error);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
        // Delegate to service (returns { prescriptions: [...], message: ... })
        return prescriptionService.getPrescription(appointmentId);
    }

}
