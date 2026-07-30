# RoomMate Finder — Flutter Developer API Guide

**Base URL (Docker / production):** `http://<host>:8015`  
**Base URL (local dev):** `http://localhost:8010`  
**Content-Type:** `application/json` (unless noted as `multipart/form-data`)  
**Auth:** All endpoints except those marked **Public** require a JWT bearer token:

```
Authorization: Bearer <token>
```

---

## Table of Contents

1. [Authentication](#1-authentication)
2. [Profile](#2-profile)
3. [Images](#3-images)
4. [Discovery (Swipe Feed)](#4-discovery-swipe-feed)
5. [Swipes](#5-swipes)
6. [Matches](#6-matches)
7. [Chat — REST](#7-chat--rest)
8. [Chat — WebSocket (STOMP)](#8-chat--websocket-stomp)
9. [Static Content & Support](#9-static-content--support)
10. [Error Responses](#10-error-responses)
11. [Data Models Reference](#11-data-models-reference)

---

## 1. Authentication

All auth endpoints are **Public** (no token required).

### 1.1 Register

```
POST /register
```

**Request body:**
```json
{
  "firstName": "Nimesh",
  "lastName": "Nepal",
  "email": "nimesh@example.com",
  "phoneNumber": "9800000000",
  "password": "secret123"
}
```

**Success 200:**
```json
{
  "message": "Registration successful. Please verify your email.",
  "userId": 42
}
```

**Error 400** — email already registered.  
**Error 429** — too many OTP requests (3 per 15 min per email).

---

### 1.2 Verify Email (OTP)

```
POST /verify
```

**Request body:**
```json
{
  "email": "nimesh@example.com",
  "otp": "483920"
}
```

**Success 200:**
```json
{
  "message": "Email verified successfully",
  "token": "<jwt>",
  "userId": 42
}
```

**Error 401** — invalid OTP.

> Store `token` and `userId` locally. `token` expires after **1 hour**.

---

### 1.3 Resend OTP

```
POST /resend-otp
```

**Request body:**
```json
{ "email": "nimesh@example.com" }
```

**Success 200:**
```json
{ "message": "OTP resent successfully" }
```

**Error 400** — account already verified.  
**Error 429** — rate limited.

---

### 1.4 Login

```
POST /login
```

**Request body:**
```json
{
  "email": "nimesh@example.com",
  "password": "secret123"
}
```

**Success 200:**
```json
{
  "token": "<jwt>",
  "userId": 42
}
```

**Error 400** — invalid credentials.  
**Error 429** — too many failed attempts (5 per 10 min per email).

---

### 1.5 Forgot Password

```
POST /forgot-password
```

**Request body:**
```json
{ "email": "nimesh@example.com" }
```

**Success 200:**
```json
{ "message": "Password reset OTP sent to your email" }
```

**Error 400** — email not found.  
**Error 429** — rate limited.

---

### 1.6 Reset Password

```
POST /reset-password
```

**Request body:**
```json
{
  "email": "nimesh@example.com",
  "otp": "291847",
  "newPassword": "newSecret123"
}
```

**newPassword** must be at least 8 characters.

**Success 200:**
```json
{ "message": "Password reset successful" }
```

**Error 401** — invalid or expired OTP.  
**Error 400** — user not found or validation failed.

---

## 2. Profile

### 2.1 Get Profile

```
GET /api/profile?userId=42
```

**Success 200:**
```json
{
  "id": 42,
  "firstName": "Nimesh",
  "lastName": "Nepal",
  "email": "nimesh@example.com",
  "phoneNumber": "9800000000",
  "student": true,
  "studyLevel": "Undergraduate",
  "occupation": "Student",
  "profileCompleted": true,
  "verified": true,
  "searchingRoommate": true,
  "searchingRoom": false,
  "roommateGender": "Male",
  "propertyDescription": "3BHK near campus, clean and quiet.",
  "notes": "Looking for a non-smoker.",
  "minBudget": 5000.0,
  "maxBudget": 10000.0,
  "sleepingSchedule": "Night owl",
  "cleanliness": 4,
  "smoking": false,
  "diet": "Vegetarian",
  "hobbies": "Reading, Gaming, Cooking",
  "musicPreference": "Pop",
  "petPreference": "No pets",
  "preferredMale": true,
  "preferredFemale": false,
  "images": [
    { "id": 1, "name": "uuid_photo.jpg", "type": "image/jpeg", "imageData": "/app/images/uuid_photo.jpg" }
  ],
  "imageCount": 1
}
```

**Error 403** — `userId` does not match the authenticated user.

---

### 2.2 Update Profile

```
PUT /api/profile
Content-Type: multipart/form-data
```

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long | **Required** — must match authenticated user |
| `firstName` | String | |
| `lastName` | String | |
| `phoneNumber` | String | |
| `student` | Boolean | |
| `studyLevel` | String | |
| `occupation` | String | |
| `searchingRoommate` | Boolean | |
| `searchingRoom` | Boolean | |
| `roommateGender` | String | e.g. `"Male"`, `"Female"` |
| `propertyDescription` | String | Required when `searchingRoommate=true` |
| `notes` | String | |
| `minBudget` | Double | Must be ≤ maxBudget |
| `maxBudget` | Double | |
| `sleepingSchedule` | String | e.g. `"Night owl"`, `"Early bird"` |
| `cleanliness` | Integer | 1 (messy) to 5 (very clean) |
| `smoking` | Boolean | |
| `diet` | String | e.g. `"Vegetarian"`, `"Non-vegetarian"` |
| `hobbies` | String | Comma-separated: `"Reading, Gaming"` |
| `musicPreference` | String | |
| `petPreference` | String | |
| `preferredMale` | Boolean | |
| `preferredFemale` | Boolean | |
| `images` | File[] | Optional — replaces existing images |

**Validation rules:**
- `searchingRoommate=true` → `propertyDescription` required + minimum **4 images**.
- `searchingRoom=true` → minimum **1 image**.
- `minBudget` ≤ `maxBudget`.

**Success 200:** returns the updated profile (same shape as Get Profile).  
**Error 400** — validation failed.  
**Error 403** — `id` doesn't match authenticated user.

---

## 3. Images

### 3.1 Upload / Replace Images

```
POST /upload
Content-Type: multipart/form-data
```

| Field | Type | Notes |
|-------|------|-------|
| `userId` | Long | **Required** |
| `images` | File[] | One or more image files (max 10 MB each) |

**Success 200:**
```json
{ "message": "Images uploaded successfully" }
```

---

## 4. Discovery (Swipe Feed)

Returns a ranked list of compatible users excluding already-swiped profiles.

```
GET /api/discovery?userId=42
```

**Success 200:**
```json
[
  {
    "id": 55,
    "firstName": "Priya",
    "lastName": "Sharma",
    "roommateGender": "Female",
    "minBudget": 6000.0,
    "maxBudget": 12000.0,
    "occupation": "Engineer",
    "student": false,
    "studyLevel": null,
    "sleepingSchedule": "Night owl",
    "cleanliness": 4,
    "smoking": false,
    "diet": "Vegetarian",
    "hobbies": "Reading, Cooking",
    "musicPreference": "Pop",
    "petPreference": "Cats",
    "searchingRoommate": false,
    "searchingRoom": true,
    "propertyDescription": null,
    "notes": "Prefer quiet evenings.",
    "compatibilityScore": 87.5,
    "images": [
      { "id": 3, "name": "abc.jpg", "type": "image/jpeg", "imageData": "/app/images/abc.jpg" }
    ]
  }
]
```

List is sorted by `compatibilityScore` descending. Candidates with score 0 (gender mismatch) are excluded.

**Error 403** — `userId` doesn't match authenticated user.

---

## 5. Swipes

### 5.1 Record a Swipe

```
POST /api/swipes
```

**Request body:**
```json
{
  "swiperId": 42,
  "swipedId": 55,
  "liked": true
}
```

`liked: true` = swipe right (like), `liked: false` = swipe left (pass).

**Success 200:**
```json
{
  "id": 101,
  "swiperId": 42,
  "swipedId": 55,
  "liked": true,
  "swipedAt": "2026-06-26T10:30:00"
}
```

**Business logic:**
- If `liked=true` and the other user already liked back → a **ACCEPTED** match is created.
- If `liked=true` and no reciprocal like yet → a **PENDING** match is created.
- If `liked=false` → no match created.
- Swiping the same user twice → **400** "Already swiped on this user".

**Error 403** — `swiperId` doesn't match authenticated user.  
**Error 400** — duplicate swipe.  
**Error 404** — user not found.

---

## 6. Matches

### 6.1 Get All Matches

```
GET /api/matches/user/{userId}
```

Returns all matches (PENDING, ACCEPTED, REJECTED) for the user.

**Success 200:** Array of match objects (see [MatchDto](#matchdto)).

---

### 6.2 Get Accepted Matches

```
GET /api/matches/user/{userId}/accepted
```

Returns only ACCEPTED matches — use this to populate the **Matches** screen.

---

### 6.3 Get Pending Matches

```
GET /api/matches/user/{userId}/pending
```

Returns PENDING matches where the **current user is `user2`** (i.e. someone liked them and is waiting for a response).

---

### 6.4 Accept a Match

```
PUT /api/matches/accept/{matchId}
```

No request body. The caller must be a participant of the match.

**Success 200:** Updated match object with `status: "ACCEPTED"`.  
**Error 403** — caller is not a participant.  
**Error 400** — match is not in PENDING status.

---

### 6.5 Reject a Match

```
PUT /api/matches/reject/{matchId}
```

No request body.

**Success 200:** Updated match object with `status: "REJECTED"`.  
**Error 403** — caller is not a participant.

---

## 7. Chat — REST

### 7.1 Get Chat History

```
GET /api/chat/{matchId}/messages
```

Returns all messages for a match in chronological order. Caller must be a participant.

**Success 200:**
```json
[
  {
    "id": 1,
    "matchId": 10,
    "senderId": 42,
    "senderName": "Nimesh Nepal",
    "content": "Hey! Are you still looking for a roommate?",
    "messageType": "TEXT",
    "sentAt": "2026-06-26T14:05:00"
  }
]
```

**Error 403** — caller is not a match participant.  
**Error 404** — match not found.

---

### 7.2 Get Conversations (Inbox)

```
GET /api/chat/conversations?userId=42
```

Returns one entry per accepted match, sorted by most-recent message first. Use this to render the **chat list / inbox** screen.

**Success 200:**
```json
[
  {
    "matchId": 10,
    "otherUserId": 55,
    "otherUserName": "Priya Sharma",
    "matchScore": 87.5,
    "lastMessage": "See you tomorrow!",
    "lastMessageAt": "2026-06-26T18:45:00"
  }
]
```

**Error 403** — `userId` doesn't match authenticated user.

---

## 8. Chat — WebSocket (STOMP)

### 8.1 Connect

**Endpoint:** `ws://<host>/ws-chat`  
**Protocol:** STOMP over WebSocket

Include the JWT in the STOMP `CONNECT` frame header:

```
CONNECT
Authorization: Bearer <token>
```

If the token is missing or expired the connection is refused with an error.

---

### 8.2 Send a Message

**Destination:** `/app/chat.send/{matchId}`

**Payload (JSON):**
```json
{
  "content": "Hello!",
  "messageType": "TEXT",
  "sentAt": "2026-06-26T14:05:00"
}
```

> `senderId` is resolved from the JWT — do **not** send it in the payload.

---

### 8.3 Subscribe to a Chat Room

**Topic:** `/topic/chat/{matchId}`

Subscribe before (or immediately after) connecting. Each message broadcast on this topic is a `ChatMessageDto` (same shape as the REST history response).

```json
{
  "id": 201,
  "matchId": 10,
  "senderId": 55,
  "senderName": "Priya Sharma",
  "content": "Hello!",
  "messageType": "TEXT",
  "sentAt": "2026-06-26T14:05:30"
}
```

---

### 8.4 Flutter STOMP Setup (example)

```dart
// Add stomp_dart_client to pubspec.yaml
import 'package:stomp_dart_client/stomp.dart';
import 'package:stomp_dart_client/stomp_config.dart';
import 'package:stomp_dart_client/stomp_frame.dart';

StompClient client = StompClient(
  config: StompConfig(
    url: 'ws://localhost:8010/ws-chat',
    onConnect: onConnected,
    beforeConnect: () async {},
    onWebSocketError: (dynamic error) => debugPrint(error.toString()),
    stompConnectHeaders: {'Authorization': 'Bearer $token'},
    webSocketConnectHeaders: {'Authorization': 'Bearer $token'},
  ),
);

void onConnected(StompFrame frame) {
  client.subscribe(
    destination: '/topic/chat/$matchId',
    callback: (frame) {
      final message = jsonDecode(frame.body!);
      // update UI
    },
  );
}

// Send a message
client.send(
  destination: '/app/chat.send/$matchId',
  body: jsonEncode({'content': 'Hi!', 'messageType': 'TEXT'}),
);
```

---

## 9. Static Content & Support

### 9.1 Get Static Content

```
GET /api/services/{service}
```

`{service}` must be one of: `NOTIFICATION`, `PRIVACY`, `ABOUT`, `TERMS`, `FAQ`

**Success 200:**
```json
{
  "service": "PRIVACY",
  "title": "Privacy Policy",
  "content": "..."
}
```

**Error 404** — content not found for that service type.

---

### 9.2 Submit Help & Support Request

```
POST /api/support
```

**Request body:**
```json
{
  "username": "Nimesh Nepal",
  "phoneNumber": "9800000000",
  "email": "nimesh@example.com",
  "title": "Cannot upload images",
  "body": "When I try to upload a profile picture..."
}
```

**Success 200:**
```json
{ "message": "Your support request has been submitted." }
```

---

## 10. Error Responses

All error responses follow this shape:

```json
{ "error": "Human-readable error message" }
```

| HTTP Status | Meaning |
|-------------|---------|
| 400 | Bad request / validation failed |
| 401 | Invalid credentials or OTP |
| 403 | Forbidden — you don't own this resource |
| 404 | Resource not found |
| 429 | Rate limit exceeded |
| 500 | Internal server error |

---

## 11. Data Models Reference

### MatchDto
```json
{
  "id": 10,
  "user1Id": 42,
  "user2Id": 55,
  "matchScore": 87.5,
  "status": "ACCEPTED",
  "matchedAt": "2026-06-25T09:00:00",
  "lastInteraction": "2026-06-26T18:45:00"
}
```

`status` values: `PENDING`, `ACCEPTED`, `REJECTED`

---

### SwipeDto
```json
{
  "id": 101,
  "swiperId": 42,
  "swipedId": 55,
  "liked": true,
  "swipedAt": "2026-06-26T10:30:00"
}
```

---

### ChatMessageDto
```json
{
  "id": 201,
  "matchId": 10,
  "senderId": 42,
  "senderName": "Nimesh Nepal",
  "content": "Hey!",
  "messageType": "TEXT",
  "sentAt": "2026-06-26T14:05:00"
}
```

---

### ConversationDto
```json
{
  "matchId": 10,
  "otherUserId": 55,
  "otherUserName": "Priya Sharma",
  "matchScore": 87.5,
  "lastMessage": "See you tomorrow!",
  "lastMessageAt": "2026-06-26T18:45:00"
}
```

---

### ImageDto
```json
{
  "id": 3,
  "name": "uuid_filename.jpg",
  "type": "image/jpeg",
  "imageData": "/app/images/uuid_filename.jpg"
}
```

> `imageData` is a **server-side file path**, not a URL. You will need to either:
> - Serve static files via an endpoint (e.g., `GET /images/{filename}`), or
> - Encode images as base64 via `GET /upload` and store in `imageData`.  
>
> Confirm the image delivery strategy with the backend developer.

---

## 12. Typical Flutter Flow

```
1. POST /register          → save userId
2. POST /verify            → save token + userId
   (or POST /login)

3. GET /api/profile?userId → check profileCompleted
   └─ if false: PUT /api/profile (multipart) to complete profile

4. GET /api/discovery?userId → show swipe deck
   └─ on swipe right: POST /api/swipes { liked: true }
   └─ on swipe left:  POST /api/swipes { liked: false }

5. GET /api/matches/user/{userId}/accepted → show matches list

6. GET /api/chat/conversations?userId      → show chat inbox
   └─ tap conversation: GET /api/chat/{matchId}/messages
   └─ send: WebSocket STOMP /app/chat.send/{matchId}
   └─ receive: subscribe /topic/chat/{matchId}
```
