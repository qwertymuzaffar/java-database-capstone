package com.project.back_end.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
public class Patient {
    // @Entity annotation:
    //    - Marks the class as a JPA entity, meaning it represents a table in the database.
    //    - Required for persistence frameworks (e.g., Hibernate) to map the class to a database table.


    // 1. 'id' field:
    //    - Type: private Long
    //    - Description:
    //      - Represents the unique identifier for each patient.
    //      - The @Id annotation marks it as the primary key.
    //      - The @GeneratedValue(strategy = GenerationType.IDENTITY) annotation auto-generates the ID value when a new record is inserted into the database.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // 2. 'name' field:
    //    - Type: private String
    //    - Description:
    //      - Represents the patient's full name.
    //      - The @NotNull annotation ensures that the patient's name is required.
    //      - The @Size(min = 3, max = 100) annotation ensures that the name length is between 3 and 100 characters.
    //      - Provides validation for correct input and user experience.

    @NotNull
    @Size(min = 3, max = 100)
    private String name;


    // 3. 'email' field:
    //    - Type: private String
    //    - Description:
    //      - Represents the patient's email address.
    //      - The @NotNull annotation ensures that an email address must be provided.
    //      - The @Email annotation validates that the email address follows a valid email format (e.g., patient@example.com).

    @NotNull
    @Email
    private String email;

    // 4. 'password' field:
    //    - Type: private String
    //    - Description:
    //      - Represents the patient's password for login authentication.
    //      - The @NotNull annotation ensures that a password must be provided.
    //      - The @Size(min = 6) annotation ensures that the password must be at least 6 characters long.

    @NotNull
    @Size(min = 6)
    private String password;

    // 5. 'phone' field:
    //    - Type: private String
    //    - Description:
    //      - Represents the patient's phone number.
    //      - The @NotNull annotation ensures that a phone number must be provided.
    //      - The @Pattern(regexp = "^[0-9]{10}$") annotation validates that the phone number must be exactly 10 digits long.

    @NotNull
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;

    // 6. 'address' field:
    //    - Type: private String
    //    - Description:
    //      - Represents the patient's address.
    //      - The @NotNull annotation ensures that the address must be provided.
    //      - The @Size(max = 255) annotation ensures that the address does not exceed 255 characters in length, providing validation for the address input.

    @NotNull
    @Size(max = 255)
    private String address;

    // 7. Getters and Setters:
    //    - Standard getter and setter methods are provided for all fields: id, name, email, password, phone, and address.
    //    - These methods allow access and modification of the fields of the Patient class.


    public long getId() {
        return id;
    }

    public @NotNull @Size(min = 3, max = 100) String getName() {
        return name;
    }

    public @NotNull @Email String getEmail() {
        return email;
    }

    public @NotNull @Size(min = 6) String getPassword() {
        return password;
    }

    public @NotNull @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits") String getPhone() {
        return phone;
    }

    public @NotNull @Size(max = 255) String getAddress() {
        return address;
    }

    public void setName(@NotNull @Size(min = 3, max = 100) String name) {
        this.name = name;
    }

    public void setEmail(@NotNull @Email String email) {
        this.email = email;
    }

    public void setPassword(@NotNull @Size(min = 6) String password) {
        this.password = password;
    }

    public void setPhone(@NotNull @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits") String phone) {
        this.phone = phone;
    }

    public void setAddress(@NotNull @Size(max = 255) String address) {
        this.address = address;
    }
}
