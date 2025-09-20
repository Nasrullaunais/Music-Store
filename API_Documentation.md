# Music Store API Documentation

## Base URL
```
http://localhost:8082
```

## Overview
This API provides endpoints for a music store application with support for user authentication, music management, cart operations, and audio streaming. The API is designed to work seamlessly with React frontends for music playback and e-commerce functionality.

## Authentication Endpoints

### 1. User Registration
**Endpoint:** `POST /api/auth/register`

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

**Error Response (400):**
```json
{
  "message": "Registration failed: Username already exists"
}
```

### 2. User Login
**Endpoint:** `POST /api/auth/login`

**Description:** Authenticate user and receive JWT token for protected endpoints

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
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "CUSTOMER",
    "firstName": "John",
    "lastName": "Doe"
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
Authorization: Bearer {JWT_TOKEN}
```

**Success Response (200):**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "CUSTOMER",
  "firstName": "John",
  "lastName": "Doe"
}
```

---

## Music Endpoints

### 1. Get All Music (Paginated)
**Endpoint:** `GET /api/music`

**Description:** Retrieve paginated list of all music tracks with filtering and sorting options

**Query Parameters:**
- `page` (optional, default: 0) - Page number (0-based)
- `size` (optional, default: 50) - Number of items per page
- `sortBy` (optional, default: "createdAt") - Field to sort by
- `sortDir` (optional, default: "desc") - Sort direction ("asc" or "desc")
- `genre` (optional) - Filter by genre
- `artist` (optional) - Filter by artist username
- `search` (optional) - Search in name, artist, or genre

**Example Request:**
```
GET /api/music?page=0&size=10&sortBy=name&sortDir=asc&genre=Islamic
```

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "A Thousand Greetings",
      "description": "Motivational nasheed that uplifts the soul and connects with divine spirituality",
      "price": 9.99,
      "imageUrl": "https://example.com/images/thousand-greetings.jpg",
      "audioFilePath": "/uploads/music/A THOUSAND GREETINGS - MOTIVATIONAL NASHEED - MUHAMMAD AL MUQIT .mp3",
      "category": "Nasheed",
      "artistUsername": "muhammad_al_muqit",
      "albumName": "Spiritual Collection",
      "genre": "Islamic",
      "releaseYear": 2023,
      "createdAt": "2024-01-15T10:30:00"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 8,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 8
}
```

### 2. Get Music by ID
**Endpoint:** `GET /api/music/{id}`

**Description:** Retrieve a specific music track by its ID

**Path Parameters:**
- `id` (required) - Music track ID

**Success Response (200):**
```json
{
  "id": 1,
  "name": "A Thousand Greetings",
  "description": "Motivational nasheed that uplifts the soul and connects with divine spirituality",
  "price": 9.99,
  "imageUrl": "https://example.com/images/thousand-greetings.jpg",
  "audioFilePath": "/uploads/music/A THOUSAND GREETINGS - MOTIVATIONAL NASHEED - MUHAMMAD AL MUQIT .mp3",
  "category": "Nasheed",
  "artistUsername": "muhammad_al_muqit",
  "albumName": "Spiritual Collection",
  "genre": "Islamic",
  "releaseYear": 2023,
  "createdAt": "2024-01-15T10:30:00"
}
```

**Error Response (404):**
```json
{
  "message": "Music not found with id: 1"
}
```

### 3. Get Featured Music
**Endpoint:** `GET /api/music/featured`

**Description:** Get the first 8 music tracks to display as featured content

**Success Response (200):**
```json
[
  {
    "id": 1,
    "name": "A Thousand Greetings",
    "description": "Motivational nasheed that uplifts the soul and connects with divine spirituality",
    "price": 9.99,
    "imageUrl": "https://example.com/images/thousand-greetings.jpg",
    "audioFilePath": "/uploads/music/A THOUSAND GREETINGS - MOTIVATIONAL NASHEED - MUHAMMAD AL MUQIT .mp3",
    "category": "Nasheed",
    "artistUsername": "muhammad_al_muqit",
    "albumName": "Spiritual Collection",
    "genre": "Islamic",
    "releaseYear": 2023,
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

### 4. Get Available Genres
**Endpoint:** `GET /api/music/genres`

**Description:** Get list of all available music genres

**Success Response (200):**
```json
[
  "Islamic",
  "Pop",
  "Rock",
  "Jazz",
  "Classical",
  "Electronic",
  "Hip-Hop",
  "Country",
  "Blues"
]
```

### 5. Get Available Artists
**Endpoint:** `GET /api/music/artists`

**Description:** Get list of all available artists

**Success Response (200):**
```json
[
  "muhammad_al_muqit",
  "traditional_artist",
  "grateful_voice",
  "reflective_soul",
  "yearning_heart"
]
```

### 6. Get Music by Artist
**Endpoint:** `GET /api/music/by-artist/{artistUsername}`

**Description:** Get paginated music tracks by a specific artist

**Path Parameters:**
- `artistUsername` (required) - Artist's username

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 12) - Number of items per page

**Success Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "A Thousand Greetings",
      "description": "Motivational nasheed that uplifts the soul and connects with divine spirituality",
      "price": 9.99,
      "imageUrl": "https://example.com/images/thousand-greetings.jpg",
      "audioFilePath": "/uploads/music/A THOUSAND GREETINGS - MOTIVATIONAL NASHEED - MUHAMMAD AL MUQIT .mp3",
      "category": "Nasheed",
      "artistUsername": "muhammad_al_muqit",
      "albumName": "Spiritual Collection",
      "genre": "Islamic",
      "releaseYear": 2023,
      "createdAt": "2024-01-15T10:30:00"
    }
  ],
  "totalElements": 3,
  "totalPages": 1
}
```

### 7. Get Artist Music Count
**Endpoint:** `GET /api/music/artists/{artistUsername}/count`

**Description:** Get the total number of music tracks by a specific artist

**Path Parameters:**
- `artistUsername` (required) - Artist's username

**Success Response (200):**
```json
{
  "count": 3,
  "artist": "muhammad_al_muqit"
}
```

### 8. Upload Music
**Endpoint:** `POST /api/music/upload`

**Description:** Upload a new music file with metadata (Form data)

**Request Headers:**
```
Content-Type: multipart/form-data
Authorization: Bearer {JWT_TOKEN} (if authentication required)
```

**Form Data Parameters:**
- `file` (required) - Audio file (MP3, WAV, etc.)
- `name` (required) - Music track name
- `description` (required) - Track description
- `price` (required) - Price as decimal string
- `genre` (required) - Music genre
- `artist` (required) - Artist username
- `albumName` (optional) - Album name
- `releaseYear` (required) - Release year as string

**Success Response (200):**
```json
{
  "id": 9,
  "name": "New Track",
  "description": "Amazing new track",
  "price": 15.99,
  "audioFilePath": "/uploads/music/uuid_filename.mp3",
  "category": "Pop",
  "artistUsername": "new_artist",
  "albumName": "New Album",
  "genre": "Pop",
  "releaseYear": 2024,
  "createdAt": "2024-01-20T14:30:00"
}
```

---

## Audio Streaming

### 1. Stream Music File
**Endpoint:** `GET /uploads/music/{filename}`

**Description:** Stream audio file for playback in React applications

**Path Parameters:**
- `filename` (required) - Audio file name from audioFilePath

**Response:** Audio stream (MP3/WAV format)

**Usage in React:**
```javascript
const audioUrl = `http://localhost:8082/uploads/music/A%20THOUSAND%20GREETINGS%20-%20MOTIVATIONAL%20NASHEED%20-%20MUHAMMAD%20AL%20MUQIT%20.mp3`;
const audio = new Audio(audioUrl);
audio.play();
```

---

## Sample Music Data

The application comes pre-loaded with 8 nasheed tracks:

1. **A Thousand Greetings** - Muhammad Al Muqit ($9.99)
2. **I Rise** - Muhammad Al Muqit ($8.99)
3. **Qad Kafani Ilmu Rabbi** - Traditional Artist ($10.99)
4. **Shukran Laka Rabbi** - Grateful Voice ($7.99)
5. **Sins Nasheed** - Reflective Soul ($9.49)
6. **Tabsirah (Slowed Reverb)** - Muhammad Al Muqit ($11.99)
7. **Taweel Al Shawq** - Yearning Heart ($8.49)
8. **The Book of Allah is My Constitution** - Muhammad Al Muqit ($12.99)

All tracks are in the "Islamic" genre and "Nasheed" category with complete metadata including descriptions, album names, and release years.

---

## Error Handling

### Common Error Responses

**400 Bad Request:**
```json
{
  "message": "Invalid request parameters",
  "details": "Specific validation error details"
}
```

**401 Unauthorized:**
```json
{
  "message": "Authentication required"
}
```

**403 Forbidden:**
```json
{
  "message": "Access denied"
}
```

**404 Not Found:**
```json
{
  "message": "Resource not found"
}
```

**500 Internal Server Error:**
```json
{
  "message": "Internal server error",
  "details": "Error processing request"
}
```

---

## CORS Configuration

The API is configured to accept requests from:
- `http://localhost:5173` (Default Vite React development server)

---

## React Frontend Integration

### Basic Usage Example

```javascript
// Fetch all music tracks
const fetchMusic = async () => {
  try {
    const response = await fetch('http://localhost:8082/api/music?page=0&size=20');
    const data = await response.json();
    return data.content; // Array of music tracks
  } catch (error) {
    console.error('Error fetching music:', error);
  }
};

// Play a music track
const playTrack = (audioFilePath) => {
  const audio = new Audio(`http://localhost:8082${audioFilePath}`);
  audio.play();
};

// Get featured music for homepage
const fetchFeaturedMusic = async () => {
  try {
    const response = await fetch('http://localhost:8082/api/music/featured');
    const featuredTracks = await response.json();
    return featuredTracks;
  } catch (error) {
    console.error('Error fetching featured music:', error);
  }
};
```

### Audio Player Component Example

```jsx
import React, { useState, useRef } from 'react';

const AudioPlayer = ({ track }) => {
  const [isPlaying, setIsPlaying] = useState(false);
  const audioRef = useRef(null);

  const togglePlayPause = () => {
    if (isPlaying) {
      audioRef.current.pause();
    } else {
      audioRef.current.play();
    }
    setIsPlaying(!isPlaying);
  };

  return (
    <div className="audio-player">
      <audio
        ref={audioRef}
        src={`http://localhost:8082${track.audioFilePath}`}
        onEnded={() => setIsPlaying(false)}
      />
      <button onClick={togglePlayPause}>
        {isPlaying ? 'Pause' : 'Play'}
      </button>
      <div className="track-info">
        <h3>{track.name}</h3>
        <p>{track.artistUsername}</p>
        <p>${track.price}</p>
      </div>
    </div>
  );
};
```

This API documentation provides everything you need to integrate music playback functionality into your React frontend application.
