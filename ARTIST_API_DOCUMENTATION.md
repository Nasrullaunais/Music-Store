# Artist API Documentation

## Overview
This documentation provides comprehensive details about the Artist API endpoints in the Music Store application. Artists can manage their music uploads, track analytics, view reviews, and access dashboard statistics through these endpoints.

## Base URL
```
http://localhost:8082/api/artist
```

## Authentication
All Artist endpoints require:
- JWT authentication via `Authorization: Bearer <token>` header
- User must have `ARTIST` role
- Most endpoints use `@AuthenticationPrincipal UserDetails` to get the current artist's username

## Response Format
All endpoints return a standardized response format:

### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response data object
  }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

## HTTP Status Codes
- `200 OK` - Successful operation
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid input or validation error
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

## Music Management Endpoints

### 1. Upload Music
**Endpoint:** `POST /api/artist/music/upload`

**Description:** Upload a new music track with audio file and cover image

**Content-Type:** `multipart/form-data`

**Request Parameters:**
- `title` (String, required) - Music title
- `genre` (String, required) - Music genre
- `price` (Double, required) - Price in decimal format
- `description` (String, optional) - Music description
- `musicFile` (MultipartFile, required) - Audio file (must be audio/* content type)
- `coverImage` (MultipartFile, required) - Cover image (must be image/* content type)

**File Validation:**
- Music file must have `audio/*` content type
- Cover image must have `image/*` content type
- Files cannot be empty

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Music uploaded successfully",
  "data": {
    "id": 123,
    "name": "Song Title",
    "description": "Song description",
    "price": 9.99,
    "genre": "Pop",
    "artist": "artist_username",
    "imageUrl": "/uploads/covers/1234567890_cover.jpg",
    "audioFilePath": "/uploads/music/1234567890_song.mp3",
    "createdAt": "2025-09-23T10:30:00",
    "averageRating": 0.0,
    "totalReviews": 0
  }
}
```

**Error Examples:**
```json
// Missing required field
{
  "success": false,
  "message": "Title is required",
  "data": null
}

// Invalid file format
{
  "success": false,
  "message": "Invalid music file format. Please upload an audio file.",
  "data": null
}
```

### 2. Get My Music
**Endpoint:** `GET /api/artist/music/my-music`

**Description:** Retrieve all music tracks uploaded by the current artist

**Query Parameters:**
- `page` (int, default: 0) - Page number for pagination
- `size` (int, default: 10) - Page size for pagination

**Pagination Behavior:**
- If using default values (page=0, size=10), returns all music without pagination
- Otherwise returns paginated results with pagination metadata

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Music retrieved successfully",
  "data": {
    "music": [
      {
        "id": 123,
        "name": "Song Title",
        "description": "Song description",
        "price": 9.99,
        "genre": "Pop",
        "artist": "artist_username",
        "imageUrl": "/uploads/covers/cover.jpg",
        "audioFilePath": "/uploads/music/song.mp3",
        "createdAt": "2025-09-23T10:30:00",
        "averageRating": 4.5,
        "totalReviews": 10
      }
    ],
    "totalElements": 25,
    "totalPages": 3,
    "currentPage": 0,
    "pageSize": 10,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### 3. Get Music Count
**Endpoint:** `GET /api/artist/music/count`

**Description:** Get the total number of music tracks uploaded by the artist

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Music count retrieved successfully",
  "data": {
    "count": 25
  }
}
```

### 4. Update Music
**Endpoint:** `PUT /api/artist/music/{musicId}`

**Description:** Update details of a specific music track (artist can only update their own music)

**Path Parameters:**
- `musicId` (Long, required) - ID of the music to update

**Request Body:**
```json
{
  "name": "Updated Song Title",
  "description": "Updated description",
  "price": 12.99,
  "genre": "Rock",
  "albumName": "My Album",
  "releaseYear": 2025
}
```

**Ownership Validation:** The system automatically verifies that the music belongs to the requesting artist.

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Music updated successfully",
  "data": {
    "id": 123,
    "name": "Updated Song Title",
    "description": "Updated description",
    "price": 12.99,
    "genre": "Rock",
    "albumName": "My Album",
    "releaseYear": 2025,
    "updatedAt": "2025-09-23T11:00:00"
  }
}
```

**Error Examples:**
```json
// Music not found
{
  "success": false,
  "message": "Music not found with id: 123",
  "data": null
}

// Unauthorized access
{
  "success": false,
  "message": "You can only update your own music",
  "data": null
}
```

### 5. Delete Music
**Endpoint:** `DELETE /api/artist/music/{musicId}`

**Description:** Delete a specific music track (artist can only delete their own music)

**Path Parameters:**
- `musicId` (Long, required) - ID of the music to delete

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Music deleted successfully",
  "data": null
}
```

---

## Review Management Endpoints

### 6. Get Music Reviews
**Endpoint:** `GET /api/artist/music/{musicId}/reviews`

**Description:** Get all reviews for a specific music track

**Path Parameters:**
- `musicId` (Long, required) - ID of the music

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Reviews retrieved successfully",
  "data": [
    {
      "id": 1,
      "rating": 5,
      "comment": "Great song!",
      "customerUsername": "customer123",
      "createdAt": "2025-09-23T09:00:00"
    }
  ]
}
```

---

## Analytics Endpoints

### 7. Get Sales Analytics
**Endpoint:** `GET /api/artist/analytics/sales`

**Description:** Get comprehensive sales and performance analytics for the artist

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Sales analytics retrieved successfully",
  "data": {
    "totalTracks": 25,
    "totalSales": 150,
    "totalRevenue": 1299.50,
    "topTracks": [
      {
        "id": 123,
        "name": "Popular Song",
        "averageRating": 4.8,
        "totalReviews": 45
      }
    ],
    "genreDistribution": {
      "Pop": 10,
      "Rock": 8,
      "Jazz": 5,
      "Electronic": 2
    },
    "recentUploads": 3
  }
}
```

### 8. Get Review Analytics
**Endpoint:** `GET /api/artist/analytics/reviews`

**Description:** Get review-based analytics for the artist's music

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Review analytics retrieved successfully",
  "data": {
    "totalReviews": 120,
    "averageRating": 4.3,
    "ratingDistribution": {
      "5": 45,
      "4": 35,
      "3": 25,
      "2": 10,
      "1": 5
    },
    "recentReviews": 8,
    "topRatedTracks": [
      {
        "id": 123,
        "name": "Best Song",
        "averageRating": 4.9
      }
    ]
  }
}
```

---

## Profile & Dashboard Endpoints

### 9. Get Artist Profile
**Endpoint:** `GET /api/artist/profile`

**Description:** Get the artist's profile information

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "username": "artist_username",
    "authorities": ["ROLE_ARTIST"],
    "totalTracks": 25
  }
}
```

### 10. Get Dashboard Stats
**Endpoint:** `GET /api/artist/dashboard-stats`

**Description:** Get comprehensive dashboard statistics combining sales and review analytics

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Dashboard stats retrieved successfully",
  "data": {
    "totalTracks": 25,
    "salesAnalytics": {
      "totalTracks": 25,
      "totalSales": 150,
      "totalRevenue": 1299.50,
      "genreDistribution": {
        "Pop": 10,
        "Rock": 8
      },
      "recentUploads": 3
    },
    "reviewAnalytics": {
      "totalReviews": 120,
      "averageRating": 4.3,
      "recentReviews": 8
    }
  }
}
```

---

## Artist Dashboard Features

### Recommended Frontend Features

Based on the available endpoints, the artist dashboard should include:

#### 1. **Music Management Section**
- **Upload New Music**: Form with file upload for audio and cover image
- **My Music Library**: Paginated table/grid showing all uploaded tracks
- **Edit Music**: Modal/form for updating music details
- **Delete Music**: Confirmation dialog for music deletion

#### 2. **Analytics Dashboard**
- **Overview Cards**: Total tracks, sales, revenue, average rating
- **Charts**: 
  - Genre distribution pie chart
  - Revenue over time line chart
  - Rating distribution bar chart
- **Top Performing Tracks**: List of highest-rated/best-selling songs

#### 3. **Review Management**
- **Recent Reviews**: Latest customer feedback
- **Review Analytics**: Rating trends and distribution
- **Individual Track Reviews**: Detailed review viewing per song

#### 4. **Profile Management**
- **Artist Information**: Display and edit artist details
- **Account Settings**: Username, preferences
- **Upload Statistics**: Recent activity summary

### Expected User Flow

1. **Login** → Artist authenticates and receives JWT token
2. **Dashboard** → Load dashboard stats via `/dashboard-stats`
3. **Upload Music** → Use `/music/upload` with form validation
4. **Manage Library** → View music via `/music/my-music`
5. **View Analytics** → Access sales and review analytics
6. **Monitor Reviews** → Check individual music reviews
7. **Update Content** → Edit or delete music as needed

### File Upload Considerations

- **Supported Audio Formats**: Any format with `audio/*` MIME type
- **Supported Image Formats**: Any format with `image/*` MIME type
- **File Storage**: Files are stored in `/uploads/music/` and `/uploads/covers/`
- **Unique Naming**: Files are renamed with timestamp prefix to avoid conflicts
- **Validation**: Both client-side and server-side validation recommended

### Error Handling Best Practices

- Always check the `success` field in responses
- Display user-friendly error messages from the `message` field
- Implement proper loading states during file uploads
- Handle network errors gracefully
- Validate file types before upload on frontend

---

This documentation provides all the necessary information for frontend developers to implement a complete artist dashboard with full CRUD operations for music management, comprehensive analytics, and review monitoring capabilities.
