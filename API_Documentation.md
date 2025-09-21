# Music Store API Documentation

## Base URL
```
http://localhost:8082
```

## Overview
This API provides endpoints for a comprehensive music store application with support for user authentication, music management, cart operations, order processing, reviews, and administrative functions. The API features an enhanced ticket support system with chat-like messaging capabilities.

## Recent Updates (September 2025)
- **Revolutionary Chat-Based Ticket System**: Complete overhaul of the support ticket system
  - Tickets now support multiple messages in a conversation format
  - Real-time chat between customers and staff members
  - Staff assignment and escalation capabilities
  - Comprehensive ticket status management
- **Simplified Architecture**: Removed pagination complexity for university project requirements
- **Enhanced Performance**: Optimized queries and removed unnecessary complexity
- **Better User Experience**: Chat-like interface for support interactions
- **Complete Admin Management**: Full administrative control over tickets, users, music, and orders

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

**Response (Success - 200 OK):**
```json
{
  "token": "jwt_token_string",
  "type": "Bearer",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "CUSTOMER"
  }
}
```

---

## Admin Management System

### User Management

#### 1. Create User
**Endpoint:** `POST /api/admin/users/create`

**Access Level:** ADMIN

**Description:** Create a new user with specified role

**Request Body:**
```json
{
  "username": "string (required)",
  "password": "string (required)",
  "email": "string (required)",
  "role": "string (required: CUSTOMER, ARTIST, STAFF, ADMIN)",
  "firstName": "string (optional)",
  "lastName": "string (optional)",
  "artistName": "string (optional, for ARTIST role)",
  "cover": "string (optional, cover image URL)"
}
```

#### 2. Get All Users
**Endpoint:** `GET /api/admin/users`

**Access Level:** ADMIN

**Query Parameters:**
- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `role` (String, optional): Filter by user role

#### 3. Update User
**Endpoint:** `PUT /api/admin/users/{userId}`

**Access Level:** ADMIN

#### 4. Delete User
**Endpoint:** `DELETE /api/admin/users/{userId}`

**Access Level:** ADMIN

#### 5. Update User Status
**Endpoint:** `PUT /api/admin/users/{userId}/status`

**Access Level:** ADMIN

**Request Body:**
```json
{
  "enabled": "boolean (required)"
}
```

### Music Management

#### 1. Get All Music (Admin)
**Endpoint:** `GET /api/admin/music`

**Access Level:** ADMIN

#### 2. Delete Music
**Endpoint:** `DELETE /api/admin/music/{musicId}`

**Access Level:** ADMIN

#### 3. Update Music Status
**Endpoint:** `PUT /api/admin/music/{musicId}/status`

**Access Level:** ADMIN

**Request Body:**
```json
{
  "status": "string (required)"
}
```

### Order Management

#### 1. Get All Orders (Admin)
**Endpoint:** `GET /api/admin/orders`

**Access Level:** ADMIN

**Query Parameters:**
- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `status` (String, optional): Filter by order status

#### 2. Refund Order
**Endpoint:** `PUT /api/admin/orders/{orderId}/refund`

**Access Level:** ADMIN

### Analytics and Reports

#### 1. System Overview
**Endpoint:** `GET /api/admin/analytics/overview`

**Access Level:** ADMIN

**Description:** Get comprehensive system statistics

**Response (Success - 200 OK):**
```json
{
  "totalUsers": 1250,
  "totalMusic": 3420,
  "totalOrders": 5670,
  "totalRevenue": 45230.50,
  "activeTickets": 23
}
```

#### 2. Detailed Analytics
**Endpoint:** `GET /api/admin/analytics/detailed`

**Access Level:** ADMIN

**Description:** Get detailed analytics including ticket statistics

**Response (Success - 200 OK):**
```json
{
  "userGrowth": "User growth analytics data",
  "salesAnalytics": "Sales analytics data",
  "musicAnalytics": "Music analytics data",
  "ticketAnalytics": [
    ["OPEN", 15],
    ["IN_PROGRESS", 8],
    ["CLOSED", 120],
    ["URGENT", 3]
  ]
}
```

#### 3. Generate Comprehensive Report
**Endpoint:** `GET /api/admin/reports/comprehensive`

**Access Level:** ADMIN

**Query Parameters:**
- `startDate` (LocalDate, optional): Start date for report
- `endDate` (LocalDate, optional): End date for report
- `format` (String, default: "pdf"): Report format

### System Settings

#### 1. Create System Backup
**Endpoint:** `POST /api/admin/settings/backup`

**Access Level:** ADMIN

**Description:** Initiate system backup

---

## Admin Ticket Management

### 1. Get All Tickets (Admin)
**Endpoint:** `GET /api/admin/tickets`

**Access Level:** ADMIN

**Description:** Retrieve all support tickets with optional status filtering

**Query Parameters:**
- `status` (String, optional): Filter by ticket status (OPEN, IN_PROGRESS, URGENT, CLOSED)

**Response (Success - 200 OK):**
```json
[
  {
    "id": 1,
    "subject": "Payment Issue",
    "status": "OPEN",
    "customer": {
      "id": 1,
      "username": "john_doe"
    },
    "assignedStaff": null,
    "createdAt": "2025-09-21T10:30:00",
    "closedAt": null,
    "messages": [...]
  }
]
```

### 2. Get Ticket Details (Admin)
**Endpoint:** `GET /api/admin/tickets/{ticketId}`

**Access Level:** ADMIN

**Description:** Get detailed information about a specific ticket

### 3. Get Ticket Messages (Admin)
**Endpoint:** `GET /api/admin/tickets/{ticketId}/messages`

**Access Level:** ADMIN

**Description:** Get all messages in a ticket conversation (admin view)

### 4. Reply to Ticket (Admin)
**Endpoint:** `POST /api/admin/tickets/{ticketId}/reply`

**Access Level:** ADMIN

**Description:** Add an admin reply to a ticket conversation

**Request Body:**
```json
{
  "message": "string (required, admin reply content)"
}
```

**Response (Success - 200 OK):**
```json
{
  "id": 3,
  "content": "We've escalated your issue to our technical team.",
  "timestamp": "2025-09-21T11:00:00",
  "isFromStaff": true,
  "customer": null,
  "staff": {
    "id": 1,
    "username": "admin_user"
  }
}
```

### 5. Update Ticket Status (Admin)
**Endpoint:** `PUT /api/admin/tickets/{ticketId}/status`

**Access Level:** ADMIN

**Description:** Update the status of any ticket

**Request Body:**
```json
{
  "status": "string (required: OPEN, IN_PROGRESS, URGENT, CLOSED)"
}
```

### 6. Assign Ticket (Admin)
**Endpoint:** `POST /api/admin/tickets/{ticketId}/assign`

**Access Level:** ADMIN

**Description:** Assign a ticket to the authenticated admin

### 7. Close Ticket (Admin)
**Endpoint:** `POST /api/admin/tickets/{ticketId}/close`

**Access Level:** ADMIN

**Description:** Mark any ticket as closed

### 8. Reopen Ticket (Admin)
**Endpoint:** `POST /api/admin/tickets/{ticketId}/reopen`

**Access Level:** ADMIN

**Description:** Reopen any closed ticket

### 9. Delete Ticket (Admin)
**Endpoint:** `DELETE /api/admin/tickets/{ticketId}`

**Access Level:** ADMIN

**Description:** Permanently delete a ticket (admin only)

### 10. Search Tickets (Admin)
**Endpoint:** `GET /api/admin/tickets/search`

**Access Level:** ADMIN

**Description:** Search all tickets by content in subject or messages

**Query Parameters:**
- `query` (String, required): Search term

### 11. Get Urgent Tickets (Admin)
**Endpoint:** `GET /api/admin/tickets/urgent`

**Access Level:** ADMIN

**Description:** Get all tickets marked as urgent

### 12. Get Unassigned Tickets (Admin)
**Endpoint:** `GET /api/admin/tickets/unassigned`

**Access Level:** ADMIN

**Description:** Get all tickets that haven't been assigned to any staff member

### 13. Get Ticket Statistics (Admin)
**Endpoint:** `GET /api/admin/tickets/stats`

**Access Level:** ADMIN

**Description:** Get comprehensive ticket statistics and status distribution

**Response (Success - 200 OK):**
```json
[
  ["OPEN", 15],
  ["IN_PROGRESS", 8],
  ["CLOSED", 120],
  ["URGENT", 3]
]
```

---

## Customer Support Ticket System (NEW CHAT-BASED)

### API Flow Overview
The new chat-based ticket system follows this optimized flow:

1. **Create Ticket**: `POST /api/customer/support/ticket` - Returns clean ticket info without messages (faster response)
2. **Get Conversation**: `GET /api/customer/support/ticket/{ticketId}/messages` - Returns full chat conversation
3. **Add Message**: `POST /api/customer/support/ticket/{ticketId}/message` - Adds new message to conversation

This separation provides better performance and cleaner API design.

### 1. Create Support Ticket
**Endpoint:** `POST /api/customer/support/ticket`

**Access Level:** CUSTOMER

**Description:** Create a new support ticket with initial message. **Note: Returns ticket info without messages for better performance. Use the messages endpoint to get the full conversation.**

**Request Body:**
```json
{
  "subject": "string (required, max 255 characters)",
  "description": "string (required, initial message)"
}
```

**Response (Success - 200 OK):**
```json
{
  "id": 1,
  "subject": "Payment Issue",
  "status": "OPEN",
  "customer": {
    "id": 1,
    "username": "john_doe",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com"
  },
  "assignedStaff": null,
  "createdAt": "2025-09-21T10:30:00",
  "closedAt": null
}
```

**Note:** The response does NOT include messages to avoid lazy loading issues and improve performance. Use the dedicated messages endpoint to get the conversation.

### 2. Get Customer's Tickets
**Endpoint:** `GET /api/customer/support/tickets`

**Access Level:** CUSTOMER

**Description:** Retrieve all tickets created by the authenticated customer (without messages for performance)

**Response (Success - 200 OK):**
```json
[
  {
    "id": 1,
    "subject": "Payment Issue",
    "status": "IN_PROGRESS",
    "customer": {
      "id": 1,
      "username": "john_doe",
      "firstName": "John",
      "lastName": "Doe"
    },
    "assignedStaff": {
      "id": 1,
      "username": "support_agent"
    },
    "createdAt": "2025-09-21T10:30:00",
    "closedAt": null
  }
]
```

### 3. Add Message to Ticket (Customer Reply)
**Endpoint:** `POST /api/customer/support/ticket/{ticketId}/message`

**Access Level:** CUSTOMER

**Description:** Add a new message to an existing ticket conversation

**Path Parameters:**
- `ticketId` (Long): ID of the ticket

**Request Body:**
```json
{
  "content": "string (required, message content)"
}
```

**Response (Success - 200 OK):**
```json
{
  "id": 5,
  "content": "Thank you for your help!",
  "timestamp": "2025-09-21T11:45:00",
  "isFromStaff": false,
  "customer": {
    "id": 1,
    "username": "john_doe"
  },
  "staff": null
}
```

### 4. Get Ticket Conversation
**Endpoint:** `GET /api/customer/support/ticket/{ticketId}/messages`

**Access Level:** CUSTOMER

**Description:** Retrieve all messages for a specific ticket (complete chat conversation)

**Path Parameters:**
- `ticketId` (Long): ID of the ticket

**Security:** Customers can only access messages for their own tickets.

**Response (Success - 200 OK):**
```json
[
  {
    "id": 1,
    "content": "I'm having trouble with my payment",
    "timestamp": "2025-09-21T10:30:00",
    "isFromStaff": false,
    "customer": {
      "id": 1,
      "username": "john_doe"
    },
    "staff": null
  },
  {
    "id": 2,
    "content": "I can help you with that. What payment method are you using?",
    "timestamp": "2025-09-21T10:35:00",
    "isFromStaff": true,
    "customer": null,
    "staff": {
      "id": 1,
      "username": "support_agent"
    }
  },
  {
    "id": 3,
    "content": "I'm using PayPal but the payment keeps failing",
    "timestamp": "2025-09-21T10:40:00",
    "isFromStaff": false,
    "customer": {
      "id": 1,
      "username": "john_doe"
    },
    "staff": null
  }
]
```

---

## Staff Support Management

### 1. Get All Tickets (Staff)
**Endpoint:** `GET /api/staff/tickets`

**Access Level:** STAFF

**Description:** Retrieve all support tickets with optional status filtering

**Query Parameters:**
- `status` (String, optional): Filter by ticket status

### 2. Get Urgent Tickets (Staff)
**Endpoint:** `GET /api/staff/tickets/urgent`

**Access Level:** STAFF

### 3. Get Tickets Needing Attention
**Endpoint:** `GET /api/staff/tickets/needs-attention`

**Access Level:** STAFF

### 4. Get Unassigned Tickets (Staff)
**Endpoint:** `GET /api/staff/tickets/unassigned`

**Access Level:** STAFF

### 5. Reply to Ticket (Staff)
**Endpoint:** `POST /api/staff/tickets/{ticketId}/reply`

**Access Level:** STAFF

**Request Body:**
```json
{
  "message": "string (required, staff reply content)"
}
```

### 6. Assign Ticket (Staff)
**Endpoint:** `POST /api/staff/tickets/{ticketId}/assign`

**Access Level:** STAFF

### 7. Update Ticket Status (Staff)
**Endpoint:** `PUT /api/staff/tickets/{ticketId}/status`

**Access Level:** STAFF

### 8. Close Ticket (Staff)
**Endpoint:** `POST /api/staff/tickets/{ticketId}/close`

**Access Level:** STAFF

### 9. Search Tickets (Staff)
**Endpoint:** `GET /api/staff/tickets/search`

**Access Level:** STAFF

**Query Parameters:**
- `query` (String, required): Search term

---

## Ticket Status Values

The ticket system supports the following status values:

- **OPEN**: New ticket, awaiting staff assignment
- **IN_PROGRESS**: Ticket assigned to staff and being worked on
- **URGENT**: High priority ticket requiring immediate attention
- **CLOSED**: Ticket resolved and closed

## Ticket Message Structure

Each ticket contains multiple messages forming a conversation:

```json
{
  "id": "Long (message ID)",
  "content": "String (message content)",
  "timestamp": "LocalDateTime (when message was sent)",
  "isFromStaff": "Boolean (true if from staff, false if from customer)",
  "customer": "Customer object (if message from customer)",
  "staff": "Staff object (if message from staff)"
}
```

## Key Features

### 1. Multi-Level Management
- **Customer**: Create tickets, add messages, view own tickets
- **Staff**: Manage assigned tickets, reply to customers, update status
- **Admin**: Full control over all tickets, delete tickets, comprehensive analytics

### 2. Chat-Like Experience
- Multiple messages per ticket creating conversation flow
- Clear distinction between customer and staff messages
- Chronological message ordering for easy conversation following

### 3. Comprehensive Analytics
- System overview with key metrics
- Ticket status distribution
- User, music, and order analytics
- Detailed reporting capabilities

### 4. Simplified Architecture
- No complex pagination for university project requirements
- Straightforward REST endpoints
- Clear separation of customer, staff, and admin functionalities

---

## Error Handling

All endpoints return consistent error responses:

**Error Response (400 Bad Request):**
```json
{
  "error": "Detailed error message describing what went wrong"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "error": "Unauthorized access"
}
```

**Error Response (403 Forbidden):**
```json
{
  "error": "Access denied"
}
```

---

## Implementation Notes

### Database Schema Changes
The new system requires these tables:
- `tickets` - Main ticket information with staff assignment
- `ticket_messages` - Individual messages within tickets

### Migration Considerations
- Existing single message/reply tickets need migration to new message format
- Staff assignment functionality requires proper staff user management
- All pagination-based queries replaced with simple list operations

### Performance Considerations
- Messages loaded lazily to improve ticket list performance
- Search functionality works across both subjects and message content
- Simple list operations replace complex paginated queries for better maintainability

This comprehensive ticket system provides a modern, chat-like support experience while maintaining the structure needed for effective customer service management across multiple user roles.
