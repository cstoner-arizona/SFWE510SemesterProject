# Spot Service API Endpoints

Base URL (via Gateway): `http://localhost:8072`
Direct URL: `http://localhost:8080`

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

## Spot Management

### Create Spot
- **POST** `/api/spots`
- **Headers**: `Authorization: Bearer {token}`
- **Body**:
```json
{
  "name": "string",
  "description": "string",
  "streetAddress": "string",
  "city": "string",
  "state": "string",
  "zipCode": "string",
  "latitude": 0.0,
  "longitude": 0.0,
  "difficulty": "BEGINNER|INTERMEDIATE|ADVANCED|EXPERT",
  "terrainType": "STREET|PARK|VERT|BOWL|RAILS|STAIRS|LEDGE|GAP|MANUAL_PAD|TRANSITION|FLATGROUND|OTHER",
  "isPublic": true
}
```
- **Response**: `201 Created` - Returns created Spot object

### Get Spot by ID
- **GET** `/api/spots/{spotId}`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `200 OK` - Returns Spot object

### Get All Spots
- **GET** `/api/spots`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `200 OK` - Returns array of Spot objects

### Update Spot
- **PUT** `/api/spots/{spotId}`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: Same as Create Spot (partial updates supported)
- **Response**: `200 OK` - Returns updated Spot object

### Delete Spot
- **DELETE** `/api/spots/{spotId}`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `204 No Content`

---

## Search & Filter

### Search Spots by Name
- **GET** `/api/spots/search?name={spotName}`
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `name` (required): Name to search for
- **Response**: `200 OK` - Returns array of matching Spot objects

### Get Spots by Founder
- **GET** `/api/spots/founder/{founderSkaterId}`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `200 OK` - Returns array of Spot objects created by the founder

### Get Spots in Geographic Area
- **GET** `/api/spots/area?minLat={minLat}&maxLat={maxLat}&minLon={minLon}&maxLon={maxLon}`
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `minLat` (required): Minimum latitude
  - `maxLat` (required): Maximum latitude
  - `minLon` (required): Minimum longitude
  - `maxLon` (required): Maximum longitude
- **Response**: `200 OK` - Returns array of Spot objects within the area

---

## Trick Attempts

### Add Trick Attempt to Spot
- **POST** `/api/spots/{spotId}/trick-attempts`
- **Headers**: `Authorization: Bearer {token}`
- **Body**:
```json
{
  "trickName": "string"
}
```
- **Response**: `201 Created` - Returns created TrickAttempt object

### Get Trick Attempts for Spot
- **GET** `/api/spots/{spotId}/trick-attempts`
- **Headers**: `Authorization: Bearer {token}`
- **Response**: `200 OK` - Returns array of TrickAttempt objects

---

## Notes
- All IDs are UUID strings
- JWT authentication is handled at the gateway level
- The gateway extracts the user ID from the JWT `sub` claim and sets it as `X-Skater-Id`
- Only the spot founder (creator) can update or delete a spot
- Keycloak admin console: `http://localhost:8084` (admin/admin)
