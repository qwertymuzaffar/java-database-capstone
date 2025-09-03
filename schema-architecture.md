**Section 1: Architecture Summary**

This Spring Boot application follows a layered architecture that combines both MVC and REST approaches. For the Admin and Doctor dashboards, Thymeleaf templates are rendered using MVC controllers to provide server-side HTML views. For all other modules, REST APIs are exposed to handle requests from clients. The application integrates with two databases: MySQL, which stores structured relational data such as patients, doctors, appointments, and admin records, and MongoDB, which stores unstructured document-based data such as prescriptions.

All requests are routed through a service layer, which encapsulates the business logic and ensures separation of concerns. The service layer interacts with the persistence layer using Spring Data JPA repositories for MySQL and Spring Data MongoDB repositories for MongoDB. This layered design promotes scalability, modularity, and maintainability.

**Section 2: Numbered Flow of Data and Control**

1. The user initiates an action, such as accessing the Admin Dashboard, Doctor Dashboard, or performing operations (e.g., scheduling an appointment, viewing prescriptions).

2. Depending on the action, the request is routed to either a Thymeleaf MVC controller (for dashboards) or a REST controller (for API-based operations).

3. The controller validates the request and forwards it to the service layer.

4. The service layer executes the business logic and determines whether to query or update MySQL or MongoDB.

5. For relational data (e.g., patient, doctor, appointment, admin), the service layer calls JPA repositories to interact with MySQL.

6. For document data (e.g., prescriptions), the service layer calls MongoDB repositories to interact with MongoDB.

7. The response is returned back:

  - Thymeleaf controllers render an updated HTML page for the user.

  - REST controllers return JSON responses to clients (e.g., web, mobile apps, or third-party systems).
