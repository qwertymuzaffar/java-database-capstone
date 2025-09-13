package com.project.back_end.controllers;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.CommonService;
import com.project.back_end.services.TokenService;
import jakarta.validation.Valid;
import org.antlr.v4.runtime.Token;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    // 1. Set Up the Controller Class:
    //    - Annotate the class with `@RestController` to define it as a REST API controller.
    //    - Use `@RequestMapping("/appointments")` to set a base path for all appointment-related endpoints.
    //    - This centralizes all routes that deal with booking, updating, retrieving, and canceling appointments.


    // 2. Autowire Dependencies:
    //    - Inject `AppointmentService` for handling the business logic specific to appointments.
    //    - Inject the general `Service` class, which provides shared functionality like token validation and appointment checks.

    private final AppointmentService appointmentService;
    private final CommonService commonService;
    private final TokenService tokenService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentController(
            AppointmentService appointmentService,
            CommonService commonService,
            TokenService tokenService,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository
    ) {
        this.appointmentService = appointmentService;
        this.commonService = commonService;
        this.tokenService = tokenService;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    // 3. Define the `getAppointments` Method:
    //    - Handles HTTP GET requests to fetch appointments based on date and patient name.
    //    - Takes the appointment date, patient name, and token as path variables.
    //    - First validates the token for role `"doctor"` using the `Service`.
    //    - If the token is valid, returns appointments for the given patient on the specified date.
    //    - If the token is invalid or expired, responds with the appropriate message and status code.

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<?> getAppointment(@PathVariable String date, @PathVariable String patientName, @PathVariable String token) {
        String error = commonService.validateToken(token, "doctor"); // null if valid
        if (error != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", error));
        }

        String email;
        try {
            email = tokenService.extractEmail(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid token."));
        }

        var docOpt = doctorRepository.findByEmailIgnoreCase(email);
        if (docOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Doctor not found."));
        }

        Long doctorId = docOpt.get().getId();

        final LocalDate day;
        try {
            day = LocalDate.parse(date); // ISO-8601, e.g. 2025-09-13
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid date format. Use YYYY-MM-DD."));
        }

        String nameFilter = (patientName == null
                || patientName.isBlank()
                || "null".equalsIgnoreCase(patientName)
                || "-".equals(patientName)) ? null : patientName.trim();

        var appts = appointmentService.getAppointmentsForDoctorOnDate(doctorId, day, nameFilter);
        List<AppointmentDTO> dto = appts.stream().map(a -> {
            AppointmentDTO dto2 = new AppointmentDTO();
            BeanUtils.copyProperties(a, dto2);
            return dto2;
        }).toList();

        return ResponseEntity.ok(dto);

    }

    // 4. Define the `bookAppointment` Method:
    //    - Handles HTTP POST requests to create a new appointment.
    //    - Accepts a validated `Appointment` object in the request body and a token as a path variable.
    //    - Validates the token for the `"patient"` role.
    //    - Uses service logic to validate the appointment data (e.g., check for doctor availability and time conflicts).
    //    - Returns success if booked, or appropriate error messages if the doctor ID is invalid or the slot is already taken.

    @PostMapping("/appointments/book/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(@PathVariable String token,
                                                               @Valid @RequestBody AppointmentDTO appointment) {
        // validate token for role "patient"
        String error = commonService.validateToken(token, "patient"); // null if valid
        if (error != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", error));
        }

        // resolve patient from token
        String email = tokenService.extractEmail(token);
        Optional<Patient> patientOpt = patientRepository.findByEmailIgnoreCase(email);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Patient not found."));
        }

        // basic payload checks
        if (appointment.getDoctorId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Doctor id is required."));
        }
        if (appointment.getAppointmentTime() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Appointment time is required."));
        }

        // validate slot/availability for that doctor & date
        int slotCheck = commonService.validateAppointment(
                appointment.getDoctorId(), appointment.getAppointmentTime());
        if (slotCheck == -1) { // doctor not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found."));
        }
        if (slotCheck == 0) { // slot unavailable
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Requested slot is unavailable."));
        }

        // set the owning patient
        appointment.setPatientId(patientOpt.get().getId());

        // delegate save to service (returns 1 on success / 0 on failure)
        Appointment appointment1 = new Appointment();
        BeanUtils.copyProperties(appointment, appointment1);

        int saved = appointmentService.bookAppointment(appointment1);
        if (saved == 1) {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Appointment booked successfully."));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to book appointment. Please try again."));
    }


    // 5. Define the `updateAppointment` Method:
    //    - Handles HTTP PUT requests to modify an existing appointment.
    //    - Accepts a validated `Appointment` object and a token as input.
    //    - Validates the token for `"patient"` role.
    //    - Delegates the update logic to the `AppointmentService`.
    //    - Returns an appropriate success or failure response based on the update result.

    @PutMapping("/appointments/{token:.+}")
    public ResponseEntity<Map<String, String>> updateAppointment(@PathVariable String token,
                                                                 @Valid @RequestBody Appointment appointment) {
        // validate token for role "patient"
        String error = commonService.validateToken(token, "patient");
        if (error != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", error));
        }

        // resolve patient (ownership enforcement happens in service)
        String email = tokenService.extractEmail(token);
        Optional<Patient> patientOpt = patientRepository.findByEmailIgnoreCase(email);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Patient not found."));
        }
        return appointmentService.updateAppointment(appointment);
    }


    // 6. Define the `cancelAppointment` Method:
    //    - Handles HTTP DELETE requests to cancel a specific appointment.
    //    - Accepts the appointment ID and a token as path variables.
    //    - Validates the token for `"patient"` role to ensure the user is authorized to cancel the appointment.
    //    - Calls `AppointmentService` to handle the cancellation process and returns the result.

    @DeleteMapping("/appointments/{appointmentId}/{token:.+}")
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable Long appointmentId,
                                                                 @PathVariable String token) {
        
        // resolve patient
        String email = tokenService.extractEmail(token);
        Optional<Patient> patientOpt = patientRepository.findByEmailIgnoreCase(email);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Patient not found."));
        }

        return appointmentService.cancelAppointment(appointmentId, patientOpt.get().getId());
    }

}
