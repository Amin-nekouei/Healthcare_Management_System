# ACME Medical: Healthcare Management REST API

A Jakarta EE REST API for a healthcare system that models physicians, patients, medicines, prescriptions, medical schools and training, persists them with JPA, and protects endpoints with role-based access control.

![Java](https://img.shields.io/badge/Java-ED8B00?logo=openjdk&logoColor=white)
![Jakarta EE](https://img.shields.io/badge/Jakarta_EE_9-0C4E9C)
![Hibernate](https://img.shields.io/badge/Hibernate_JPA-59666C?logo=hibernate&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?logo=apachemaven&logoColor=white)

> **Status: complete.** A group project for the Enterprise Application Programming course (CST8277) at Algonquin College, built on an instructor-provided skeleton. All REST resources, JPA mappings and role-based security are implemented and covered by JUnit integration tests.

## What the project covers

- **REST API under `/api/v1`** with JAX-RS (Jersey) and Jackson for JSON.
- **Domain model with JPA/Hibernate:** physicians, patients, medicines, prescriptions (composite key), medical certificates, medical schools (public and private) and medical training.
- **Security:** HTTP Basic authentication through a custom `HttpAuthenticationMechanism` and a JPA-backed identity store, with passwords stored as PBKDF2 hashes.
- **Role-based authorization:** `@RolesAllowed` restricts operations to `ADMIN_ROLE` or `USER_ROLE`.
- **JSON error responses** through exception mappers and an error servlet.
- **API documentation:** an OpenAPI description with a bundled Swagger UI page.
- **72 integration tests** with JUnit 5 and a Jersey client, testing every resource as both an admin and a regular user.

## Example endpoints

| Method | Path | Role |
|---|---|---|
| `GET` | `/api/v1/physician/{id}` | Admin or User |
| `PUT` | `/api/v1/physician/{physicianId}/patient/{patientId}/medicine` | Admin |
| `GET` | `/api/v1/medicalschool/{medicalSchoolId}` | Admin or User |
| `PUT` / `DELETE` | `/api/v1/medicalschool/{medicalSchoolId}` | Admin |
| `POST` | `/api/v1/medicalschool/{medicalSchoolId}/medicaltraining` | Admin |

## Tech stack

Java · Jakarta EE 9 (JAX-RS, CDI, EJB, JPA, Jakarta Security) · Hibernate 5.6 · Jackson · MySQL 8 · Payara Server · Maven · JUnit 5 · Log4j 2 · OpenAPI / Swagger UI

## Running it

1. Install MySQL 8, Payara Server and JDK 11 or later.
2. Create the `acmemedical` database and run the SQL scripts in `Scripts/`.
3. Check the connection pool in `src/main/webapp/WEB-INF/payara-resources.xml`.
4. Build with `mvn clean package` and deploy the WAR from `target/` to Payara.
5. With the server running, run the tests with `mvn test`.

## Project structure

```
src/main/java/acmemedical/
├── entity/      # JPA entities and listeners
├── ejb/         # ACMEMedicalService: business logic and transactions
├── rest/        # JAX-RS config, resources, exception mappers, serializers
├── security/    # Custom authentication mechanism and identity store
└── utility/     # Constants, PBKDF2 hash generator, JSON error servlet
src/test/java/acmemedical/   # JUnit 5 integration tests
Scripts/                     # SQL setup scripts
```

## Team

| Member | Contribution |
|---|---|
| **Amin Nekouei** · [LinkedIn](https://www.linkedin.com/in/amin-nekouei) · [GitHub](https://github.com/Amin-nekouei) | Medicine and MedicalSchool REST resources, all 72 JUnit tests, bug fixes |
| Kyung A Jang | Physician, Prescription and Patient REST resources, security user and role annotations |
| Yanrong Cen | MedicalCertificate and MedicalTraining REST resources, bug fixes |
