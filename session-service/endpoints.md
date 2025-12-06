# Session Service API Endpoints

Base URL (via Gateway): `http://localhost:8072`
Direct URL: `http://localhost:8082`

## Authentication

All endpoints require JWT authentication via Keycloak. The gateway extracts the `sub` claim from the JWT and forwards it as the `X-Skater-Id` header to downstream services.

### Getting a JWT Token

**POST** `http://localhost:8084/realms/skate/protocol/openid-connect/token`

**Body** (x-www-form-urlencoded):
| Key | Value |
|-----|-------|
| `grant_type` | `password` |
| `client_id` | `skate-app` |
| `client_secret` | `skate-app-secret` |
| `username` | `tony_hawk` |
| `password` | `password` |

**Response**:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### Using the Token

Add the following header to all API requests:
```
Authorization: Bearer {access_token}
```

### Test Users

| Username | Password | Skater ID |
|----------|----------|-----------|
| `tony_hawk` | `password` | `650e8400-e29b-41d4-a716-446655440001` |
| `sk8ergirl` | `password` | `650e8400-e29b-41d4-a716-446655440002` |
| `grind_master` | `password` | `650e8400-e29b-41d4-a716-446655440003` |
| `flip_wizard` | `password` | `650e8400-e29b-41d4-a716-446655440004` |
| `newbie_skate` | `password` | `650e8400-e29b-41d4-a716-446655440005` |

---

## Session Management

### Start a New Session
- **POST** `/api/session`
- **Headers**: `Authorization: Bearer {token}`
- **Body**:
```json
{
  "spotId": "string (UUID)",
  "weather": "string (optional)",
  "notes": "string (optional)"
}
```
- **Response**: `201 Created` - Returns created Session object
- **Notes**: A skater can only have one active session at a time

### End an Active Session
- **PUT** `/api/session/{sessionId}/end`
- **Headers**: `Authorization: Bearer {token}`
- **Body** (optional):
```json
{
  "rating": 5
}
```
- **Response**: `200 OK` - Returns updated Session object
- **Notes**: Rating is optional (1-5 scale)

### Get Session by ID
- **GET** `/api/session/{sessionId}`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `200 OK` - Returns Session object with all session tricks

### Get All Sessions by Skater
- **GET** `/api/session/skater/{skaterId}`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `200 OK` - Returns array of Session objects for the specified skater

### Get Active Session
- **GET** `/api/session/active`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `200 OK` - Returns the currently active Session for the authenticated skater
- **Notes**: Returns the session where `endTime` is null

---

## Session Object Structure

```json
{
  "sessionId": "string (UUID)",
  "skaterId": "string (UUID)",
  "spotId": "string (UUID)",
  "startTime": "2025-12-02T10:30:00",
  "endTime": "2025-12-02T12:45:00",
  "notes": "string",
  "weather": "string",
  "sessionTricks": [],
  "rating": 5,
  "createdAt": "2025-12-02T10:30:00"
}
```

---

## Notes
- All IDs are UUID strings
- Sessions track when a skater visits a spot
- A skater can only have one active session at a time (endTime must be null)
- Session tricks are managed through the SessionTrick entity (one-to-many relationship)
- Timestamps are in ISO 8601 format
- JWT authentication is handled at the gateway level
- The gateway extracts the user ID from the JWT `sub` claim and sets it as `X-Skater-Id`
- Keycloak admin console: `http://localhost:8084` (admin/admin)
