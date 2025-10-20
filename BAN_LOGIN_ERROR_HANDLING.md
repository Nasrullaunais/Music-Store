# User Ban Login Error Handling - Implementation Guide

## Overview
The system now provides user-friendly error messages when banned users attempt to log in. Instead of a generic "invalid credentials" message, users receive detailed information about their ban including the reason and expiration time.

## Backend Implementation

### Error Response Structure

When a banned user tries to log in, the API returns a **403 Forbidden** status with the following response:

```json
{
  "message": "Your account has been temporarily banned",
  "errorType": "ACCOUNT_BANNED",
  "banReason": "Violation of community guidelines",
  "bannedUntil": "2025-10-20T10:30:00",
  "timestamp": "2025-10-19T19:03:52"
}
```

### Status Codes

- **403 Forbidden**: User is banned or disabled
- **400 Bad Request**: Invalid username or password
- **200 OK**: Successful login

## Frontend Implementation

### React/TypeScript Example

```typescript
// Login function with ban error handling
async function handleLogin(username: string, password: string) {
  try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password }),
    });

    const data = await response.json();

    if (response.ok) {
      // Successful login
      localStorage.setItem('token', data.token);
      // Redirect to dashboard
      window.location.href = '/dashboard';
    } else if (response.status === 403) {
      // User is banned or disabled
      if (data.errorType === 'ACCOUNT_BANNED') {
        // Show ban details to user
        showBanMessage(data);
      } else {
        // Account disabled
        alert(data.message);
      }
    } else {
      // Invalid credentials
      alert(data.message || 'Invalid username or password');
    }
  } catch (error) {
    console.error('Login error:', error);
    alert('An error occurred during login');
  }
}

function showBanMessage(banData: any) {
  const bannedUntil = new Date(banData.bannedUntil);
  const formattedDate = bannedUntil.toLocaleString();
  
  const message = `
    Your account has been temporarily banned.
    
    Reason: ${banData.banReason}
    Ban expires: ${formattedDate}
    
    If you believe this is a mistake, please contact support.
  `;
  
  alert(message);
  // Or show in a modal/toast notification
}
```

### React Component Example

```tsx
import React, { useState } from 'react';

interface BanInfo {
  message: string;
  errorType: string;
  banReason: string;
  bannedUntil: string;
  timestamp: string;
}

function LoginForm() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [banInfo, setBanInfo] = useState<BanInfo | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setBanInfo(null);

    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });

      const data = await response.json();

      if (response.ok) {
        localStorage.setItem('token', data.token);
        window.location.href = '/dashboard';
      } else if (response.status === 403 && data.errorType === 'ACCOUNT_BANNED') {
        setBanInfo(data);
      } else {
        setError(data.message || 'Login failed');
      }
    } catch (error) {
      setError('An error occurred. Please try again.');
    }
  };

  return (
    <div className="login-container">
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <button type="submit">Login</button>
      </form>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      {banInfo && (
        <div className="ban-message">
          <h3>⚠️ Account Banned</h3>
          <p><strong>Reason:</strong> {banInfo.banReason}</p>
          <p><strong>Ban expires:</strong> {new Date(banInfo.bannedUntil).toLocaleString()}</p>
          <p>If you believe this is a mistake, please contact support.</p>
        </div>
      )}
    </div>
  );
}

export default LoginForm;
```

### JavaScript (Vanilla) Example

```javascript
document.getElementById('loginForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  
  const username = document.getElementById('username').value;
  const password = document.getElementById('password').value;
  
  try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    
    const data = await response.json();
    
    if (response.ok) {
      localStorage.setItem('token', data.token);
      window.location.href = '/dashboard';
    } else if (response.status === 403) {
      if (data.errorType === 'ACCOUNT_BANNED') {
        // Show ban message
        const bannedUntil = new Date(data.bannedUntil).toLocaleString();
        document.getElementById('errorMessage').innerHTML = `
          <div class="ban-alert">
            <h3>⚠️ Account Temporarily Banned</h3>
            <p><strong>Reason:</strong> ${data.banReason}</p>
            <p><strong>Ban expires:</strong> ${bannedUntil}</p>
            <p>If you believe this is a mistake, please contact support.</p>
          </div>
        `;
      } else {
        document.getElementById('errorMessage').textContent = data.message;
      }
    } else {
      document.getElementById('errorMessage').textContent = 
        data.message || 'Invalid username or password';
    }
  } catch (error) {
    document.getElementById('errorMessage').textContent = 
      'An error occurred during login';
  }
});
```

## CSS Styling Example

```css
.ban-message {
  background-color: #fff3cd;
  border: 2px solid #ffc107;
  border-radius: 8px;
  padding: 20px;
  margin-top: 20px;
  color: #856404;
}

.ban-message h3 {
  margin-top: 0;
  color: #d32f2f;
}

.ban-message p {
  margin: 10px 0;
}

.ban-message strong {
  color: #000;
}

.error-message {
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  border-radius: 4px;
  padding: 12px;
  margin-top: 10px;
  color: #721c24;
}
```

## Testing

### Test a Banned User Login

1. **Ban a user** (as admin):
```bash
curl -X POST http://localhost:8080/api/admin/customers/1/ban \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"reason":"Testing ban feature","durationInHours":24}'
```

2. **Try to login** as the banned user:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"bannedUser","password":"password123"}'
```

3. **Expected Response** (403 Forbidden):
```json
{
  "message": "Your account has been temporarily banned",
  "errorType": "ACCOUNT_BANNED",
  "banReason": "Testing ban feature",
  "bannedUntil": "2025-10-20T19:03:52",
  "timestamp": "2025-10-19T19:03:52"
}
```

## Key Features

✅ **User-Friendly Messages**: Clear communication about ban status
✅ **Ban Details**: Users see the reason and expiration time
✅ **Distinct Error Types**: Different handling for banned vs disabled accounts
✅ **Security**: Password is still validated, preventing username enumeration
✅ **Auto-Expiration Check**: System checks if ban has expired before showing error

## Error Type Differentiation

The system distinguishes between three states:

1. **ACCOUNT_BANNED** (403): User is temporarily banned with details
2. **Account Disabled** (403): User account is disabled (generic message)
3. **Invalid Credentials** (400): Wrong username or password

## Support Contact

Users should be directed to support if they:
- Believe the ban was issued in error
- Want to appeal the ban
- Have questions about the ban reason

Contact support at: support@musicstore.com (or your support channel)

