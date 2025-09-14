package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    // 1. **Add @Service Annotation**:
    //    - This class should be annotated with `@Service` to indicate that it is a service layer class.
    //    - The `@Service` annotation marks this class as a Spring-managed bean for business logic.
    //    - Instruction: Add `@Service` above the class declaration.

    // 2. **Constructor Injection for Dependencies**:
    //    - The `DoctorService` class depends on `DoctorRepository`, `AppointmentRepository`, and `TokenService`.
    //    - These dependencies should be injected via the constructor for proper dependency management.
    //    - Instruction: Ensure constructor injection is used for injecting dependencies into the service.

    private DoctorRepository doctorRepository;
    private AppointmentRepository appointmentRepository;
    private TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // 3. **Add @Transactional Annotation for Methods that Modify or Fetch Database Data**:
    //    - Methods like `getDoctorAvailability`, `getDoctors`, `findDoctorByName`, `filterDoctorsBy*` should be annotated with `@Transactional`.
    //    - The `@Transactional` annotation ensures that database operations are consistent and wrapped in a single transaction.
    //    - Instruction: Add the `@Transactional` annotation above the methods that perform database operations or queries.

    // 4. **getDoctorAvailability Method**:
    //    - Retrieves the available time slots for a specific doctor on a particular date and filters out already booked slots.
    //    - The method fetches all appointments for the doctor on the given date and calculates the availability by comparing against booked slots.
    //    - Instruction: Ensure that the time slots are properly formatted and the available slots are correctly filtered.

    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) return Collections.emptyList();

        // Day window [start, end)
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();


        // Appointments booked that day for this doctor
        List<Appointment> dayAppointments =
                appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);


        // Collect booked LocalTime values
        Set<LocalTime> bookedTimes = dayAppointments.stream()
                .map(Appointment::getAppointmentTime)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalTime)
                .collect(Collectors.toSet());


        // Doctor's configured available time-of-day slots
        List<String> slots = safeAvailableTimes(doctor);


        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");
        return slots.stream()
                .filter(t -> !bookedTimes.contains(t))
                .sorted()
                .collect(Collectors.toList());
    }

    // 5. **saveDoctor Method**:
    //    - Used to save a new doctor record in the database after checking if a doctor with the same email already exists.
    //    - If a doctor with the same email is found, it returns `-1` to indicate conflict; `1` for success, and `0` for internal errors.
    //    - Instruction: Ensure that the method correctly handles conflicts and exceptions when saving a doctor.

    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctor == null || doctor.getEmail() == null) return 0;
            boolean exists = doctorRepository.findByEmailIgnoreCase(doctor.getEmail()).isPresent();
            if (exists) return -1;
            Doctor saved = doctorRepository.save(doctor);
            return (saved != null && saved.getId() != null) ? 1 : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // 6. **updateDoctor Method**:
    //    - Updates an existing doctor's details in the database. If the doctor doesn't exist, it returns `-1`.
    //    - Instruction: Make sure that the doctor exists before attempting to save the updated record and handle any errors properly.

    @Transactional
    public int updateDoctor(Doctor updated) {
        try {
            if (updated == null || updated.getId() == null) return 0;
            Optional<Doctor> opt = doctorRepository.findById(updated.getId());
            if (opt.isEmpty()) return -1;
            Doctor d = opt.get();

            // copy safe/updatable fields (adjust to your model)
            d.setName(updated.getName());
            d.setEmail(updated.getEmail());
            d.setPhone(updated.getPhone());
            d.setPassword(updated.getPassword()); // TODO: encode if needed
            d.setSpecialty(updated.getSpecialty()); // or setSpecialization
            d.setAvailableTimes(safeAvailableTimes(updated));
            Doctor saved = doctorRepository.save(d);
            return (saved != null) ? 1 : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // 7. **getDoctors Method**:
    //    - Fetches all doctors from the database. It is marked with `@Transactional` to ensure that the collection is properly loaded.
    //    - Instruction: Ensure that the collection is eagerly loaded, especially if dealing with lazy-loaded relationships (e.g., available times).

    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        List<Doctor> list = doctorRepository.findAll();
        // Eagerly touch collections to avoid LazyInitialization outside tx
        list.forEach(d -> { List<String> at = safeAvailableTimes(d); at.size(); });
        return list;
    }

    // 8. **deleteDoctor Method**:
    //    - Deletes a doctor from the system along with all appointments associated with that doctor.
    //    - It first checks if the doctor exists. If not, it returns `-1`; otherwise, it deletes the doctor and their appointments.
    //    - Instruction: Ensure the doctor and their appointments are deleted properly, with error handling for internal issues.

    @Transactional
    public int deleteDoctor(Long doctorId) {
        try {
            Optional<Doctor> opt = doctorRepository.findById(doctorId);
            if (opt.isEmpty()) return -1;
            // delete appointments first to satisfy FK constraints
            // Supports either derived or custom method; prefer derived naming
            try {
                // If you created this method earlier:
                appointmentRepository.deleteAllByDoctorId(doctorId);
            } catch (Throwable t) {
                // Fallback to custom name if used in your repo
                try { appointmentRepository.deleteAllByDoctorId(doctorId); } catch (Throwable ignore) {}
            }
            doctorRepository.deleteById(doctorId);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

        // 9. **validateDoctor Method**:
        //    - Validates a doctor's login by checking if the email and password match an existing doctor record.
        //    - It generates a token for the doctor if the login is successful, otherwise returns an error message.
        //    - Instruction: Make sure to handle invalid login attempts and password mismatches properly with error responses.

        @Transactional(readOnly = true)
        public ResponseEntity<Map<String,String>> validateDoctor(String email, String password) {
            Map<String,String> resp = new HashMap<>();
            if (email == null || password == null) {
                resp.put("message", "Email and password are required.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
            }
            Optional<Doctor> opt = doctorRepository.findByEmailIgnoreCase(email);
            if (opt.isEmpty()) {
                resp.put("message", "Invalid credentials.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
            }
            Doctor d = opt.get();
            // NOTE: For production use PasswordEncoder. Here we compare plain text per spec.
            if (!Objects.equals(d.getPassword(), password)) {
                resp.put("message", "Invalid credentials.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
            }
            String token = tokenService.generateToken(d.getEmail());
            resp.put("message", "Login successful.");
            resp.put("token", token);
            return ResponseEntity.ok(resp);
        }

    // 10. **findDoctorByName Method**:
    //    - Finds doctors based on partial name matching and returns the list of doctors with their available times.
    //    - This method is annotated with `@Transactional` to ensure that the database query and data retrieval are properly managed within a transaction.
    //    - Instruction: Ensure that available times are eagerly loaded for the doctors.

    // ---- 10) findDoctorByName ----
    @Transactional(readOnly = true)
    public List<Doctor> findDoctorByName(String name) {
        List<Doctor> docs = doctorRepository.findByNameContaining(name);
        // eager touch availableTimes
        docs.forEach(d -> { List<String> at = safeAvailableTimes(d); at.size(); });
        return docs;
    }

    // 11. **filterDoctorsByNameSpecilityandTime Method**:
    //    - Filters doctors based on their name, specialty, and availability during a specific time (AM/PM).
    //    - The method fetches doctors matching the name and specialty criteria, then filters them based on their availability during the specified time period.
    //    - Instruction: Ensure proper filtering based on both the name and specialty as well as the specified time period.

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorsByNameSpecilityandTime(String name, String specialty, String period) {
        List<Doctor> base = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        return filterDoctorByTime(base, period);
    }

    // 12. **filterDoctorByTime Method**:
    //    - Filters a list of doctors based on whether their available times match the specified time period (AM/PM).
    //    - This method processes a list of doctors and their available times to return those that fit the time criteria.
    //    - Instruction: Ensure that the time filtering logic correctly handles both AM and PM time slots and edge cases.

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByTime(List<Doctor> doctors, String period) {
        if (doctors == null || doctors.isEmpty()) return Collections.emptyList();
        boolean wantAM = isAM(period);
        return doctors.stream()
                .filter(d -> safeAvailableTimes(d).stream().anyMatch(t -> matchesPeriod(LocalTime.parse(t), wantAM)))
                .peek(d -> { List<String> at = safeAvailableTimes(d); at.size(); })
                .collect(Collectors.toList());
    }

    // 13. **filterDoctorByNameAndTime Method**:
    //    - Filters doctors based on their name and the specified time period (AM/PM).
    //    - Fetches doctors based on partial name matching and filters the results to include only those available during the specified time period.
    //    - Instruction: Ensure that the method correctly filters doctors based on the given name and time of day (AM/PM).
    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByNameAndTime(String name, String period) {
        List<Doctor> base = doctorRepository.findByNameContaining(name);
        return filterDoctorByTime(base, period);
    }

    // 14. **filterDoctorByNameAndSpecility Method**:
    //    - Filters doctors by name and specialty.
    //    - It ensures that the resulting list of doctors matches both the name (case-insensitive) and the specified specialty.
    //    - Instruction: Ensure that both name and specialty are considered when filtering doctors.
    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByNameAndSpecility(String name, String specialty) {
        List<Doctor> base = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        base.forEach(d -> { List<String> at = safeAvailableTimes(d); at.size(); });
        return base;
    }

    // 15. **filterDoctorByTimeAndSpecility Method**:
    //    - Filters doctors based on their specialty and availability during a specific time period (AM/PM).
    //    - Fetches doctors based on the specified specialty and filters them based on their available time slots for AM/PM.
    //    - Instruction: Ensure the time filtering is accurately applied based on the given specialty and time period (AM/PM).

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByTimeAndSpecility(String period, String specialty) {
        List<Doctor> base = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        return filterDoctorByTime(base, period);
    }

    // 16. **filterDoctorBySpecility Method**:
    //    - Filters doctors based on their specialty.
    //    - This method fetches all doctors matching the specified specialty and returns them.
    //    - Instruction: Make sure the filtering logic works for case-insensitive specialty matching.

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorBySpecility(String specialty) {
        List<Doctor> base = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        base.forEach(d -> { List<String> at = safeAvailableTimes(d); at.size(); });
        return base;
    }

    // 17. **filterDoctorsByTime Method**:
    //    - Filters all doctors based on their availability during a specific time period (AM/PM).
    //    - The method checks all doctors' available times and returns those available during the specified time period.
    //    - Instruction: Ensure proper filtering logic to handle AM/PM time periods.

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorsByTime(String period) {
        List<Doctor> all = doctorRepository.findAll();
        return filterDoctorByTime(all, period);
    }

    private boolean isAM(String period) {
        return period != null && period.trim().equalsIgnoreCase("AM");
    }


    private boolean matchesPeriod(LocalTime t, boolean wantAM) {
        return wantAM ? t.getHour() < 12 : t.getHour() >= 12;
    }

    @SuppressWarnings("unchecked")
    private List<String> safeAvailableTimes(Doctor d) {
        if (d == null) return Collections.emptyList();
        List<String> at = d.getAvailableTimes();
        return (at != null) ? at : Collections.emptyList();
    }
}
