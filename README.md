# UniHub

## Overview

`UniHub` is a student support project combining:
- an Android client app under `src/Android/UniHub`
- a Python Flask backend under `src/Server`

The Android application uses Retrofit to communicate with the server, while the server provides:
- grade prediction functionality
- AI-powered study guide responses using the `odhgos_spoydwn.pdf`
- course and grade retrieval endpoints

## Repository Structure

- `src/Android/UniHub/`
  - Android Studio project for the UniHub mobile app
  - `app/` contains the application module
- `src/Server/`
  - Python backend and data assets
  - `app_connection.py` is the Flask API entrypoint
  - `grade_prediction.py` contains the prediction logic
  - `model.py` builds the RAG model from the study guide PDF
  - `transformed_courses.json` contains course data served by the API
  - `course_labeled.csv`, `labels.csv` are used for grade prediction
  - `odhgos_spoydwn.pdf` is the study guide used for AI responses

## Android App

### Key details
- Android client source is Java under `app/src/main/java/com/example/unihub`
- Gradle Kotlin DSL project configuration
- `compileSdk` and `targetSdk` set to 35
- Minimum supported Android API level: 23
- `applicationId`: `com.example.unihub`

### Main dependencies
- AndroidX AppCompat
- Material components
- ConstraintLayout
- Navigation components
- Retrofit 2.9.0 with Gson converter

### Running the Android app

1. Open `src/Android/UniHub` in Android Studio.
2. Sync Gradle and build the project.
3. Run the `app` module on an emulator or physical device.

## Python Server

### Entry point
- `src/Server/app_connection.py`

### API endpoints
- `POST /predict_grade`
  - expects JSON with `target_course` and `grades`
  - returns a predicted grade and contributing course data
- `POST /ai_model`
  - expects JSON with `input`
  - returns an answer based on the study guide PDF
- `GET /get_courses`
  - returns the contents of `transformed_courses.json`
- `POST /get_grades`
  - expects JSON with `username` and `password`
  - returns scraped grade data from the student portal

### Model setup
- `src/Server/model.py` loads `odhgos_spoydwn.pdf`
- Builds a retrieval-augmented generation (RAG) chain using:
  - `langchain`
  - `HuggingFaceEmbeddings`
  - `Chroma`
  - `ChatOllama`

### Grade prediction logic
- `src/Server/grade_prediction.py`
- Uses `pandas`, `scikit-learn`, and cosine similarity to compute a predicted grade
- Generates top contributing courses for the prediction

### Required files
- `src/Server/odhgos_spoydwn.pdf`
- `src/Server/transformed_courses.json`
- `src/Server/course_labeled.csv`
- `src/Server/labels.csv`
- `src/Server/direction.json`

### Notes
- The `/get_grades` endpoint depends on a `scrape.py` module, which must be present in the same directory to function correctly.
- `src/Server/app_connection.py` initializes the AI model on startup, so the Flask server may take extra time to begin serving requests.

## Setup Recommendations

### Python environment

Create a virtual environment and install packages required for Flask, data processing, and the language model stack.

Example:
```bash
cd src/Server
python -m venv venv
source venv/-r requirements.txt
pip install flask pandas scikit-learn langchain langchain-llama langchain-huggingface langchain-community chromadb
```

> If your project already has a dependency file, install from it instead.

### Running the server

```bash
cd src/Server
python app_connection.py
```

The server starts on `http://0.0.0.0:5002` by default.

## Troubleshooting

- If the Android app cannot reach the backend, ensure the Flask server is running and the device/emulator network allows access.
- If `/get_grades` fails, verify that `scrape.py` exists and the student portal credentials are correct.
- If the AI endpoint fails, confirm the `odhgos_spoydwn.pdf` file is present and readable.

## Optional Improvements

- add a `requirements.txt` or `pyproject.toml` for the Python backend
- add Kotlin-specific documentation for Android navigation destinations and Retrofit interfaces
- implement error handling and logging for missing backend dependencies
