package com.project.back_end.controllers;

import com.project.back_end.models.Doctor;
import com.project.back_end.services.CommonService;
import com.project.back_end.services.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}" + "doctor")
public class DoctorController {

    // 1. Set Up the Controller Class:
    //    - Annotate the class with `@RestController` to define it as a REST controller that serves JSON responses.
    //    - Use `@RequestMapping("${api.path}doctor")` to prefix all endpoints with a configurable API path followed by "doctor".
    //    - This class manages doctor-related functionalities such as registration, login, updates, and availability.


    // 2. Autowire Dependencies:
    //    - Inject `DoctorService` for handling the core logic related to doctors (e.g., CRUD operations, authentication).
    //    - Inject the shared `Service` class for general-purpose features like token validation and filtering.

    private DoctorService doctorService;
    private CommonService commonService;


    public DoctorController(DoctorService doctorService, CommonService commonService) {
        this.doctorService = doctorService;
        this.commonService = commonService;
    }

    // 3. Define the `getDoctorAvailability` Method:
    //    - Handles HTTP GET requests to check a specific doctor’s availability on a given date.
    //    - Requires `user` type, `doctorId`, `date`, and `token` as path variables.
    //    - First validates the token against the user type.
    //    - If the token is invalid, returns an error response; otherwise, returns the availability status for the doctor.

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(@PathVariable String user,
                                                                     @PathVariable Long doctorId,
                                                                     @PathVariable String date,
                                                                     @PathVariable String token) {
        Map<String, Object> body = new HashMap<>();
        String error = commonService.validateToken(token, user); // null when valid
        if (error != null) {
            body.put("message", error);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
        final LocalDate day;
        try {
            day = LocalDate.parse(date); // expects YYYY-MM-DD
        } catch (Exception e) {
            body.put("message", "Invalid date format. Use YYYY-MM-DD.");
            return ResponseEntity.badRequest().body(body);
        }
        List<String> available = doctorService.getDoctorAvailability(doctorId, day);
        body.put("doctorId", doctorId);
        body.put("date", date);
        body.put("available", available);
        body.put("message", available.isEmpty() ? "No available slots." : "Success");
        return ResponseEntity.ok(body);
    }

    // 4. Define the `getDoctor` Method:
    //    - Handles HTTP GET requests to retrieve a list of all doctors.
    //    - Returns the list within a response map under the key `"doctors"` with HTTP 200 OK status.

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctor() {
        Map<String, Object> body = new HashMap<>();
        List<Doctor> doctors = doctorService.getDoctors();
        body.put("doctors", doctors);
        return ResponseEntity.ok(body);
    }

    // 5. Define the `saveDoctor` Method:
    //    - Handles HTTP POST requests to register a new doctor.
    //    - Accepts a validated `Doctor` object in the request body and a token for authorization.
    //    - Validates the token for the `"admin"` role before proceeding.
    //    - If the doctor already exists, returns a conflict response; otherwise, adds the doctor and returns a success message.

    @PostMapping("/save/{token}")
    public ResponseEntity<Map<String, String>> saveDoctor(@PathVariable String token,
                                                          @Valid @RequestBody Doctor doctor) {
        String error = commonService.validateToken(token, "admin");
        if (error != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", error));
        }
        int res = doctorService.saveDoctor(doctor); // -1 conflict, 1 success, 0 error
        return switch (res) {
            case 1 -> ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Doctor saved successfully."));
            case -1 -> ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Doctor already exists."));
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to save doctor."));
        };
    }

    // 6. Define the `doctorLogin` Method:
    //    - Handles HTTP POST requests for doctor login.
    //    - Accepts a validated `Login` DTO containing credentials.
    //    - Delegates authentication to the `DoctorService` and returns login status and token information.

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@Valid @RequestBody Login req) {
        if (req == null || req.email == null || req.email.isBlank() || req.password == null || req.password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required."));
        }
        return doctorService.validateDoctor(req.email, req.password);
    }

    // 7. Define the `updateDoctor` Method:
    //    - Handles HTTP PUT requests to update an existing doctor's information.
    //    - Accepts a validated `Doctor` object and a token for authorization.
    //    - Token must belong to an `"admin"`.
    //    - If the doctor exists, updates the record and returns success; otherwise, returns not found or error messages.

    @PutMapping("/update/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(@PathVariable String token,
                                                            @Valid @RequestBody Doctor doctor) {
        String error = commonService.validateToken(token, "admin");
        if (error != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", error));
        }
        int res = doctorService.updateDoctor(doctor); // -1 not found, 1 ok, 0 error
        return switch (res) {
            case 1 -> ResponseEntity.ok(Map.of("message", "Doctor updated successfully."));
            case -1 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found."));
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to update doctor."));
        };
    }

    // 8. Define the `deleteDoctor` Method:
    //    - Handles HTTP DELETE requests to remove a doctor by ID.
    //    - Requires both doctor ID and an admin token as path variables.
    //    - If the doctor exists, deletes the record and returns a success message; otherwise, responds with a not found or error message.

    @DeleteMapping("/{doctorId}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable Long doctorId,
                                                            @PathVariable String token) {
        String error = commonService.validateToken(token, "admin");
        if (error != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", error));
        }
        int res = doctorService.deleteDoctor(doctorId); // -1 not found, 1 ok, 0 error
        return switch (res) {
            case 1 -> ResponseEntity.ok(Map.of("message", "Doctor deleted successfully."));
            case -1 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found."));
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to delete doctor."));
        };
    }

    // 9. Define the `filter` Method:
    //    - Handles HTTP GET requests to filter doctors based on name, time, and specialty.
    //    - Accepts `name`, `time`, and `speciality` as path variables.
    //    - Calls the shared `Service` to perform filtering logic and returns matching doctors in the response.

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filter(@PathVariable String name,
                                                      @PathVariable String time,
                                                      @PathVariable("speciality") String specialty) {
        String n = normalize(name);
        String t = normalize(time); // "AM"/"PM" expected (or null)
        String s = normalize(specialty);
        List<Doctor> doctors = commonService.filterDoctor(n, s, t);
        return ResponseEntity.ok(Map.of("doctors", doctors));
    }

    private String normalize(String v) {
        return (v == null || v.isBlank() || "null".equalsIgnoreCase(v) || "-".equals(v)) ? null : v.trim();
    }


    // DTO for login
    public static class Login {
        public String email;
        public String password;
    }

}
