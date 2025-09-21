# Music Store API Documentation

## Base URL
```
http://localhost:8082
```

## Overview
This API provides endpoints for a comprehensive music store application with support for user authentication, music management, cart operations, order processing, reviews, and administrative functions. The API features an improved rating system with cached averages and optimized cart functionality for better performance.

## Recent Updates (September 2025)
- **Enhanced Rating System**: Music tracks now store average ratings and review counts for better performance
- **Optimized Cart Operations**: Fixed LazyInitializationException and improved cart total calculations
- **Improved Checkout Process**: Fixed null value constraints and enhanced order processing
- **Better Error Handling**: Added specific HTTP status codes and detailed error messages

## Authentication
Most endpoints require JWT authentication via the `Authorization` header:
```
Authorization: Bearer <jwt_token>
```

## Role-Based Access Control
- **PUBLIC**: Accessible without authentication
- **CUSTOMER**: Requires CUSTOMER role
- **ARTIST**: Requires ARTIST role  
- **STAFF**: Requires STAFF role
- **ADMIN**: Requires ADMIN role

---

## Authentication Endpoints

### 1. User Login
**Endpoint:** `POST /api/auth/login`

**Access Level:** PUBLIC

**Description:** Authenticate user and receive JWT token

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "string (required, 3-50 characters)",
  "password": "string (required, min 6 characters)"
}
```

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "CUSTOMER",
    "firstName": "John",
    "lastName": "Doe",
    "artistName": null,
    "cover": null
  }
}
```

**Error Responses:**
- **400**: Invalid username or password
- **500**: Internal server error

### 2. User Registration
**Endpoint:** `POST /api/auth/register`

**Access Level:** PUBLIC

**Description:** Register a new user (Customer or Artist)

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "string (required, 3-50 characters)",
  "password": "string (required, min 6 characters)",
  "email": "string (required, valid email format)",
  "role": "string (required, 'CUSTOMER' or 'ARTIST')",
  "firstName": "string (optional)",
  "lastName": "string (optional)",
  "artistName": "string (optional, required for artists)",
  "cover": "string (optional, bio/description for artists)"
}
```

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 2,
    "username": "new_user",
    "email": "newuser@example.com",
    "role": "CUSTOMER",
    "firstName": "New",
    "lastName": "User"
  }
}
```

**Error Responses:**
- **400**: Validation errors or user already exists
- **500**: Internal server error

---

## Music Endpoints

### 1. Get All Music
**Endpoint:** `GET /api/music`

**Access Level:** PUBLIC

**Description:** Retrieve all music tracks with enhanced rating information

**Query Parameters:**
- `page` (optional): Page number for pagination (default: 0)
- `size` (optional): Number of items per page (default: 10)
- `sort` (optional): Sort field (default: created date)

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Song Title",
      "description": "Song description",
      "price": 9.99,
      "imageUrl": "/uploads/covers/song.jpg",
      "audioFilePath": "/uploads/music/song.mp3",
      "category": "Pop",
      "artist": "artist_username",
      "artistName": "Artist Display Name",
      "albumName": "Album Name",
      "genre": "Pop",
      "releaseYear": 2024,
      "averageRating": 4.25,
      "totalReviews": 12,
      "createdAt": "2024-01-15T10:30:00"
    }
  ],
  "totalElements": 50,
  "totalPages": 5,
  "size": 10,
  "number": 0
}
```

### 2. Get Music by ID
**Endpoint:** `GET /api/music/{id}`

**Access Level:** PUBLIC

**Description:** Get detailed information about a specific music track

**Path Parameters:**
- `id`: Music ID

**Success Response (200):**
```json
{
  "id": 1,
  "name": "Song Title",
  "description": "Detailed song description",
  "price": 9.99,
  "imageUrl": "/uploads/covers/song.jpg",
  "audioFilePath": "/uploads/music/song.mp3",
  "category": "Pop",
  "artist": "artist_username",
  "albumName": "Album Name",
  "genre": "Pop",
  "releaseYear": 2024,
  "averageRating": 4.25,
  "totalReviews": 12,
  "createdAt": "2024-01-15T10:30:00"
}
```

**Error Responses:**
- **404**: Music not found

---

## Cart Endpoints (Enhanced)

### 1. Get User Cart
**Endpoint:** `GET /api/cart`

**Access Level:** CUSTOMER

**Description:** Retrieve user's current cart with all items

**Success Response (200):**
```json
{
  "id": 1,
  "customerUsername": "john_doe",
  "items": [
    {
      "id": 1,
      "music": {
        "id": 5,
        "name": "Song Title",
        "artist": "artist_username",
        "price": 9.99,
        "imageUrl": "/uploads/covers/song.jpg"
      },
      "unitPrice": 9.99,
      "totalPrice": 9.99
    }
  ],
  "total": 19.98
}
```

**Error Responses:**
- **401**: Authentication required

### 2. Add to Cart
**Endpoint:** `POST /api/cart/add/{musicId}`

**Access Level:** CUSTOMER

**Description:** Add a music track to user's cart

**Path Parameters:**
- `musicId`: ID of the music to add

**Success Response (200):**
```json
{
  "id": 1,
  "customerUsername": "john_doe",
  "items": [
    {
      "id": 1,
      "music": {
        "id": 5,
        "name": "Song Title",
        "artist": "artist_username",
        "price": 9.99,
        "imageUrl": "/uploads/covers/song.jpg"
      },
      "unitPrice": 9.99,
      "totalPrice": 9.99
    }
  ],
  "total": 9.99
}
```

**Error Responses:**
- **401**: Authentication required
- **404**: Music not found
- **409**: Music already in cart or already purchased

### 3. Remove from Cart
**Endpoint:** `DELETE /api/cart/remove/{itemId}`

**Access Level:** CUSTOMER

**Description:** Remove an item from user's cart

**Path Parameters:**
- `itemId`: Cart item ID to remove

**Success Response (200):**
```json
{
  "id": 1,
  "customerUsername": "john_doe",
  "items": [],
  "total": 0.00
}
```

**Error Responses:**
- **401**: Authentication required
- **404**: Cart item not found
- **403**: Unauthorized to remove this item

### 4. Clear Cart
**Endpoint:** `POST /api/cart/clear`

**Access Level:** CUSTOMER

**Description:** Remove all items from user's cart

**Success Response (200):**
```json
{
  "id": 1,
  "customerUsername": "john_doe",
  "items": [],
  "total": 0.00
}
```

**Error Responses:**
- **401**: Authentication required
- **500**: Failed to clear cart

### 5. Checkout Cart
**Endpoint:** `POST /api/cart/checkout`

**Access Level:** CUSTOMER

**Description:** Process cart checkout and create order (Enhanced with proper total calculation)

**Success Response (200):**
```json
{
  "message": "Checkout successful",
  "orderId": 123
}
```

**Error Responses:**
- **401**: Authentication required
- **400**: Cart is empty or invalid cart state
- **500**: Failed to process checkout

---

## Review Endpoints (Enhanced Rating System)

### 1. Create Review
**Endpoint:** `POST /api/reviews/music/{musicId}`

**Access Level:** CUSTOMER

**Description:** Create a review for a music track (automatically updates music rating statistics)

**Path Parameters:**
- `musicId`: ID of the music to review

**Request Body:**
```json
{
  "rating": 5,
  "comment": "Great song! Love it."
}
```

**Success Response (200):**
```json
{
  "id": 1,
  "musicId": 5,
  "username": "john_doe",
  "customerName": "John Doe",
  "rating": 5,
  "comment": "Great song! Love it.",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "isOwnReview": true
}
```

**Error Responses:**
- **401**: Authentication required
- **400**: Invalid rating (must be 1-5) or user already reviewed
- **404**: Music not found

### 2. Update Review
**Endpoint:** `PUT /api/reviews/{reviewId}`

**Access Level:** CUSTOMER

**Description:** Update an existing review (automatically recalculates music rating statistics)

**Path Parameters:**
- `reviewId`: ID of the review to update

**Request Body:**
```json
{
  "rating": 4,
  "comment": "Updated review comment"
}
```

**Success Response (200):**
```json
{
  "id": 1,
  "musicId": 5,
  "username": "john_doe",
  "customerName": "John Doe",
  "rating": 4,
  "comment": "Updated review comment",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T11:00:00",
  "isOwnReview": true
}
```

**Error Responses:**
- **401**: Authentication required
- **400**: Invalid rating or unauthorized access
- **404**: Review not found

### 3. Delete Review
**Endpoint:** `DELETE /api/reviews/{reviewId}`

**Access Level:** CUSTOMER

**Description:** Delete a review (automatically recalculates music rating statistics)

**Path Parameters:**
- `reviewId`: ID of the review to delete

**Success Response (200):**
```json
{
  "message": "Review deleted successfully"
}
```

**Error Responses:**
- **401**: Authentication required
- **403**: Can only delete own reviews
- **404**: Review not found

### 4. Get Reviews for Music
**Endpoint:** `GET /api/reviews/music/{musicId}`

**Access Level:** PUBLIC

**Description:** Get paginated reviews for a specific music track

**Path Parameters:**
- `musicId`: ID of the music

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "musicId": 5,
      "username": "john_doe",
      "customerName": "John Doe",
      "rating": 5,
      "comment": "Great song!",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00",
      "isOwnReview": false
    }
  ],
  "totalElements": 12,
  "totalPages": 2,
  "size": 10,
  "number": 0
}
```

### 5. Get User's Review for Music
**Endpoint:** `GET /api/reviews/music/{musicId}/my-review`

**Access Level:** CUSTOMER

**Description:** Get the current user's review for a specific music track

**Path Parameters:**
- `musicId`: ID of the music

**Success Response (200):**
```json
{
  "id": 1,
  "musicId": 5,
  "username": "john_doe",
  "customerName": "John Doe",
  "rating": 5,
  "comment": "Great song!",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "isOwnReview": true
}
```

**Error Responses:**
- **401**: Authentication required
- **404**: Review not found

### 6. Get Review Statistics
**Endpoint:** `GET /api/reviews/music/{musicId}/stats`

**Access Level:** PUBLIC

**Description:** Get comprehensive review statistics for a music track (uses cached data for better performance)

**Path Parameters:**
- `musicId`: ID of the music

**Success Response (200):**
```json
{
  "totalReviews": 12,
  "averageRating": 4.25,
  "ratingDistribution": {
    "1": 0,
    "2": 1,
    "3": 2,
    "4": 4,
    "5": 5
  }
}
```

---

## Order Endpoints (Enhanced)

### 1. Get User Orders
**Endpoint:** `GET /api/orders`

**Access Level:** CUSTOMER

**Description:** Get user's order history with pagination

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "orderDate": "2024-01-15T10:30:00",
      "status": "COMPLETED",
      "totalAmount": 29.97,
      "items": [
        {
          "id": 1,
          "musicName": "Song Title",
          "artistName": "Artist Name",
          "price": 9.99,
          "quantity": 1
        }
      ]
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

### 2. Get Order Details
**Endpoint:** `GET /api/orders/{orderId}`

**Access Level:** CUSTOMER

**Description:** Get detailed information about a specific order

**Path Parameters:**
- `orderId`: ID of the order

**Success Response (200):**
```json
{
  "id": 1,
  "orderDate": "2024-01-15T10:30:00",
  "status": "COMPLETED",
  "totalAmount": 29.97,
  "paymentMethod": "CREDIT_CARD",
  "items": [
    {
      "id": 1,
      "musicName": "Song Title",
      "artistName": "Artist Name",
      "price": 9.99,
      "quantity": 1,
      "totalPrice": 9.99
    }
  ]
}
```

**Error Responses:**
- **401**: Authentication required
- **403**: Can only view own orders
- **404**: Order not found

---

## Technical Implementation Details

### Enhanced Rating System
- **Cached Averages**: Music tracks store `averageRating` and `totalReviews` fields for instant access
- **Automatic Updates**: Rating statistics are recalculated automatically when reviews are created, updated, or deleted
- **Performance Optimized**: No need to calculate averages on-demand, resulting in faster API responses
- **Data Integrity**: Rating updates are atomic and handle edge cases gracefully

### Improved Cart Operations
- **Lazy Loading Fixed**: Resolved Hibernate LazyInitializationException with proper eager fetching
- **Better Error Handling**: Specific HTTP status codes and detailed error messages
- **Total Calculation**: Fixed null value constraints in checkout process
- **Transaction Safety**: All cart operations are properly transactional

### Database Schema Updates
```sql
-- Music table enhancements
ALTER TABLE music ADD COLUMN average_rating DECIMAL(3,2) DEFAULT 0.0;
ALTER TABLE music ADD COLUMN total_reviews INTEGER DEFAULT 0;

-- These fields are automatically maintained by the application
```

### Error Handling Standards
- **401**: Authentication required
- **403**: Forbidden/Unauthorized access
- **404**: Resource not found
- **409**: Conflict (e.g., already in cart)
- **400**: Bad request/validation errors
- **500**: Internal server error

---

## Security Features
- JWT-based authentication
- Role-based access control
- CORS enabled for frontend integration
- Request validation and sanitization
- Secure password handling

## Performance Optimizations
- Cached rating calculations
- Optimized database queries with eager fetching
- Efficient pagination
- Transactional consistency

---

## Playlist Endpoints

### Overview
The playlist functionality allows customers to create and manage personal music collections. Users can only add music tracks they have purchased to their playlists, ensuring proper licensing compliance.

### Key Features
- Create, read, update, and delete playlists
- Add/remove purchased music tracks to/from playlists
- Automatic track count management
- User-specific playlist isolation (security)
- Duplicate name prevention per user

---

### 1. Create Playlist
**Endpoint:** `POST /api/playlists`

**Access Level:** CUSTOMER

**Description:** Create a new playlist for the authenticated customer

**Request Headers:**
```
Content-Type: application/json
Authorization: Bearer <jwt_token>
```

**Request Body:**
```json
{
  "name": "string (required, max 100 characters)"
}
```

**Success Response (201):**
```json
{
  "success": true,
  "message": "Playlist created successfully",
  "data": {
    "id": 1,
    "name": "My Favorite Songs",
    "customerId": 1,
    "customerUsername": "john_doe",
    "trackCount": 0,
    "createdAt": "2024-01-15 10:30:00",
    "updatedAt": "2024-01-15 10:30:00"
  }
}
```

**Error Responses:**
- **400**: Validation errors (empty name, name too long)
- **401**: Authentication required
- **404**: Customer not found
- **409**: Playlist with same name already exists
- **500**: Internal server error

### 2. Get All User Playlists
**Endpoint:** `GET /api/playlists`

**Access Level:** CUSTOMER

**Description:** Retrieve all playlists belonging to the authenticated customer

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Playlists retrieved successfully",
  "count": 2,
  "data": [
    {
      "id": 1,
      "name": "My Favorite Songs",
      "customerId": 1,
      "customerUsername": "john_doe",
      "trackCount": 5,
      "createdAt": "2024-01-15 10:30:00",
      "updatedAt": "2024-01-15 11:45:00"
    },
    {
      "id": 2,
      "name": "Workout Mix",
      "customerId": 1,
      "customerUsername": "john_doe",
      "trackCount": 8,
      "createdAt": "2024-01-16 09:15:00",
      "updatedAt": "2024-01-16 14:20:00"
    }
  ]
}
```

**Error Responses:**
- **401**: Authentication required
- **404**: Customer not found
- **500**: Internal server error

### 3. Get Playlist with Music Tracks
**Endpoint:** `GET /api/playlists/{id}`

**Access Level:** CUSTOMER

**Description:** Retrieve a specific playlist with all its music tracks (only owner can access)

**Path Parameters:**
- `id`: Playlist ID

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Playlist retrieved successfully",
  "data": {
    "id": 1,
    "name": "My Favorite Songs",
    "customerId": 1,
    "customerUsername": "john_doe",
    "trackCount": 2,
    "createdAt": "2024-01-15 10:30:00",
    "updatedAt": "2024-01-15 11:45:00",
    "musics": [
      {
        "id": 5,
        "name": "Amazing Song",
        "description": "A wonderful track",
        "price": 9.99,
        "imageUrl": "/uploads/covers/song1.jpg",
        "audioFilePath": "/uploads/music/song1.mp3",
        "category": "Pop",
        "artist": "artist_username",
        "album": "Greatest Hits",
        "genre": "Pop",
        "releaseYear": 2024,
        "createdAt": "2024-01-10T08:00:00"
      },
      {
        "id": 8,
        "name": "Another Hit",
        "description": "Another great song",
        "price": 12.99,
        "imageUrl": "/uploads/covers/song2.jpg",
        "audioFilePath": "/uploads/music/song2.mp3",
        "category": "Rock",
        "artist": "rock_artist",
        "album": "Rock Collection",
        "genre": "Rock",
        "releaseYear": 2023,
        "createdAt": "2024-01-12T14:30:00"
      }
    ]
  }
}
```

**Error Responses:**
- **401**: Authentication required
- **404**: Playlist not found or not owned by customer
- **500**: Internal server error

### 4. Update Playlist Name
**Endpoint:** `PUT /api/playlists/{id}`

**Access Level:** CUSTOMER

**Description:** Update the name of an existing playlist (only owner can update)

**Path Parameters:**
- `id`: Playlist ID

**Request Headers:**
```
Content-Type: application/json
Authorization: Bearer <jwt_token>
```

**Request Body:**
```json
{
  "name": "string (required, max 100 characters)"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Playlist updated successfully",
  "data": {
    "id": 1,
    "name": "Updated Playlist Name",
    "customerId": 1,
    "customerUsername": "john_doe",
    "trackCount": 5,
    "createdAt": "2024-01-15 10:30:00",
    "updatedAt": "2024-01-15 12:00:00"
  }
}
```

**Error Responses:**
- **400**: Validation errors or name conflicts
- **401**: Authentication required
- **404**: Playlist not found or not owned by customer
- **409**: Playlist with same name already exists
- **500**: Internal server error

### 5. Delete Playlist
**Endpoint:** `DELETE /api/playlists/{id}`

**Access Level:** CUSTOMER

**Description:** Delete a playlist (only owner can delete)

**Path Parameters:**
- `id`: Playlist ID

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Playlist deleted successfully"
}
```

**Error Responses:**
- **401**: Authentication required
- **404**: Playlist not found or not owned by customer
- **500**: Internal server error

### 6. Add Music to Playlist
**Endpoint:** `POST /api/playlists/{id}/music/{musicId}`

**Access Level:** CUSTOMER

**Description:** Add a purchased music track to a playlist (requires music ownership)

**Path Parameters:**
- `id`: Playlist ID
- `musicId`: Music track ID

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Music added to playlist successfully",
  "data": {
    "id": 1,
    "name": "My Favorite Songs",
    "customerId": 1,
    "customerUsername": "john_doe",
    "trackCount": 3,
    "createdAt": "2024-01-15 10:30:00",
    "updatedAt": "2024-01-15 12:15:00",
    "musics": [
      {
        "id": 5,
        "name": "Amazing Song",
        "description": "A wonderful track",
        "price": 9.99,
        "imageUrl": "/uploads/covers/song1.jpg",
        "audioFilePath": "/uploads/music/song1.mp3",
        "category": "Pop",
        "artist": "artist_username",
        "album": "Greatest Hits",
        "genre": "Pop",
        "releaseYear": 2024,
        "createdAt": "2024-01-10T08:00:00"
      }
    ]
  }
}
```

**Error Responses:**
- **400**: Music already in playlist or not purchased by user
- **401**: Authentication required
- **404**: Playlist not found, music not found, or not owned by customer
- **409**: Music already exists in playlist
- **500**: Internal server error

**Business Rules:**
- Users can only add music they have purchased
- Duplicate music tracks in the same playlist are not allowed
- Track count is automatically updated

### 7. Remove Music from Playlist
**Endpoint:** `DELETE /api/playlists/{id}/music/{musicId}`

**Access Level:** CUSTOMER

**Description:** Remove a music track from a playlist

**Path Parameters:**
- `id`: Playlist ID
- `musicId`: Music track ID

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Music removed from playlist successfully",
  "data": {
    "id": 1,
    "name": "My Favorite Songs",
    "customerId": 1,
    "customerUsername": "john_doe",
    "trackCount": 2,
    "createdAt": "2024-01-15 10:30:00",
    "updatedAt": "2024-01-15 12:30:00",
    "musics": [
      {
        "id": 8,
        "name": "Another Hit",
        "description": "Another great song",
        "price": 12.99,
        "imageUrl": "/uploads/covers/song2.jpg",
        "audioFilePath": "/uploads/music/song2.mp3",
        "category": "Rock",
        "artist": "rock_artist",
        "album": "Rock Collection",
        "genre": "Rock",
        "releaseYear": 2023,
        "createdAt": "2024-01-12T14:30:00"
      }
    ]
  }
}
```

**Error Responses:**
- **400**: Music not in playlist
- **401**: Authentication required
- **404**: Playlist not found, music not found, or not owned by customer
- **500**: Internal server error

---

## Playlist Technical Implementation

### Database Schema
```sql
-- Playlists table
CREATE TABLE playlists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    customer_id BIGINT NOT NULL,
    track_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    INDEX idx_customer_name (customer_id, name)
);

-- Playlist-Music junction table
CREATE TABLE playlist_music (
    playlist_id BIGINT NOT NULL,
    music_id BIGINT NOT NULL,
    PRIMARY KEY (playlist_id, music_id),
    FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
    FOREIGN KEY (music_id) REFERENCES music(id) ON DELETE CASCADE
);
```

### Security Features
- **Ownership Verification**: Users can only access/modify their own playlists
- **Purchase Validation**: Only purchased music can be added to playlists
- **JWT Authentication**: All endpoints require valid authentication
- **Input Validation**: Comprehensive validation using Jakarta Bean Validation

### Performance Optimizations
- **Lazy Loading**: Customer relationship uses lazy loading to prevent N+1 queries
- **Automatic Track Count**: Track count is maintained automatically for quick access
- **Composite Indexes**: Database indexes on (customer_id, name) for efficient queries
- **Transactional Operations**: All modifications are properly transactional

### Business Logic
- **Unique Names**: Playlist names must be unique per customer (case-insensitive)
- **Purchase Requirement**: Users can only add music they have purchased
- **Duplicate Prevention**: Same music cannot be added twice to a playlist
- **Automatic Updates**: Updated timestamps are automatically managed

---

*Last Updated: September 21, 2025*
*API Version: 2.1 (Enhanced with Playlist Management)*
