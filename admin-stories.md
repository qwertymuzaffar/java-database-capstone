User Story 1: Admin Login

Title: As an admin, I want to log into the portal with my username and password, so that I can securely manage the platform.

Acceptance Criteria:

Admin must enter a valid username and password.

System validates credentials against stored records in MySQL.

Successful login redirects the admin to the Admin Dashboard.

Invalid credentials show an appropriate error message.

Priority: High
Story Points: 3
Notes: Consider password encryption and session handling.

User Story 2: Admin Logout

Title: As an admin, I want to log out of the portal, so that I can protect the system from unauthorized access.

Acceptance Criteria:

Logout option is available on all admin dashboard pages.

Clicking logout ends the admin session.

System redirects to the login page after logout.

No admin functionality should be accessible after logout.

Priority: High
Story Points: 2
Notes: Ensure proper session invalidation.

User Story 3: Add Doctor Profile

Title: As an admin, I want to add doctors to the portal, so that they can be registered in the system and start managing appointments.

Acceptance Criteria:

Admin provides doctor’s name, email, specialization, and other required details.

The system validates the input fields.

The doctor’s profile is saved in the MySQL database.

Confirmation message is shown after successful addition.

Priority: High
Story Points: 5
Notes: Consider input validation, duplicate entries, and email uniqueness.

User Story 4: Delete Doctor Profile

Title: As an admin, I want to delete a doctor’s profile from the portal, so that I can remove inactive or invalid records.

Acceptance Criteria:

Admin can view a list of doctors with delete options.

The system asks for confirmation before deleting.

Deleted doctor profile is removed from MySQL.

A success message is displayed after deletion.

Priority: Medium
Story Points: 3
Notes: Consider cascade delete (appointments, prescriptions) or restrict if linked records exist.

User Story 5: Run Stored Procedure for Reports

Title: As an admin, I want to run a stored procedure in the MySQL CLI, so that I can get the number of appointments per month and track usage statistics.

Acceptance Criteria:

Stored procedure is available in MySQL with proper indexing.

Procedure returns the number of appointments grouped by month.

Admin can access this report securely.

Output is displayed as a table or exported as needed.

Priority: Medium
Story Points: 4
Notes: Ensure procedure is optimized for large datasets.
