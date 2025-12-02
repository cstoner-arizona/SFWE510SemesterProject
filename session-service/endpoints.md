# Session Service API Endpoints

Base URL: `http://localhost:8082`

## Authentication
All endpoints require an `X-Skater-Id` header for authentication (temporary auth mechanism).

---

## Session Management

### Start a New Session
- **POST** `/api/session`
- **Headers**: `X-Skater-Id: {skaterId}`
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
- **Headers**: `X-Skater-Id: {skaterId}`
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
- **Response**: `200 OK` - Returns Session object with all session tricks

### Get All Sessions by Skater
- **GET** `/api/session/skater/{skaterId}`
- **Response**: `200 OK` - Returns array of Session objects for the specified skater

### Get Active Session
- **GET** `/api/session/active`
- **Headers**: `X-Skater-Id: {skaterId}`
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
- The `X-Skater-Id` header is a temporary authentication mechanism (TODO: Replace with actual auth)
- Session tricks are managed through the SessionTrick entity (one-to-many relationship)
- Timestamps are in ISO 8601 format
