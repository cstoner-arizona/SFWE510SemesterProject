# Skater Service API Endpoints

Base URL: `http://localhost:8081`

---

## Skater Management

### Create Skater
- **POST** `/api/skater`
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
- **Response**: `200 OK` - Returns Skater object

### Get All Skaters
- **GET** `/api/skater/all`
- **Response**: `200 OK` - Returns array of all Skater objects

### Update Skater
- **PUT** `/api/skater/{skaterId}`
- **Body**: Same as Create Skater (partial updates supported)
- **Response**: `200 OK` - Returns updated Skater object

### Delete Skater
- **DELETE** `/api/skater/{skaterId}`
- **Response**: `204 No Content`

---

## Notes
- All IDs are UUID strings
- The `bio` field has a maximum length of 160 characters
- The `tricks` field is an array of Trick enum values
- Timestamps (`created_at`, `updated_at`) are managed automatically
