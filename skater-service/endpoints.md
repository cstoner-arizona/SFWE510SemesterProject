# Skater Service API Endpoints

Base URL (via Gateway): `http://localhost:8072`
Direct URL: `http://localhost:8081`

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

## Skater Management

### Create Skater
- **POST** `/api/skater`
- **Headers**: `Authorization: Bearer {token}`
- **Body**:
```json
{
  "username": "string",
  "email": "string",
  "displayName": "string",
  "bio": "string (max 160 characters)",
  "skillLevel": "BEGINNER|INTERMEDIATE|ADVANCED|PRO",
  "stance": "REGULAR|GOOFY|SWITCH|MONGO",
  "hometown": "string",
  "profilePhotoUrl": "string",
  "tricks": ["OLLIE", "KICKFLIP", "HEELFLIP", "etc."]
}
```
- **Response**: `201 Created` - Returns created Skater object

### Get Skater by ID
- **GET** `/api/skater/{skaterId}`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `200 OK` - Returns Skater object

### Get All Skaters
- **GET** `/api/skater/all`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `200 OK` - Returns array of all Skater objects

### Update Skater
- **PUT** `/api/skater/{skaterId}`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: Same as Create Skater (partial updates supported)
- **Response**: `200 OK` - Returns updated Skater object

### Delete Skater
- **DELETE** `/api/skater/{skaterId}`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `204 No Content`

---

## Notes
- All IDs are UUID strings
- The `bio` field has a maximum length of 160 characters
- The `tricks` field is an array of Trick enum values
- Timestamps (`created_at`, `updated_at`) are managed automatically
- JWT authentication is handled at the gateway level
- The gateway extracts the user ID from the JWT `sub` claim and sets it as `X-Skater-Id`
- Keycloak admin console: `http://localhost:8084` (admin/admin)
