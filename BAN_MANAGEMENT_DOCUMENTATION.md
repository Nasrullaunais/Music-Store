# User Ban Management System - Documentation

## Overview
The Music Store application now supports temporary user bans for both Customers and Artists. Admins can ban users for a specified duration with a reason, and the system automatically sends email notifications to banned users.

## Features

### 1. Temporary User Bans
- Admins can temporarily ban customers and artists
- Ban duration specified in hours
- Mandatory ban reason required
- Users are automatically disabled during ban period
- Email notifications sent to banned users

### 2. Ban Information Tracking
- Ban reason
- Ban start time (when banned)
- Ban end time (when ban expires)
- Admin who issued the ban

### 3. Automatic Ban Expiration
- Bans automatically expire after the specified duration
- System can check and auto-unban expired bans
- Users are re-enabled when bans expire
- Email notifications sent when bans are lifted

### 4. Manual Unban
- Admins can manually lift bans before expiration
- Users receive email notification when unbanned

## API Endpoints

### Ban a Customer
**POST** `/api/admin/customers/{customerId}/ban`

**Request Body:**
```json
{
  "reason": "Violation of community guidelines",
  "durationInHours": 24
}
```

**Response:**
```json
{
  "userId": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "isBanned": true,
  "banReason": "Violation of community guidelines",
  "bannedAt": "2025-10-19T10:30:00",
  "bannedUntil": "2025-10-20T10:30:00",
  "bannedBy": "admin_user",
  "message": "Customer banned successfully"
}
```

### Ban an Artist
**POST** `/api/admin/artists/{artistId}/ban`

**Request Body:**
```json
{
  "reason": "Copyright infringement",
  "durationInHours": 168
}
```

**Response:** Same as customer ban response

### Unban a Customer
**DELETE** `/api/admin/customers/{customerId}/ban`

**Response:**
```json
{
  "userId": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "isBanned": false,
  "banReason": null,
  "bannedAt": null,
  "bannedUntil": null,
  "bannedBy": null,
  "message": "Customer unbanned successfully"
}
```

### Unban an Artist
**DELETE** `/api/admin/artists/{artistId}/ban`

**Response:** Same as customer unban response

### Get Customer Ban Status
**GET** `/api/admin/customers/{customerId}/ban-status`

**Response:**
```json
{
  "userId": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "isBanned": true,
  "banReason": "Violation of community guidelines",
  "bannedAt": "2025-10-19T10:30:00",
  "bannedUntil": "2025-10-20T10:30:00",
  "bannedBy": "admin_user",
  "message": "Customer is currently banned"
}
```

### Get Artist Ban Status
**GET** `/api/admin/artists/{artistId}/ban-status`

**Response:** Same as customer ban status response

## Email Notifications

### Ban Notification Email
When a user is banned, they receive an email with:
- Notification that their account has been banned
- Reason for the ban
- Duration/expiration date of the ban
- Information about account restrictions
- Contact information for support

**Example Email:**
```
Subject: Account Temporarily Banned - Music Store

Dear john_doe,

We regret to inform you that your account has been temporarily banned.

Reason: Violation of community guidelines

Ban Duration: Until 2025-10-20 10:30:00

During this period, you will not be able to access your account or use our services.

If you believe this action was taken in error or have any questions, 
please contact our support team.

Best regards,
Music Store Administration Team
```

### Unban Notification Email
When a ban is lifted (manually or automatically), users receive an email with:
- Notification that the ban has been lifted
- Confirmation that they can access their account again
- Encouragement for better behavior

**Example Email:**
```
Subject: Account Ban Lifted - Music Store

Dear john_doe,

Good news! Your account ban has been lifted.

You can now access your account and use all our services again.

We hope to provide you with a better experience moving forward.

If you have any questions, please don't hesitate to contact our support team.

Best regards,
Music Store Administration Team
```

## Database Schema

### Customers Table
```sql
ALTER TABLE customers
ADD COLUMN is_banned BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN ban_reason VARCHAR(500),
ADD COLUMN banned_until TIMESTAMP,
ADD COLUMN banned_by VARCHAR(100),
ADD COLUMN banned_at TIMESTAMP;
```

### Artists Table
```sql
ALTER TABLE artists
ADD COLUMN is_banned BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN ban_reason VARCHAR(500),
ADD COLUMN banned_until TIMESTAMP,
ADD COLUMN banned_by VARCHAR(100),
ADD COLUMN banned_at TIMESTAMP;
```

## Business Rules

1. **Ban Duration**: Can be any positive integer representing hours (e.g., 24 for 1 day, 168 for 1 week)
2. **Ban Reason**: Required field - admins must provide a reason for the ban
3. **Account Status**: When banned, the user's `enabled` flag is set to `false`
4. **Double Ban Prevention**: Cannot ban a user who is already banned
5. **Unban Restriction**: Cannot unban a user who is not currently banned
6. **Automatic Re-enable**: When a ban expires or is lifted, the user's `enabled` flag is set to `true`

## Usage Examples

### Example 1: Ban a user for 24 hours
```bash
POST /api/admin/customers/1/ban
{
  "reason": "Spam in reviews",
  "durationInHours": 24
}
```

### Example 2: Ban a user for 1 week
```bash
POST /api/admin/artists/5/ban
{
  "reason": "Multiple policy violations",
  "durationInHours": 168
}
```

### Example 3: Check ban status
```bash
GET /api/admin/customers/1/ban-status
```

### Example 4: Manually unban a user
```bash
DELETE /api/admin/customers/1/ban
```

## Security & Audit

All ban-related actions are logged in the audit log system:
- **BAN_CUSTOMER**: When a customer is banned
- **BAN_ARTIST**: When an artist is banned
- **UNBAN_CUSTOMER**: When a customer is unbanned
- **UNBAN_ARTIST**: When an artist is unbanned
- **VIEW_CUSTOMER_BAN_STATUS**: When ban status is checked
- **VIEW_ARTIST_BAN_STATUS**: When ban status is checked

Each log entry includes:
- Admin username who performed the action
- Timestamp of the action
- User ID affected
- Detailed description (including ban duration and reason)

## Error Handling

### Common Errors:

1. **User Already Banned**
   - Status: 400 Bad Request
   - Message: "Customer is already banned until [date]"

2. **User Not Banned**
   - Status: 400 Bad Request
   - Message: "Customer is not currently banned"

3. **User Not Found**
   - Status: 404 Not Found
   - Message: "Customer not found with id: [id]"

4. **Missing Ban Reason**
   - Status: 400 Bad Request
   - Message: "Ban reason is required"

5. **Invalid Duration**
   - Status: 400 Bad Request
   - Message: "Ban duration in hours is required"

## Future Enhancements

Potential improvements for the ban system:
1. **Ban History**: Track all past bans for a user
2. **Progressive Penalties**: Increase ban duration for repeat offenders
3. **Ban Appeals**: Allow users to appeal bans through support tickets
4. **Scheduled Unbans**: Schedule automatic unbans at specific times
5. **Permanent Bans**: Add support for permanent account bans
6. **Ban Categories**: Different ban types (payment issues, content violations, etc.)
7. **Notification Preferences**: Let users choose how they receive ban notifications

## Testing

To test the ban feature:

1. **Test Ban Customer**:
   ```bash
   curl -X POST http://localhost:8080/api/admin/customers/1/ban \
     -H "Authorization: Bearer <admin_token>" \
     -H "Content-Type: application/json" \
     -d '{"reason":"Testing ban feature","durationInHours":1}'
   ```

2. **Verify Email**: Check that the user receives a ban notification email

3. **Test Login**: Attempt to log in as the banned user (should fail)

4. **Check Status**: Verify ban status through the API

5. **Test Unban**: Unban the user and verify they can log in again

6. **Verify Audit Log**: Check that all actions are properly logged

## Support

For questions or issues with the ban management system, contact the development team or refer to the main API documentation.

