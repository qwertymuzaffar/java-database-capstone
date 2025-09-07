package com.project.back_end.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
public class Doctor {

    // @Entity annotation:
    //    - Marks the class as a JPA entity, meaning it represents a table in the database.
    //    - Required for persistence frameworks (e.g., Hibernate) to map the class to a database table.

    // 1. 'id' field:
    //    - Type: private Long
    //    - Description:
    //      - Represents the unique identifier for each doctor.
    //      - The @Id annotation marks it as the primary key.
    //      - The @GeneratedValue(strategy = GenerationType.IDENTITY) annotation auto-generates the ID value when a new record is inserted into the database.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // 2. 'name' field:
    //    - Type: private String
    //    - Description:
    //      - Represents the doctor's name.
    //      - The @NotNull annotation ensures that the doctor's name is required.
    //      - The @Size(min = 3, max = 100) annotation ensures that the name length is between 3 and 100 characters.
    //      - Provides validation for correct input and user experience.

    @NotNull
    @Size(min = 3, max = 100)
    private String name;


    // 3. 'specialty' field:
    //    - Type: private String
    //    - Description:
    //      - Represents the medical specialty of the doctor.
    //      - The @NotNull annotation ensures that a specialty must be provided.
    //      - The @Size(min = 3, max = 50) annotation ensures that the specialty name is between 3 and 50 characters long.

    @NotNull
    @Size(min = 3, max = 50)
    private String specialty;

    // 4. 'email' field:
    //    - Type: private String
    //    - Description:
    //      - Represents the doctor's email address.
    //      - The @NotNull annotation ensures that an email address is required.
    //      - The @Email annotation validates that the email address follows a valid email format (e.g., doctor@example.com).

    @NotNull
    @Email
    private String email;

    // 5. 'password' field:
    //    - Type: private String
    //    - Description:
    //      - Represents the doctor's password for login authentication.
    //      - The @NotNull annotation ensures that a password must be provided.
    //      - The @Size(min = 6) annotation ensures that the password must be at least 6 characters long.
    //      - The @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) annotation ensures that the password is not serialized in the response (hidden from the frontend).

    @NotNull
    @Size(min = 6)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // 6. 'phone' field:
    //    - Type: private String
    //    - Description:
    //      - Represents the doctor's phone number.
    //      - The @NotNull annotation ensures that a phone number must be provided.
    //      - The @Pattern(regexp = "^[0-9]{10}$") annotation validates that the phone number must be exactly 10 digits long.

    @NotNull
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;

    // 7. 'availableTimes' field:
    //    - Type: private List<String>
    //    - Description:
    //      - Represents the available times for the doctor in a list of time slots.
    //      - Each time slot is represented as a string (e.g., "09:00-10:00", "10:00-11:00").
    //      - The @ElementCollection annotation ensures that the list of time slots is stored as a separate collection in the database.

    @ElementCollection
    private List<String> availableTimes;

    // 8. Getters and Setters:
    //    - Standard getter and setter methods are provided for all fields: id, name, specialty, email, password, phone, and availableTimes.

    public long getId() {
        return id;
    }

    public @NotNull @Size(min = 3, max = 100) String getName() {
        return name;
    }

    public @NotNull @Size(min = 3, max = 50) String getSpecialty() {
        return specialty;
    }

    public @NotNull @Email String getEmail() {
        return email;
    }

    public @NotNull @Size(min = 6) String getPassword() {
        return password;
    }

    public @NotNull @Pattern(regexp = "^[0-9]{10}$") String getPhone() {
        return phone;
    }

    public List<String> getAvailableTimes() {
        return availableTimes;
    }

    public void setName(@NotNull @Size(min = 3, max = 100) String name) {
        this.name = name;
    }

    public void setSpecialty(@NotNull @Size(min = 3, max = 50) String specialty) {
        this.specialty = specialty;
    }

    public void setEmail(@NotNull @Email String email) {
        this.email = email;
    }

    public void setPassword(@NotNull @Size(min = 6) String password) {
        this.password = password;
    }

    public void setPhone(@NotNull @Pattern(regexp = "^[0-9]{10}$") String phone) {
        this.phone = phone;
    }

    public void setAvailableTimes(List<String> availableTimes) {
        this.availableTimes = availableTimes;
    }
}

