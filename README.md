# UniHub

## Overview

**UniHub** is an Android student companion application combined with a Python Flask backend. The app helps university students manage their courses, track grades, predict future performance, and access AI-powered academic support. 

The system architecture consists of:
- **Android Client** (`src/Android/UniHub/`) — Java-based mobile app for student-facing features
- **Python Server** (`src/Server/`) — Flask REST API providing grade prediction, AI guidance, and course data

---

## Project Architecture

### Android Client Architecture

```
MainActivity (Login Screen)
    ↓
MenuActivity (Dashboard/Home)
    ├─→ ProgressActivity (Grade tracking & predictions)
    ├─→ Calendar (Event management)
    ├─→ AiChatActivity (AI tutor)
    └─→ Local data persistence (Calendar events via JSON)
```

The app uses a **bottom navigation pattern** with 4 main sections, all accessible from any screen.

---

## Android App: Components & Features

### 1. **MainActivity** — Authentication & Login

**Purpose:** Entry point where students authenticate with their university credentials.

**Key Functionality:**
- Students enter username (must start with `p3` for validation) and password
- Username and password are sent to the server's `/get_grades` endpoint
- On success, retrieves a JSON array of all student grades from the university portal
- Navigates to `MenuActivity` and passes the grades JSON via Intent

**Technical Details:**
- Uses Retrofit 2.9.0 with Gson converter for HTTP requests
- OkHttpClient configured with 60-second timeouts for network calls
- Implements view binding for UI element access
- Base URL: `http://10.0.2.2:5002/` (Android emulator address for localhost)

**Example Flow:**
```
User enters credentials → Validates username format → API call to /get_grades 
→ Receives grades array → Passes to MenuActivity → Displays main dashboard
```

---

### 2. **MenuActivity** — Dashboard

**Purpose:** Main home screen showing student progress summary.

**Key Functionality:**
- Parses the grades JSON into `Course` objects
- Calculates and displays:
  - **M.O. (Average Grade):** Weighted average of all passed courses
  - **Passed Courses:** Count of successfully completed courses
  - **ECTS Credits:** Total accumulated credit points
- Animates a circular progress bar showing average grade (0-100 scale)
- Provides bottom navigation to other sections

**Grade Calculation Logic:**
```java
For each passed course:
  - Multiply grade by 10
  - Sum all grades
  - Divide by number of graded courses
  - Average = (sum / count)
```

**Data Passed Between Screens:**
- `grades_json` — Full array of course data (passed through all activities)
- `user_identifier` — Username for identification

---

### 3. **ProgressActivity** — Detailed Grade Analysis

**Purpose:** Comprehensive course and grade tracking with multiple organizational views.

**Key Functionality:**
- **By Semester:** Groups courses into 8 semesters (typical Greek university structure)
- **Required Courses:** Filters mandatory courses from electives
- **By Direction:** Organizes courses by specialization (e.g., Software Development, Networks, AI)
- **Grade Predictions:** Shows predicted grades for courses not yet taken
- **Failed Courses:** Lists courses that need retaking

**Course Data Model:**
```java
public class Course {
    String name;           // Course title
    double grade;          // Actual grade (0-10 scale)
    String courseCode;     // Unique identifier
    int semesterId;        // 1-8
    String courseType;     // Required/Elective
    Double ects;           // Credit points
    Boolean passed;        // Completion status
    String direction;      // Specialization category
    Boolean declared;      // Registration status
}
```

**Grade Prediction Feature:**
- Calls `/predict_grade` endpoint with:
  - `target_course` — course to predict
  - `grades` — all student grades
- Returns predicted grade based on similar courses using cosine similarity
- Shows contributing courses that influenced the prediction

**Bottom Sheet Dialog:**
- Tapping a course opens a detailed view showing grade, ECTS, course code, and semester

---

### 4. **AiChatActivity** — AI Study Assistant

**Purpose:** Real-time chat interface for academic questions answered by AI.

**Key Functionality:**
- Students type academic questions in Greek
- Questions are sent to the `/ai_model` endpoint
- Server uses a RAG (Retrieval-Augmented Generation) model trained on the university study guide PDF
- Responses appear in a chat interface with automatic scrolling
- 1.5-second delay before displaying response for UX polish

**Technical Details:**
- Uses `RecyclerView` with `MessageAdapter` to display messages
- Retrofit handles asynchronous API calls with error handling
- Toast notifications for connection failures
- Messages stored in memory (no persistence)

**Backend RAG System:**
- Loads `odhgos_spoydwn.pdf` (study guide in Greek)
- Uses HuggingFace multilingual embeddings
- Chroma vector database for semantic search
- ChatOllama (Llama3) as the language model
- Ensures responses are grounded in official study guide content

---

### 5. **Calendar** — Event & Assignment Tracker

**Purpose:** Personal calendar for tracking deadlines, exams, and assignments.

**Key Functionality:**
- Add custom events with date, time, and description
- Events stored locally as JSON in app's private files
- Delete events by swiping or tapping delete button
- Events persisted across app sessions
- Uses `RecyclerView` with `EventAdapter` for list display

**Event Data Model:**
```java
public class Event {
    String date;            // Event date
    String time;            // Event time
    String description;     // What the event is
    String user_identifier; // Tied to logged-in user
}
```

**Data Persistence:**
- Events stored in private app files (typically at `/data/data/com.example.unihub/files/`)
- File format: JSON arrays
- User-specific storage via `user_identifier`

**Adding Events:**
- Taps floating action button → `AddEventActivity`
- Returns with new event → Added to list
- Automatically saved to local storage

---

### 6. **Supporting Activities & Adapters**

**AddEventActivity:**
- UI for creating new events
- Date/time pickers for user input
- Returns event to Calendar via Intent

**Adapters (UI Components):**
- `CourseAdapter` — Displays individual courses in lists
- `EventAdapter` — Manages calendar events with delete callbacks
- `SemesterAdapter` — Groups courses by semester in ProgressActivity
- `MessageAdapter` — Renders chat messages (user + AI) in AiChatActivity

---

### 7. **API Integration (ApiService)**

**Retrofit Interface** defining all endpoints called from the Android app:

```java
public interface ApiService {
    @POST("/get_grades")
    Call<JsonArray> getGrades(@Body LoginRequest request);
    
    @GET("/get_courses")
    Call<JsonArray> getCourses(@Body GetCoursesRequest request);
    
    @POST("/ai_model")
    Call<JsonObject> aiModel(@Body AiModelRequest request);
    
    @POST("predict_grade")
    Call<JsonObject> predictGrade(@Body PredictionRequest request);
}
```

**Request Classes:**
- `LoginRequest` — username + password for authentication
- `AiModelRequest` — user's question text
- `PredictionRequest` — target course + grades for prediction
- `GetCoursesRequest` — fetch available courses

---

### 8. **Navigation Flow**

```
MainActivity (Login)
    ↓ (on success)
MenuActivity (Home Dashboard)
    ↓
Bottom navigation allows switching between:
    ├─ Menu (home) → MenuActivity
    ├─ Progress → ProgressActivity
    ├─ Calendar → Calendar (with AddEventActivity for new events)
    └─ AI Chat → AiChatActivity
```

All screens maintain `grades_json` and `user_identifier` through Intent extras to preserve state.

---

### 9. **Build Configuration**

**Android Version:**
- `compileSdk: 35` (Android 15)
- `targetSdk: 35`
- `minSdk: 23` (Android 6.0)

**Build System:**
- Gradle Kotlin DSL (`.kts` files)
- Namespace: `com.example.unihub`
- Version: 1.0

**Key Dependencies:**
- **AndroidX:** AppCompat, Material, ConstraintLayout, Navigation
- **Networking:** Retrofit 2.9.0, OkHttp3, Gson
- **UI:** RecyclerView, Binding, DrawableResources
- **Testing:** JUnit, Espresso

---

### 10. **Building & Running**

1. Open `src/Android/UniHub` in **Android Studio**
2. Sync Gradle files (`File → Sync Now`)
3. Connect emulator or physical device
4. Click **Run** (green play icon) or press `Shift+F10`
5. App installs and launches

**Note:** Ensure the Flask server is running on port 5002 before testing network features.

---

## Python Server

### Entry Point
- `src/Server/app_connection.py` — Flask REST API

### API Endpoints

#### `POST /get_grades`
Authenticates student and retrieves grades from university portal.
- **Request:** `{ "username": "p3XXXXX", "password": "..." }`
- **Response:** JSON array of courses with grades, ECTS, semester, etc.
- **Uses:** `scrape.py` module (must be present)

#### `GET /get_courses`
Returns all available courses for the program.
- **Response:** Contents of `transformed_courses.json`

#### `POST /ai_model`
Queries the AI assistant with academic questions.
- **Request:** `{ "input": "Τι είναι η OOP;" }`
- **Response:** `{ "response": "..." }`
- **Backend:** RAG system using `odhgos_spoydwn.pdf`

#### `POST /predict_grade`
Predicts student's performance in a target course.
- **Request:** `{ "target_course": 101, "grades": [...] }`
- **Response:** 
  ```json
  {
    "target_course": { "id": 101, "title": "Data Structures" },
    "predicted_grade": 7.5,
    "contributing_courses": [
      { "id": 95, "title": "Programming I", "grade": 8.0, "weight": 0.95 }
    ]
  }
  ```

### Server Files

| File | Purpose |
|------|---------|
| `app_connection.py` | Flask app and endpoint definitions |
| `grade_prediction.py` | ML logic: cosine similarity-based grade prediction |
| `model.py` | RAG model setup using LangChain + HuggingFace embeddings |
| `scrape.py` | Web scraping for university grades |
| `transformed_courses.json` | Pre-processed course catalog |
| `course_labeled.csv` | Course metadata with category labels |
| `labels.csv` | Category/subject labels for courses |
| `odhgos_spoydwn.pdf` | Study guide (Greek) — knowledge base for AI |
| `direction.json` | Mapping of specializations to courses |

### Server Setup

```bash
cd src/Server
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install flask pandas scikit-learn langchain langchain-ollama langchain-huggingface langchain-community chromadb
python app_connection.py
```

Server runs on `http://0.0.0.0:5002` by default.

---

## Architecture Highlights

### 1. **Data Flow**
```
Android App → (Retrofit) → Flask API → (Scraping/ML) → Response JSON → Android (Parse & Display)
```

### 2. **Authentication**
- Login credentials validated by pattern (must start with `p3`)
- Server authenticates against university portal
- Grades returned and cached in memory during session

### 3. **Course Management**
- Courses fetched once on login
- Organized client-side (by semester, specialization, etc.)
- Passed/failed status tracked from server data

### 4. **AI Assistance**
- Server maintains RAG chain in memory
- Initializes on app startup (explains startup delay)
- Responses grounded in official study guide

### 5. **Grade Prediction**
- Uses cosine similarity to find related courses
- Weighted average based on course similarity scores
- Fallback logic for unrelated courses

### 6. **Event Persistence**
- Local JSON storage, no server sync
- User-specific isolation via `user_identifier`
- Simple file I/O for reliability

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| App cannot connect to server | Ensure Flask runs on port 5002; check firewall |
| Login fails | Username must start with `p3`; verify credentials with university portal |
| AI chat returns errors | Confirm `odhgos_spoydwn.pdf` exists; check Ollama LLM setup |
| Grade prediction unavailable | Verify `course_labeled.csv` and `labels.csv` present |
| Calendar events disappear | Events stored locally; check app file permissions |
| Emulator cannot reach `10.0.2.2` | Use device IP instead; or configure emulator network settings |

---

## Future Enhancements

- Push notifications for upcoming deadlines
- Sync calendar events to device calendar
- Dark mode support
- Support for multiple specializations
- Real-time course availability notifications
- Offline mode with cached course data
- Grade export (PDF report)
- Study recommendations based on course history

---

## Tech Stack Summary

| Layer | Technology |
|-------|------------|
| **Frontend** | Java (Android 15), Material Design, Retrofit, RecyclerView |
| **Backend** | Python, Flask, LangChain, HuggingFace |
| **ML** | Scikit-learn, Cosine Similarity, RAG with Chroma + ChatOllama |
| **Data** | JSON, CSV (Pandas), SQLite (local events) |
| **Networking** | Retrofit 2, OkHttp3, Gson |
| **Build** | Gradle (Kotlin DSL), Android Studio |

