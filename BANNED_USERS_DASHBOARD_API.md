# Banned Users Dashboard API - Documentation

## Overview
The admin dashboard now has comprehensive endpoints to view, manage, and monitor all banned users (both customers and artists) with detailed statistics.

## New Endpoints

### 1. Get All Banned Users (Paginated)
**GET** `/api/admin/banned-users`

Retrieve a paginated list of all currently banned users.

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Items per page

**Response:**
```json
{
  "content": [
    {
      "userId": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "userType": "CUSTOMER",
      "banReason": "Violation of community guidelines",
      "bannedAt": "2025-10-19T10:30:00",
      "bannedUntil": "2025-10-20T10:30:00",
      "bannedBy": "admin_user",
      "hoursRemaining": 23
    },
    {
      "userId": 5,
      "username": "artist_smith",
      "email": "smith@example.com",
      "firstName": "Jane",
      "lastName": "Smith",
      "userType": "ARTIST",
      "banReason": "Copyright infringement",
      "bannedAt": "2025-10-18T15:00:00",
      "bannedUntil": "2025-10-25T15:00:00",
      "bannedBy": "admin_user",
      "hoursRemaining": 143
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 15,
  "totalPages": 2,
  "last": false
}
```

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/admin/banned-users?page=0&size=10" \
  -H "Authorization: Bearer <admin_token>"
```

---

### 2. Get All Banned Users (No Pagination)
**GET** `/api/admin/banned-users/all`

Retrieve the complete list of all banned users without pagination.

**Response:**
```json
{
  "bannedUsers": [
    {
      "userId": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "userType": "CUSTOMER",
      "banReason": "Violation of community guidelines",
      "bannedAt": "2025-10-19T10:30:00",
      "bannedUntil": "2025-10-20T10:30:00",
      "bannedBy": "admin_user",
      "hoursRemaining": 23
    }
  ],
  "totalCount": 15,
  "timestamp": "2025-10-19T19:30:00"
}
```

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/admin/banned-users/all" \
  -H "Authorization: Bearer <admin_token>"
```

---

### 3. Get Ban Statistics
**GET** `/api/admin/banned-users/statistics`

Get statistical overview of all banned users.

**Response:**
```json
{
  "totalBanned": 15,
  "bannedCustomers": 10,
  "bannedArtists": 5,
  "expiringSoon": 3,
  "timestamp": "2025-10-19T19:30:00"
}
```

**Fields:**
- `totalBanned` - Total number of currently banned users
- `bannedCustomers` - Number of banned customers
- `bannedArtists` - Number of banned artists
- `expiringSoon` - Number of bans expiring within 24 hours
- `timestamp` - When the statistics were calculated

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/admin/banned-users/statistics" \
  -H "Authorization: Bearer <admin_token>"
```

---

## BannedUserDto Structure

Each banned user object contains:

| Field | Type | Description |
|-------|------|-------------|
| `userId` | Long | User ID |
| `username` | String | Username |
| `email` | String | Email address |
| `firstName` | String | First name |
| `lastName` | String | Last name |
| `userType` | String | "CUSTOMER" or "ARTIST" |
| `banReason` | String | Reason for the ban |
| `bannedAt` | LocalDateTime | When the ban was issued |
| `bannedUntil` | LocalDateTime | When the ban expires |
| `bannedBy` | String | Admin who issued the ban |
| `hoursRemaining` | Long | Hours until ban expires |

---

## Integration with Admin Dashboard

### Example: Display Banned Users Table

```typescript
interface BannedUser {
  userId: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  userType: string;
  banReason: string;
  bannedAt: string;
  bannedUntil: string;
  bannedBy: string;
  hoursRemaining: number;
}

async function fetchBannedUsers(page: number = 0, size: number = 10) {
  const response = await fetch(
    `http://localhost:8080/api/admin/banned-users?page=${page}&size=${size}`,
    {
      headers: {
        'Authorization': `Bearer ${adminToken}`,
      }
    }
  );
  
  return await response.json();
}

## Performance Notes

- The `/banned-users` endpoint is paginated for better performance with large datasets
- The `/banned-users/all` endpoint should be used carefully with large numbers of banned users
- Statistics are calculated in real-time - consider caching for high-traffic scenarios
- The `hoursRemaining` field is calculated dynamically for each request

---

## Testing

### Test Get All Banned Users
```bash
# Paginated
curl -X GET "http://localhost:8080/api/admin/banned-users?page=0&size=5" \
  -H "Authorization: Bearer <token>"

# All at once
curl -X GET "http://localhost:8080/api/admin/banned-users/all" \
  -H "Authorization: Bearer <token>"

# Statistics
curl -X GET "http://localhost:8080/api/admin/banned-users/statistics" \
  -H "Authorization: Bearer <token>"
```

---

## Error Handling

All endpoints return standard error responses:

```json
{
  "message": "Error message here",
  "timestamp": "2025-10-19T19:30:00"
}
```

Common error codes:
- **401 Unauthorized** - Invalid or missing authentication token
- **403 Forbidden** - User doesn't have admin privileges
- **500 Internal Server Error** - Server-side error

---

## Future Enhancements

Potential improvements:
1. **Filtering** - Filter by user type, ban reason, expiration date
2. **Sorting** - Sort by username, ban date, expiration date
3. **Search** - Search banned users by username or email
4. **Export** - Export banned users list to CSV/PDF
5. **Bulk Actions** - Unban multiple users at once
6. **Ban History** - View past bans for a user

