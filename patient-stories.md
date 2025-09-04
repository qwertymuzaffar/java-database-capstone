User Story 1: View Doctors Without Login

Title: As a patient, I want to view a list of doctors without logging in, so that I can explore options before registering.

Acceptance Criteria:

Patients can access the doctors’ list from the homepage.

Doctor details include name, specialization, and availability.

No login or registration is required to view the list.

The system prevents booking without login.

Priority: High
Story Points: 3
Notes: Optimize queries to fetch only public doctor info (exclude private data).

User Story 2: Patient Sign Up

Title: As a patient, I want to sign up using my email and password, so that I can book appointments.

Acceptance Criteria:

Patient must provide a valid email and password.

Password must meet complexity requirements.

Successful registration creates a patient record in MySQL.

Duplicate emails should not be allowed.

Priority: High
Story Points: 5
Notes: Consider email verification for security.

User Story 3: Patient Login

Title: As a patient, I want to log into the portal, so that I can manage my bookings.

Acceptance Criteria:

Patient provides valid login credentials.

Successful login redirects to the Patient Dashboard.

Invalid login shows an error message.

Session is maintained until logout.

Priority: High
Story Points: 3
Notes: Ensure secure password handling with hashing.

User Story 4: Patient Logout

Title: As a patient, I want to log out of the portal, so that I can secure my account.

Acceptance Criteria:

Logout option is visible on all dashboard pages.

Logout ends the current session.

Redirects the patient to the login page.

No booking or profile management should be accessible after logout.

Priority: High
Story Points: 2
Notes: Handle session expiration gracefully.

User Story 5: Book an Appointment

Title: As a patient, I want to log in and book an hour-long appointment with a doctor, so that I can consult with them.

Acceptance Criteria:

Patient must be logged in to book.

Appointment duration is fixed at one hour.

Patients can select a doctor and an available time slot.

Booking is saved in MySQL and confirmation is displayed.

Priority: High
Story Points: 5
Notes: Handle double-booking prevention and slot validation.

User Story 6: View Upcoming Appointments

Title: As a patient, I want to view my upcoming appointments, so that I can prepare accordingly.

Acceptance Criteria:

Patient can see a list of all upcoming confirmed appointments.

List includes doctor name, date, and time.

Only the logged-in patient’s appointments are visible.

Cancelled or past appointments are not shown in this view.

Priority: Medium
Story Points: 3
Notes: Provide filtering for upcoming vs. past appointments.
