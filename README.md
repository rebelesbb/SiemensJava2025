# Siemens Java Internship – Refactored CRUD Application

This is a fully refactored version of a Spring Boot CRUD application originally provided for the Siemens Java Internship assignment.  
It improves code quality, adds full validation, restructures async processing, and introduces complete test coverage.

---

## ✅ Assignment Goals Achieved

- ✅ Fixed logical and structural issues while maintaining original functionality
- ✅ Added input validation with Jakarta Bean Validation and regex-based email checks
- ✅ Refactored the async processing method to be thread-safe and deterministic
- ✅ Cleaned and unified REST controller logic with proper HTTP responses
- ✅ Wrote unit and integration tests with near-complete coverage
- ✅ Documented all major classes and methods

---

## 📦 Technologies Used

- Java 17
- Spring Boot 3.x
- Spring Web, Spring Data JPA
- Jakarta Validation (JSR-380)
- H2 In-Memory Database
- Lombok
- JUnit 5, Mockito
- MockMvc (Spring Test)
- Maven

---

## 🗂️ Project Structure

```
src/
├── main/
│   ├── java/com/siemens/internship/
│   │   ├── model/        # JPA Entity
│   │   ├── repository/   # JpaRepository interface
│   │   ├── service/      # Business logic + async processing
│   │   └── controller/   # REST API endpoints
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/siemens/internship/
        └── ... (unit + integration tests)
```

---

## 🧪 How to Run Tests

**Option 1 – IntelliJ:**
> Right-click `src/test/java` → Run 'All Tests with Coverage'

**Option 2 – Terminal:**
```bash
mvn test
```

---

## 🧼 Refactoring Summary

### 🧩 Controller Refactor
- Unified return types using `ResponseEntity`
- Added validation with `@Valid` and `BindingResult`
- Used correct HTTP status codes: `200`, `201`, `204`, `400`, `404`, `500`
- Improved naming, structure and clarity
- Wrote MockMvc tests for all endpoints (valid/invalid cases)

### 🔍 Validation Added
- Used `@NotBlank`, `@Size`, `@Pattern` for entity fields
- Custom regex for email validation
- Fully tested all edge cases and invalid input

### ⚙️ Async Processing
- Refactored `processItemsAsync()` using `CompletableFuture.allOf(...)`
- Ensured all items are processed and results collected
- Made method fully thread-safe (no shared mutable state)
- Covered success and failure cases via `@SpyBean` and Mockito

### 🧪 Testing
- Unit tests for validation and service logic
- Integration tests for all controller endpoints
- Edge case tests: invalid data, failed processing, not found items
- Near-full coverage

---

## 🔀 Branches

This project was developed using structured branches:
- `validation-and-tests` – input validation + testing
- `refactor-async-processing` – async logic refactor + testing
- 🔄 **Final result is merged into `master`**  
  The other branches are left intentionally to reflect clean git history and structure.

---

## 📬 Submission Notes

- The project is public at:  
  👉 [https://github.com/rebelesbb/SiemensJava2025](https://github.com/rebelesbb/SiemensJava2025)
- Please refer to the `master` branch for the final solution
- Tests can be reviewed in the `test` package and coverage verified in IntelliJ or via Maven

---

## 👤 Author

**Bogdan Călin Florin Rebeleș**  
GitHub: [https://github.com/rebelesbb](https://github.com/rebelesbb)