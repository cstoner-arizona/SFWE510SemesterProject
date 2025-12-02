# Spot Service API Endpoints

Base URL: `http://localhost:8080`

## Authentication
Most endpoints require an `X-Skater-Id` header for authentication (temporary auth mechanism).

---

## Spot Management

### Create Spot
- **POST** `/api/spots`
- **Headers**: `X-Skater-Id: {skaterId}`
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
- **Response**: `200 OK` - Returns Spot object

### Get All Spots
- **GET** `/api/spots`
- **Response**: `200 OK` - Returns array of Spot objects

### Update Spot
- **PUT** `/api/spots/{spotId}`
- **Headers**: `X-Skater-Id: {skaterId}`
- **Body**: Same as Create Spot (partial updates supported)
- **Response**: `200 OK` - Returns updated Spot object

### Delete Spot
- **DELETE** `/api/spots/{spotId}`
- **Headers**: `X-Skater-Id: {skaterId}`
- **Response**: `204 No Content`

---

## Search & Filter

### Search Spots by Name
- **GET** `/api/spots/search?name={spotName}`
- **Query Parameters**:
  - `name` (required): Name to search for
- **Response**: `200 OK` - Returns array of matching Spot objects

### Get Spots by Founder
- **GET** `/api/spots/founder/{founderSkaterId}`
- **Response**: `200 OK` - Returns array of Spot objects created by the founder

### Get Spots in Geographic Area
- **GET** `/api/spots/area?minLat={minLat}&maxLat={maxLat}&minLon={minLon}&maxLon={maxLon}`
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
- **Headers**: `X-Skater-Id: {skaterId}`
- **Body**:
```json
{
  "trickName": "string"
}
```
- **Response**: `201 Created` - Returns created TrickAttempt object

### Get Trick Attempts for Spot
- **GET** `/api/spots/{spotId}/trick-attempts`
- **Response**: `200 OK` - Returns array of TrickAttempt objects

---

## Notes
- All IDs are UUID strings
- The `X-Skater-Id` header is a temporary authentication mechanism (TODO: Replace with actual auth)
- Only the spot founder (creator) can update or delete a spot
