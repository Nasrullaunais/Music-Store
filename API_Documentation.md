# Music Store API Documentation

## Base URL
```
http://localhost:8080
```

## Authentication Endpoints

### 1. User Registration
**Endpoint:** `POST /api/auth/register`

**Description:** Register a new user (Customer or Artist only through public endpoint)

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "string (required, 3-50 characters)",
  "password": "string (required, min 6 characters)",
  "email": "string (required, valid email)",
  "role": "string (required, 'CUSTOMER' or 'ARTIST')",
  "firstName": "string (optional)",
  "lastName": "string (optional)",
  "artistName": "string (optional, for artists)",
  "cover": "string (optional, for artists)"
}
```

**Success Response (200):**
```json
{
  "token": "JWT_TOKEN_STRING",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "role": "CUSTOMER",
    "firstName": "John",
    "lastName": "Doe",
    "artistName": null,
    "cover": null
  }
}
```

**Error Response (400):**
```json
{
  "message": "Registration failed: Username already exists"
}
```

### 2. User Login
**Endpoint:** `POST /api/auth/login`

**Description:** Authenticate user and get JWT token

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "string (required)",
  "password": "string (required)"
}
```

**Success Response (200):**
```json
{
  "token": "JWT_TOKEN_STRING",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "role": "CUSTOMER",
    "firstName": "John",
    "lastName": "Doe",
    "artistName": null,
    "cover": null
  }
}
```

**Error Response (400):**
```json
{
  "message": "Invalid username or password"
}
```

### 3. Get Current User
**Endpoint:** `GET /api/auth/me`

**Description:** Get current authenticated user information

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
```

**Success Response (200):**
```json
{
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "role": "CUSTOMER",
  "firstName": "John",
  "lastName": "Doe",
  "artistName": null,
  "cover": null
}
```

**Error Response (401):**
```json
{
  "message": "Not authenticated"
}
```

### 4. Validate Token
**Endpoint:** `POST /api/auth/validate-token`

**Description:** Validate JWT token and get user information

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
```

**Success Response (200):**
```json
{
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "role": "CUSTOMER",
  "firstName": "John",
  "lastName": "Doe",
  "artistName": null,
  "cover": null
}
```

**Error Response (401):**
```json
{
  "message": "Invalid or expired token"
}
```

## Music Endpoints

### 1. Get All Music
**Endpoint:** `GET /api/music`

**Description:** Get paginated list of all music tracks

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size
- `search` (optional) - Search term for music title or artist

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN (optional for public access)
```

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Song Title",
      "artist": "Artist Name",
      "genre": "Pop",
      "duration": 180,
      "price": 0.99,
      "filePath": "/path/to/music/file",
      "coverArt": "/path/to/cover/art",
      "description": "Song description",
      "releaseDate": "2024-01-01",
      "approved": true
    }
  ],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0
}
```

### 2. Get Music by ID
**Endpoint:** `GET /api/music/{id}`

**Description:** Get specific music track by ID

**Path Parameters:**
- `id` (required) - Music track ID

**Success Response (200):**
```json
{
  "id": 1,
  "title": "Song Title",
  "artist": "Artist Name",
  "genre": "Pop",
  "duration": 180,
  "price": 0.99,
  "filePath": "/path/to/music/file",
  "coverArt": "/path/to/cover/art",
  "description": "Song description",
  "releaseDate": "2024-01-01",
  "approved": true
}
```

### 3. Upload Music (Artists Only)
**Endpoint:** `POST /api/music/upload`

**Description:** Upload a new music track (requires ARTIST role)

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
Content-Type: multipart/form-data
```

**Request Body (Form Data):**
- `title` (required) - Music title
- `genre` (required) - Music genre
- `price` (required) - Music price
- `description` (optional) - Music description
- `musicFile` (required) - Audio file
- `coverFile` (optional) - Cover art image file

**Success Response (200):**
```json
{
  "id": 1,
  "title": "Song Title",
  "artist": "Artist Name",
  "genre": "Pop",
  "duration": 180,
  "price": 0.99,
  "filePath": "/path/to/music/file",
  "coverArt": "/path/to/cover/art",
  "description": "Song description",
  "releaseDate": "2024-01-01",
  "approved": false
}
```

## Cart Endpoints

### 1. Get User's Cart
**Endpoint:** `GET /api/cart`

**Description:** Get current user's shopping cart

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
```

**Success Response (200):**
```json
{
  "id": 1,
  "customerId": 1,
  "items": [
    {
      "id": 1,
      "musicId": 1,
      "title": "Song Title",
      "artist": "Artist Name",
      "price": 0.99,
      "addedAt": "2024-01-01T10:00:00"
    }
  ],
  "totalPrice": 0.99,
  "itemCount": 1
}
```

### 2. Add Item to Cart
**Endpoint:** `POST /api/cart/add/{musicId}`

**Description:** Add a music track to the cart

**Path Parameters:**
- `musicId` (required) - Music track ID to add

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
```

**Success Response (200):**
```json
{
  "message": "Item added to cart successfully"
}
```

### 3. Remove Item from Cart
**Endpoint:** `DELETE /api/cart/remove/{musicId}`

**Description:** Remove a music track from the cart

**Path Parameters:**
- `musicId` (required) - Music track ID to remove

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
```

**Success Response (200):**
```json
{
  "message": "Item removed from cart successfully"
}
```

### 4. Clear Cart
**Endpoint:** `DELETE /api/cart/clear`

**Description:** Remove all items from the cart

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
```

**Success Response (200):**
```json
{
  "message": "Cart cleared successfully"
}
```

## Review Endpoints

### 1. Get Reviews for Music
**Endpoint:** `GET /api/reviews/music/{musicId}`

**Description:** Get all reviews for a specific music track

**Path Parameters:**
- `musicId` (required) - Music track ID

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "musicId": 1,
      "customerId": 1,
      "customerName": "John Doe",
      "rating": 5,
      "comment": "Great song!",
      "createdAt": "2024-01-01T10:00:00"
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

### 2. Create Review
**Endpoint:** `POST /api/reviews`

**Description:** Create a new review for a music track

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
Content-Type: application/json
```

**Request Body:**
```json
{
  "musicId": 1,
  "rating": 5,
  "comment": "Great song!"
}
```

**Success Response (200):**
```json
{
  "id": 1,
  "musicId": 1,
  "customerId": 1,
  "customerName": "John Doe",
  "rating": 5,
  "comment": "Great song!",
  "createdAt": "2024-01-01T10:00:00"
}
```

## Artist Endpoints

### 1. Get Artist Profile
**Endpoint:** `GET /api/artist/profile`

**Description:** Get current artist's profile (requires ARTIST role)

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
```

**Success Response (200):**
```json
{
  "id": 1,
  "username": "artist1",
  "email": "artist@example.com",
  "artistName": "Artist Name",
  "firstName": "John",
  "lastName": "Artist",
  "cover": "/path/to/cover/image",
  "musicCount": 5,
  "totalRevenue": 15.50
}
```

### 2. Get Artist's Music
**Endpoint:** `GET /api/artist/music`

**Description:** Get all music tracks by current artist

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
```

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Song Title",
      "genre": "Pop",
      "duration": 180,
      "price": 0.99,
      "approved": true,
      "uploadDate": "2024-01-01",
      "downloads": 100
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

## Customer Endpoints

### 1. Get Customer Profile
**Endpoint:** `GET /api/customer/profile`

**Description:** Get current customer's profile

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
```

**Success Response (200):**
```json
{
  "id": 1,
  "username": "customer1",
  "email": "customer@example.com",
  "firstName": "John",
  "lastName": "Customer",
  "purchaseHistory": [
    {
      "id": 1,
      "musicId": 1,
      "title": "Song Title",
      "artist": "Artist Name",
      "price": 0.99,
      "purchaseDate": "2024-01-01T10:00:00"
    }
  ]
}
```

### 2. Update Customer Profile
**Endpoint:** `PUT /api/customer/profile`

**Description:** Update current customer's profile

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Customer",
  "email": "newemail@example.com"
}
```

**Success Response (200):**
```json
{
  "message": "Profile updated successfully"
}
```

## Admin Endpoints (Requires ADMIN role)

### 1. Get System Overview
**Endpoint:** `GET /api/admin/system-overview`

**Description:** Get system statistics and overview

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
```

**Success Response (200):**
```json
{
  "totalUsers": 1000,
  "totalCustomers": 800,
  "totalArtists": 150,
  "totalStaff": 45,
  "totalAdmins": 5,
  "totalMusic": 5000,
  "pendingApprovals": 25,
  "totalRevenue": 50000.00,
  "monthlyActiveUsers": 750
}
```

### 2. Get All Users (Paginated)
**Endpoint:** `GET /api/admin/users`

**Description:** Get paginated list of all users

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
```

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Page size
- `role` (optional) - Filter by role (CUSTOMER, ARTIST, STAFF, ADMIN)

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "username": "user1",
      "email": "user@example.com",
      "role": "CUSTOMER",
      "firstName": "John",
      "lastName": "Doe",
      "enabled": true,
      "createdAt": "2024-01-01T10:00:00"
    }
  ],
  "totalElements": 1000,
  "totalPages": 50,
  "size": 20,
  "number": 0
}
```

### 3. Approve/Reject Music
**Endpoint:** `PUT /api/admin/music/{musicId}/status`

**Description:** Approve or reject music submission

**Path Parameters:**
- `musicId` (required) - Music track ID

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
Content-Type: application/json
```

**Request Body:**
```json
{
  "approved": true,
  "rejectionReason": "Optional rejection reason if approved is false"
}
```

**Success Response (200):**
```json
{
  "message": "Music status updated successfully"
}
```

## Staff Endpoints (Requires STAFF role)

### 1. Get Pending Music Approvals
**Endpoint:** `GET /api/staff/music/pending`

**Description:** Get list of music tracks pending approval

**Request Headers:**
```
Authorization: Bearer JWT_TOKEN
```

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Page size

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Song Title",
      "artist": "Artist Name",
      "genre": "Pop",
      "duration": 180,
      "uploadDate": "2024-01-01",
      "filePath": "/path/to/file"
    }
  ],
  "totalElements": 25,
  "totalPages": 2,
  "size": 20,
  "number": 0
}
```

## Error Responses

All endpoints may return the following error responses:

### 401 Unauthorized
```json
{
  "message": "Authentication required"
}
```

### 403 Forbidden
```json
{
  "message": "Access denied - insufficient permissions"
}
```

### 404 Not Found
```json
{
  "message": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "message": "Internal server error"
}
```

## Authentication Flow for React Frontend

### 1. Registration/Login Flow
```javascript
// Registration
const register = async (userData) => {
  const response = await fetch('/api/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(userData),
  });
  
  if (response.ok) {
    const data = await response.json();
    // Store token in localStorage or secure storage
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data.user));
    return data;
  }
  throw new Error('Registration failed');
};

// Login
const login = async (credentials) => {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(credentials),
  });
  
  if (response.ok) {
    const data = await response.json();
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data.user));
    return data;
  }
  throw new Error('Login failed');
};
```

### 2. Making Authenticated Requests
```javascript
const makeAuthenticatedRequest = async (url, options = {}) => {
  const token = localStorage.getItem('token');
  
  const config = {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${token}`,
    },
  };
  
  const response = await fetch(url, config);
  
  if (response.status === 401) {
    // Token expired, redirect to login
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
    return;
  }
  
  return response;
};
```

### 3. Role-Based Access Control
```javascript
const checkUserRole = (requiredRole) => {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  return user.role === requiredRole;
};

// Usage in React components
const AdminComponent = () => {
  if (!checkUserRole('ADMIN')) {
    return <div>Access Denied</div>;
  }
  
  return <div>Admin Dashboard</div>;
};
```

## CORS Configuration
The API is configured to accept requests from `http://localhost:3000` for React development. Make sure your React app runs on this port, or update the CORS configuration in the Spring Boot application.

## File Upload Considerations
When uploading music files or images:
- Maximum file size: Check application configuration
- Supported formats: MP3, WAV, FLAC for audio; JPG, PNG for images
- Files are stored in the `/uploads` directory
- Use `multipart/form-data` content type for file uploads

This documentation provides all the endpoints you need to build a complete React frontend for the Music Store application. Each endpoint includes request/response examples and proper authentication handling.
