# Admin Staff and Admin Registration API Documentation

## Overview
This documentation covers the admin functionality for registering and managing staff members and admin users. These endpoints are restricted to users with `ADMIN` role and provide database persistence with proper validation and encoding.

## Base URL
```
/api/admin
```

## Authentication
All endpoints require:
- **Authentication**: Bearer token
- **Authorization**: `ROLE_ADMIN`
- **Headers**: 
  ```
  Authorization: Bearer <your-jwt-token>
  Content-Type: application/json
  ```

---

## Staff Management Endpoints

### 1. Register New Staff Member

**Endpoint:** `POST /api/admin/staff/register`

**Description:** Creates a new staff member account with database persistence, password encoding, and automatic timestamp generation.

**Request Body:**
```json
{
  "username": "staff_username",
  "password": "secure_password123",
  "email": "staff@musicstore.com",
  "firstName": "John",
  "lastName": "Doe",
  "position": "Support Specialist"
}
```

**Required Fields:**
- `username` (string) - Must be unique
- `password` (string) - Will be BCrypt encoded
- `email` (string) - Must be valid email format and unique
- `firstName` (string)
- `lastName` (string)

**Optional Fields:**
- `position` (string) - Job title/position

**Success Response (200 OK):**
```json
{
  "id": 15,
  "username": "staff_username",
  "email": "staff@musicstore.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "ROLE_STAFF",
  "enabled": true,
  "createdAt": "2025-10-01T10:30:00",
  "position": "Support Specialist"
}
```

**Error Responses:**
- `400 Bad Request`: Validation errors or username/email already exists
- `401 Unauthorized`: Invalid or missing authentication token
- `403 Forbidden`: Insufficient permissions (not admin)

**Example Error Response:**
```json
{
  "message": "Username already exists",
  "timestamp": "2025-10-01T10:30:00"
}
```

---

### 2. Register New Admin

**Endpoint:** `POST /api/admin/admin/register`

**Description:** Creates a new admin user account with database persistence, password encoding, and automatic timestamp generation.

**Request Body:**
```json
{
  "username": "admin_username",
  "password": "secure_admin_password123",
  "email": "admin@musicstore.com",
  "firstName": "Jane",
  "lastName": "Smith"
}
```

**Required Fields:**
- `username` (string) - Must be unique
- `password` (string) - Will be BCrypt encoded
- `email` (string) - Must be valid email format and unique
- `firstName` (string)
- `lastName` (string)

**Success Response (200 OK):**
```json
{
  "id": 8,
  "username": "admin_username",
  "email": "admin@musicstore.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "role": "ROLE_ADMIN",
  "enabled": true,
  "createdAt": "2025-10-01T10:35:00"
}
```

---

## Database Persistence Details

### Staff Table Schema
The staff registration saves to the `staff` table with the following fields:
- `id` (BIGINT, auto-generated primary key)
- `username` (VARCHAR, unique constraint)
- `password` (VARCHAR, BCrypt encoded)
- `email` (VARCHAR, unique constraint)
- `first_name` (VARCHAR)
- `last_name` (VARCHAR)
- `role` (VARCHAR, defaults to "ROLE_STAFF")
- `enabled` (BOOLEAN, defaults to true)
- `created_at` (TIMESTAMP, auto-set via @PrePersist)
- `position` (VARCHAR, nullable)

### Admin Table Schema
The admin registration saves to the `admins` table with the following fields:
- `id` (BIGINT, auto-generated primary key)
- `username` (VARCHAR, unique constraint)
- `password` (VARCHAR, BCrypt encoded)
- `email` (VARCHAR, unique constraint)
- `first_name` (VARCHAR)
- `last_name` (VARCHAR)
- `role` (VARCHAR, defaults to "ROLE_ADMIN")
- `enabled` (BOOLEAN, defaults to true)
- `created_at` (TIMESTAMP, auto-set via @PrePersist)

---

## Implementation Details

### Service Layer Architecture
- **Staff Registration**: Uses `StaffService.createStaff()` directly for database persistence
- **Admin Registration**: Uses `AdminService.createAdmin()` directly for database persistence
- **Password Encoding**: BCrypt encoding handled automatically in service layer
- **Validation**: Unique constraints enforced at database level with proper error handling

### Automatic Features
- **Timestamps**: `createdAt` field automatically set via JPA `@PrePersist` annotation
- **Role Assignment**: Default roles ("ROLE_STAFF", "ROLE_ADMIN") assigned automatically
- **Account Status**: New accounts default to `enabled: true`
- **Transaction Management**: Spring `@Transactional` ensures data integrity

### Security Features
- **Password Encryption**: All passwords are hashed using BCrypt before storage
- **Unique Constraints**: Username and email must be unique across the system
- **Role-based Access**: Only admins can access these registration endpoints
- **Input Validation**: All inputs are validated before processing

---

## Error Handling

### Common HTTP Status Codes
- **200 OK**: Successful registration and data saved to database
- **400 Bad Request**: Validation errors, duplicate username/email
- **401 Unauthorized**: Missing or invalid authentication token
- **403 Forbidden**: Insufficient permissions (not admin)
- **500 Internal Server Error**: Database or server error

### Error Response Format
```json
{
  "message": "Detailed error description",
  "timestamp": "2025-10-01T10:50:00"
}
```

### Common Error Messages
- "Username already exists"
- "Email already exists"
- "Invalid email format"
- "Access denied - Admin role required"

---

## Usage Examples

### Example 1: Register a New Staff Member
```bash
curl -X POST "http://localhost:8080/api/admin/staff/register" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_staff",
    "password": "password123",
    "email": "john@musicstore.com",
    "firstName": "John",
    "lastName": "Doe",
    "position": "Customer Support"
  }'
```

**Expected Database Result:**
```sql
INSERT INTO staff (username, password, email, first_name, last_name, role, enabled, created_at, position)
VALUES ('john_staff', '$2a$10$...', 'john@musicstore.com', 'John', 'Doe', 'ROLE_STAFF', true, '2025-10-01 10:30:00', 'Customer Support');
```

### Example 2: Register a New Admin
```bash
curl -X POST "http://localhost:8080/api/admin/admin/register" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jane_admin",
    "password": "adminpass123",
    "email": "jane@musicstore.com",
    "firstName": "Jane",
    "lastName": "Smith"
  }'
```

**Expected Database Result:**
```sql
INSERT INTO admins (username, password, email, first_name, last_name, role, enabled, created_at)
VALUES ('jane_admin', '$2a$10$...', 'jane@musicstore.com', 'Jane', 'Smith', 'ROLE_ADMIN', true, '2025-10-01 10:35:00');
```

---

## Verification of Database Persistence

After successful registration, you can verify the data was saved by:

1. **Checking the database directly:**
   ```sql
   SELECT * FROM staff WHERE username = 'john_staff';
   SELECT * FROM admins WHERE username = 'jane_admin';
   ```

2. **Verifying password encoding:**
   - Passwords in database will be BCrypt hashed (starting with `$2a$10$`)
   - Original passwords are never stored in plain text

3. **Confirming automatic timestamps:**
   - `created_at` field will be automatically populated
   - No manual timestamp handling required

---

## Key Implementation Changes

### ✅ What's Working
- **Direct Service Usage**: Registration endpoints use dedicated service methods for reliable database persistence
- **Password Security**: BCrypt encoding implemented and working
- **Auto-Timestamps**: `@PrePersist` annotations ensure automatic `createdAt` population
- **Validation**: Unique constraints on username/email with proper error handling
- **Transaction Safety**: Spring's transaction management ensures data integrity

### 🔧 Simplified Design
- **Streamlined DTOs**: Request/response objects only include implemented fields
- **Direct Persistence**: No intermediate layers, services save directly to database
- **Essential Fields Only**: Focus on core fields needed for staff/admin registration
- **Automatic Defaults**: Role assignment and account enabling handled automatically

This implementation provides reliable, secure staff and admin registration with confirmed database persistence and proper validation.
