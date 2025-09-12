package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CommonService {
    // 1. **@Service Annotation**
    // The @Service annotation marks this class as a service component in Spring. This allows Spring to automatically detect it through component scanning
    // and manage its lifecycle, enabling it to be injected into controllers or other services using @Autowired or constructor injection.

    // 2. **Constructor Injection for Dependencies**
    // The constructor injects all required dependencies (TokenService, Repositories, and other Services). This approach promotes loose coupling, improves testability,
    // and ensures that all required dependencies are provided at object creation time.

    private TokenService tokenService;
    private AdminRepository adminRepository;
    private DoctorRepository doctorRepository;
    private PatientRepository patientRepository;
    private DoctorService doctorService;
    private PatientService patientService;


    public CommonService(TokenService tokenService,
                         AdminRepository adminRepository,
                         DoctorRepository doctorRepository,
                         PatientRepository patientRepository,
                         DoctorService doctorService,
                         PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    // 3. **validateToken Method**
    // This method checks if the provided JWT token is valid for a specific user. It uses the TokenService to perform the validation.
    // If the token is invalid or expired, it returns a 401 Unauthorized response with an appropriate error message. This ensures security by preventing
    // unauthorized access to protected resources.

    @Transactional(readOnly = true)
    public String validateToken(String token, String role) {
        if (token == null || token.isBlank()) return "Missing token.";
        if (role == null || role.isBlank()) return "Missing role.";


        // If TokenService exposes validateToken, use it first
        try {
        //noinspection ConstantConditions
            if (tokenService.validateToken(token, role)) return null;
        } catch (Throwable ignored) {
        // fall through to subject-based validation below
        }


        String subject;
        try {
            subject = tokenService.extractEmail(token); // for admins we also store username in subject
        } catch (Throwable t) {
            return "Invalid token.";
        }
        if (subject == null || subject.isBlank()) return "Invalid token.";


        switch (role.toLowerCase(Locale.ROOT)) {
            case "admin":
                if (existsAdminByUsernameOrEmail(subject)) return null;
                break;
            case "doctor":
                if (existsDoctorByEmail(subject)) return null;
                break;
            case "patient":
                if (existsPatientByEmail(subject)) return null;
                break;
            default:
                return "Unknown role.";
        }
        return "Unauthorized or user not found.";
    }

    // 4. **validateAdmin Method**
    // This method validates the login credentials for an admin user.
    // - It first searches the admin repository using the provided username.
    // - If an admin is found, it checks if the password matches.
    // - If the password is correct, it generates and returns a JWT token (using the admin’s username) with a 200 OK status.
    // - If the password is incorrect, it returns a 401 Unauthorized status with an error message.
    // - If no admin is found, it also returns a 401 Unauthorized.
    // - If any unexpected error occurs during the process, a 500 Internal Server Error response is returned.
    // This method ensures that only valid admin users can access secured parts of the system.

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, String>> validateAdmin(String username, String password) {
        Map<String, String> body = new HashMap<>();
        try {
            if (username == null || password == null) {
                body.put("message", "Username and password are required.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
            }
            Optional<Admin> opt = adminRepository.findByUsername(username);
            if (opt.isEmpty()) {
                body.put("message", "Invalid credentials.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
            }
            Admin a = opt.get();
            // NOTE: Replace with PasswordEncoder in production
            if (!Objects.equals(a.getPassword(), password)) {
                body.put("message", "Invalid credentials.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
            }
            String token = tokenService.generateToken(username);
            body.put("message", "Login successful.");
            body.put("token", token);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            body.put("message", "Internal error while validating admin.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

    // 5. **filterDoctor Method**
    // This method provides filtering functionality for doctors based on name, specialty, and available time slots.
    // - It supports various combinations of the three filters.
    // - If none of the filters are provided, it returns all available doctors.
    // This flexible filtering mechanism allows the frontend or consumers of the API to search and narrow down doctors based on user criteria.

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctor(String name, String specialty, String period) {
        boolean hasName = notBlank(name);
        boolean hasSpec = notBlank(specialty);
        boolean hasPeriod = notBlank(period);


        if (hasName && hasSpec && hasPeriod) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, period);
        }
        if (hasSpec && hasPeriod) {
            return doctorService.filterDoctorByTimeAndSpecility(period, specialty);
        }
        if (hasName && hasSpec) {
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        }
        if (hasName && hasPeriod) {
            return doctorService.filterDoctorByNameAndTime(name, period);
        }
        if (hasSpec) {
            return doctorService.filterDoctorBySpecility(specialty);
        }
        if (hasPeriod) {
            return doctorService.filterDoctorsByTime(period);
        }
        if (hasName) {
            return doctorService.findDoctorByName(name);
        }
        return doctorService.getDoctors();
    }

    // 6. **validateAppointment Method**
    // This method validates if the requested appointment time for a doctor is available.
    // - It first checks if the doctor exists in the repository.
    // - Then, it retrieves the list of available time slots for the doctor on the specified date.
    // - It compares the requested appointment time with the start times of these slots.
    // - If a match is found, it returns 1 (valid appointment time).
    // - If no matching time slot is found, it returns 0 (invalid).
    // - If the doctor doesn’t exist, it returns -1.
    // This logic prevents overlapping or invalid appointment bookings.

    @Transactional(readOnly = true)
    public int validateAppointment(Long doctorId, LocalDateTime requestedTime) {
        if (doctorId == null) return -1;
        if (requestedTime == null) return 0;


        var docOpt = doctorRepository.findById(doctorId);
        if (docOpt.isEmpty()) return -1;


        LocalDate date = requestedTime.toLocalDate();
        List<String> available = doctorService.getDoctorAvailability(doctorId, date); // formatted slots e.g. "09:30 AM"


        String want = requestedTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        return available.contains(want) ? 1 : 0;
    }

    // 7. **validatePatient Method**
    // This method checks whether a patient with the same email or phone number already exists in the system.
    // - If a match is found, it returns false (indicating the patient is not valid for new registration).
    // - If no match is found, it returns true.
    // This helps enforce uniqueness constraints on patient records and prevent duplicate entries.
    @Transactional(readOnly = true)
    public boolean validatePatient(String email, String phone) {
        if (notBlank(email) && patientRepository.findByEmailIgnoreCase(email).isPresent()) return false;
        if (notBlank(phone)) {
            try {
                // if you have a combined query
                if (patientRepository.findByEmailOrPhone(email == null ? "" : email, phone).isPresent()) return false;
            } catch (Throwable ignored) {
                // else fall back to an existsByPhone if available
                try { if (patientRepository.existsByPhone(phone)) return false; } catch (Throwable ignored2) {}
            }
        }
        return true;
    }

    // 8. **validatePatientLogin Method**
    // This method handles login validation for patient users.
    // - It looks up the patient by email.
    // - If found, it checks whether the provided password matches the stored one.
    // - On successful validation, it generates a JWT token and returns it with a 200 OK status.
    // - If the password is incorrect or the patient doesn't exist, it returns a 401 Unauthorized with a relevant error.
    // - If an exception occurs, it returns a 500 Internal Server Error.
    // This method ensures only legitimate patients can log in and access their data securely.

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, String>> validatePatientLogin(String email, String password) {
        Map<String, String> body = new HashMap<>();
        try {
            if (!notBlank(email) || !notBlank(password)) {
                body.put("message", "Email and password are required.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
            }
            Optional<Patient> opt = patientRepository.findByEmailIgnoreCase(email);
            if (opt.isEmpty()) {
                body.put("message", "Invalid credentials.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
            }
            Patient p = opt.get();
            if (!Objects.equals(p.getPassword(), password)) {
                body.put("message", "Invalid credentials.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
            }
            String token = tokenService.generateToken(p.getEmail());
            body.put("message", "Login successful.");
            body.put("token", token);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            body.put("message", "Internal error while validating patient login.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

    // 9. **filterPatient Method**
    // This method filters a patient's appointment history based on condition and doctor name.
    // - It extracts the email from the JWT token to identify the patient.
    // - Depending on which filters (condition, doctor name) are provided, it delegates the filtering logic to PatientService.
    // - If no filters are provided, it retrieves all appointments for the patient.
    // This flexible method supports patient-specific querying and enhances user experience on the client side.

    @Transactional(readOnly = true)
    public ResponseEntity<List<AppointmentDTO>> filterPatient(String token, String condition, String doctorName) {
        try {
            if (!notBlank(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of());
            }
            String email = tokenService.extractEmail(token);
            if (!notBlank(email)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of());
            }
            Optional<Patient> opt = patientRepository.findByEmailIgnoreCase(email);
            if (opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
            }
            Long patientId = opt.get().getId();


            boolean hasCond = notBlank(condition);
            boolean hasDoc = notBlank(doctorName);


            if (hasCond && hasDoc) {
                return patientService.filterByDoctorAndCondition(patientId, doctorName, condition);
            }
            if (hasCond) {
                return patientService.filterByCondition(patientId, condition);
            }
            if (hasDoc) {
                return patientService.filterByDoctor(patientId, doctorName);
            }
            return patientService.getPatientAppointment(patientId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    // ============================= helpers =============================


    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private boolean existsAdminByUsernameOrEmail(String subject) {
        try { if (adminRepository.existsByUsername(subject)) return true; } catch (Throwable ignored) {}
        try { if (adminRepository.findByUsername(subject).isPresent()) return true; } catch (Throwable ignored) {}
        try { if (adminRepository.existsByEmail(subject)) return true; } catch (Throwable ignored) {}
        try { return adminRepository.findByEmail(subject).isPresent(); } catch (Throwable ignored) {}
        return false;
    }

    private boolean existsDoctorByEmail(String email) {
        try { if (doctorRepository.existsByEmail(email)) return true; } catch (Throwable ignored) {}
        try { return doctorRepository.findByEmailIgnoreCase(email).isPresent(); } catch (Throwable ignored) {}
        return false;
    }

    private boolean existsPatientByEmail(String email) {
        try { if (patientRepository.existsByEmail(email)) return true; } catch (Throwable ignored) {}
        try { return patientRepository.findByEmailIgnoreCase(email).isPresent(); } catch (Throwable ignored) {}
        return false;
    }
}
