User Story 1: Doctor Login

Title: As a doctor, I want to log into the portal, so that I can manage my appointments.

Acceptance Criteria:

Doctor must provide valid username/email and password.

Successful login redirects to the Doctor Dashboard.

Invalid login shows an error message.

Session persists until logout.

Priority: High
Story Points: 3
Notes: Ensure secure authentication with password hashing.

User Story 2: Doctor Logout

Title: As a doctor, I want to log out of the portal, so that I can protect my data.

Acceptance Criteria:

Logout option is visible on all doctor pages.

Session ends when logout is clicked.

Redirects to the login page after logout.

No appointment data should be accessible after logout.

Priority: High
Story Points: 2
Notes: Handle session timeout for inactive users.

User Story 3: View Appointment Calendar

Title: As a doctor, I want to view my appointment calendar, so that I can stay organized.

Acceptance Criteria:

Doctor can view daily, weekly, or monthly appointment views.

Appointments show patient name, time, and status.

Past and upcoming appointments are clearly separated.

Calendar updates dynamically when new bookings occur.

Priority: High
Story Points: 5
Notes: Consider integrating a calendar UI library for better UX.

User Story 4: Mark Unavailability

Title: As a doctor, I want to mark my unavailability, so that patients can only book available slots.

Acceptance Criteria:

Doctor can select specific dates/times as unavailable.

System blocks those slots from patient booking.

Unavailable slots should be visually indicated on the calendar.

Patients attempting to book during unavailable slots see an error or restriction.

Priority: High
Story Points: 5
Notes: Must handle recurring unavailability (e.g., weekly off-days).

User Story 5: Update Doctor Profile

Title: As a doctor, I want to update my profile with specialization and contact information, so that patients have up-to-date information.

Acceptance Criteria:

Doctor can edit specialization, email, phone, and other details.

Updates are validated and saved in MySQL.

Patients viewing the doctor list see updated info.

Confirmation is displayed after successful update.

Priority: Medium
Story Points: 4
Notes: Consider input validation and prevent duplicate emails.

User Story 6: View Patient Details for Appointments

Title: As a doctor, I want to view the patient details for upcoming appointments, so that I can be prepared.

Acceptance Criteria:

Doctor can view patient name, contact info, and appointment reason.

Only patients assigned to the doctor are visible.

Data is fetched securely from MySQL.

Sensitive patient data must be protected.

Priority: Medium
Story Points: 3
Notes: Ensure HIPAA-like privacy considerations.
