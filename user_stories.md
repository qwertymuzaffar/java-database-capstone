# User Story Template

**Title:**
_As a [user role], I want [feature/goal], so that [reason]._

**Acceptance Criteria:**
1. [Criteria 1]
2. [Criteria 2]
3. [Criteria 3]

**Priority:** [High/Medium/Low]
**Story Points:** [Estimated Effort in Points]
**Notes:**
- [Additional information or edge cases]


## Admin User Stories

User Story 1: Admin Login

Title: As an admin, I want to log into the portal with my username and password, so that I can securely manage the platform.

Acceptance Criteria:

Admin must enter a valid username and password.

System validates credentials against stored records in MySQL.

Successful login redirects the admin to the Admin Dashboard.

Invalid credentials show an appropriate error message.

Priority: High Story Points: 3 Notes: Consider password encryption and session handling.

User Story 2: Admin Logout

Title: As an admin, I want to log out of the portal, so that I can protect the system from unauthorized access.

Acceptance Criteria:

Logout option is available on all admin dashboard pages.

Clicking logout ends the admin session.

System redirects to the login page after logout.

No admin functionality should be accessible after logout.

Priority: High Story Points: 2 Notes: Ensure proper session invalidation.

User Story 3: Add Doctor Profile

Title: As an admin, I want to add doctors to the portal, so that they can be registered in the system and start managing appointments.

Acceptance Criteria:

Admin provides doctor’s name, email, specialization, and other required details.

The system validates the input fields.

The doctor’s profile is saved in the MySQL database.

Confirmation message is shown after successful addition.

Priority: High Story Points: 5 Notes: Consider input validation, duplicate entries, and email uniqueness.

User Story 4: Delete Doctor Profile

Title: As an admin, I want to delete a doctor’s profile from the portal, so that I can remove inactive or invalid records.

Acceptance Criteria:

Admin can view a list of doctors with delete options.

The system asks for confirmation before deleting.

Deleted doctor profile is removed from MySQL.

A success message is displayed after deletion.

Priority: Medium Story Points: 3 Notes: Consider cascade delete (appointments, prescriptions) or restrict if linked records exist.

User Story 5: Run Stored Procedure for Reports

Title: As an admin, I want to run a stored procedure in the MySQL CLI, so that I can get the number of appointments per month and track usage statistics.

Acceptance Criteria:

Stored procedure is available in MySQL with proper indexing.

Procedure returns the number of appointments grouped by month.

Admin can access this report securely.

Output is displayed as a table or exported as needed.

Priority: Medium Story Points: 4 Notes: Ensure procedure is optimized for large datasets.

## Patient User Stories

User Story 1: View Doctors Without Login

Title: As a patient, I want to view a list of doctors without logging in, so that I can explore options before registering.

Acceptance Criteria:

Patients can access the doctors’ list from the homepage.

Doctor details include name, specialization, and availability.

No login or registration is required to view the list.

The system prevents booking without login.

Priority: High Story Points: 3 Notes: Optimize queries to fetch only public doctor info (exclude private data).

User Story 2: Patient Sign Up

Title: As a patient, I want to sign up using my email and password, so that I can book appointments.

Acceptance Criteria:

Patient must provide a valid email and password.

Password must meet complexity requirements.

Successful registration creates a patient record in MySQL.

Duplicate emails should not be allowed.

Priority: High Story Points: 5 Notes: Consider email verification for security.

User Story 3: Patient Login

Title: As a patient, I want to log into the portal, so that I can manage my bookings.

Acceptance Criteria:

Patient provides valid login credentials.

Successful login redirects to the Patient Dashboard.

Invalid login shows an error message.

Session is maintained until logout.

Priority: High Story Points: 3 Notes: Ensure secure password handling with hashing.

User Story 4: Patient Logout

Title: As a patient, I want to log out of the portal, so that I can secure my account.

Acceptance Criteria:

Logout option is visible on all dashboard pages.

Logout ends the current session.

Redirects the patient to the login page.

No booking or profile management should be accessible after logout.

Priority: High Story Points: 2 Notes: Handle session expiration gracefully.

User Story 5: Book an Appointment

Title: As a patient, I want to log in and book an hour-long appointment with a doctor, so that I can consult with them.

Acceptance Criteria:

Patient must be logged in to book.

Appointment duration is fixed at one hour.

Patients can select a doctor and an available time slot.

Booking is saved in MySQL and confirmation is displayed.

Priority: High Story Points: 5 Notes: Handle double-booking prevention and slot validation.

User Story 6: View Upcoming Appointments

Title: As a patient, I want to view my upcoming appointments, so that I can prepare accordingly.

Acceptance Criteria:

Patient can see a list of all upcoming confirmed appointments.

List includes doctor name, date, and time.

Only the logged-in patient’s appointments are visible.

Cancelled or past appointments are not shown in this view.

Priority: Medium Story Points: 3 Notes: Provide filtering for upcoming vs. past appointments.

## Doctor User Stories

User Story 1: Doctor Login

Title: As a doctor, I want to log into the portal, so that I can manage my appointments.

Acceptance Criteria:

Doctor must provide valid username/email and password.

Successful login redirects to the Doctor Dashboard.

Invalid login shows an error message.

Session persists until logout.

Priority: High Story Points: 3 Notes: Ensure secure authentication with password hashing.

User Story 2: Doctor Logout

Title: As a doctor, I want to log out of the portal, so that I can protect my data.

Acceptance Criteria:

Logout option is visible on all doctor pages.

Session ends when logout is clicked.

Redirects to the login page after logout.

No appointment data should be accessible after logout.

Priority: High Story Points: 2 Notes: Handle session timeout for inactive users.

User Story 3: View Appointment Calendar

Title: As a doctor, I want to view my appointment calendar, so that I can stay organized.

Acceptance Criteria:

Doctor can view daily, weekly, or monthly appointment views.

Appointments show patient name, time, and status.

Past and upcoming appointments are clearly separated.

Calendar updates dynamically when new bookings occur.

Priority: High Story Points: 5 Notes: Consider integrating a calendar UI library for better UX.

User Story 4: Mark Unavailability

Title: As a doctor, I want to mark my unavailability, so that patients can only book available slots.

Acceptance Criteria:

Doctor can select specific dates/times as unavailable.

System blocks those slots from patient booking.

Unavailable slots should be visually indicated on the calendar.

Patients attempting to book during unavailable slots see an error or restriction.

Priority: High Story Points: 5 Notes: Must handle recurring unavailability (e.g., weekly off-days).

User Story 5: Update Doctor Profile

Title: As a doctor, I want to update my profile with specialization and contact information, so that patients have up-to-date information.

Acceptance Criteria:

Doctor can edit specialization, email, phone, and other details.

Updates are validated and saved in MySQL.

Patients viewing the doctor list see updated info.

Confirmation is displayed after successful update.

Priority: Medium Story Points: 4 Notes: Consider input validation and prevent duplicate emails.

User Story 6: View Patient Details for Appointments

Title: As a doctor, I want to view the patient details for upcoming appointments, so that I can be prepared.

Acceptance Criteria:

Doctor can view patient name, contact info, and appointment reason.

Only patients assigned to the doctor are visible.

Data is fetched securely from MySQL.

Sensitive patient data must be protected.

Priority: Medium Story Points: 3 Notes: Ensure HIPAA-like privacy considerations.

