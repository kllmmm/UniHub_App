# UniHub - System Requirements

## Android App Requirements

### Development Environment
- **Android Studio** 2023.1 or later
- **Java/Kotlin:** JDK 11 or higher
- **Android SDK:** API level 35 (Android 15)
- **Build Tools:** 35.x

### Runtime Requirements
- **Min Android:** 6.0 (API 23)
- **Target Android:** 15 (API 35)
- **RAM:** 2 GB minimum (4 GB recommended)
- **Storage:** ~150 MB for app installation
- **Network:** Active internet connection for server communication

### Gradle Dependencies
- AndroidX AppCompat 1.x
- Material Design 3
- ConstraintLayout 2.x
- Navigation Components
- Retrofit 2.9.0 + Gson
- OkHttp3
- JUnit 4
- Espresso Testing Library

---

## Python Server Requirements

### Python Version
- **Python 3.8** or higher (3.10+ recommended)

### Core Dependencies
```
flask                    # REST API framework
pandas                   # Data processing
scikit-learn             # ML (cosine similarity for grade prediction)
langchain                # LLM orchestration
langchain-ollama         # Ollama integration
langchain-huggingface    # HuggingFace embeddings
langchain-community      # Additional LangChain utilities
chromadb                 # Vector database for RAG
```

### Optional LLM Setup
- **Ollama** (for running Llama3 locally) — https://ollama.ai
  - Download and install Ollama
  - Pull the Llama3 model: `ollama pull llama3`
  - Server expects Ollama running on `localhost:11434`

---

## System Requirements

### OS Compatibility
| OS | Requirement |
|---|---|
| **Linux** | Ubuntu 20.04 LTS or later |
| **macOS** | 10.15 (Catalina) or later |
| **Windows** | Windows 10/11 with WSL2 (recommended) or native |

### Network
- **Port 5002** available (Flask server)
- **Port 11434** available (Ollama, if using local LLM)
- Network access to university portal for credential validation

### Storage
- ~2 GB for Android Studio + SDK
- ~500 MB for Python dependencies
- ~100 MB for model files (embeddings, vector database)
- ~50 MB for PDF and data files

### Recommended Hardware
- **CPU:** Multi-core (4+ cores)
- **RAM:** 8 GB (for Ollama + Flask + development)
- **Storage:** SSD (faster build times)

---

## Installation Instructions

### Android Development Setup

1. **Install Android Studio**
   - Download from https://developer.android.com/studio
   - Follow installation wizard for your OS

2. **Install JDK**
   - Android Studio can assist with JDK 11+ installation
   - Or manually install from https://adoptium.net/

3. **Open UniHub Project**
   - Launch Android Studio
   - File → Open → Navigate to `src/Android/UniHub`

4. **Sync Gradle**
   - Wait for Gradle to sync automatically
   - If needed: File → Sync Now

5. **Create Virtual Device or Connect Physical Device**
   - For emulator: Tools → Device Manager → Create Device
   - For physical device: Enable USB debugging in developer settings

6. **Run Application**
   - Click green play icon or press `Shift+F10`
   - Select target device

### Python Server Setup

1. **Navigate to Server Directory**
   ```bash
   cd src/Server
   ```

2. **Create Virtual Environment**
   ```bash
   python -m venv venv
   ```

3. **Activate Virtual Environment**
   - **Linux/macOS:**
     ```bash
     source venv/bin/activate
     ```
   - **Windows:**
     ```bash
     venv\Scripts\activate
     ```

4. **Install Python Dependencies**
   ```bash
   pip install flask pandas scikit-learn langchain langchain-ollama langchain-huggingface langchain-community chromadb
   ```

5. **(Optional) Install and Run Ollama**
   ```bash
   # Download from https://ollama.ai
   ollama pull llama3
   ollama serve
   ```

6. **Run Flask Server**
   ```bash
   python app_connection.py
   ```
   - Server will be available at `http://0.0.0.0:5002`

---

## Required Data Files

The following files **must** be present in `src/Server/`:

| File | Purpose | Required |
|------|---------|----------|
| `odhgos_spoydwn.pdf` | Study guide for RAG model | ✅ YES (for AI chat) |
| `transformed_courses.json` | Course catalog | ✅ YES |
| `course_labeled.csv` | Course metadata | ✅ YES (for predictions) |
| `labels.csv` | Subject labels | ✅ YES (for predictions) |
| `direction.json` | Specialization mapping | ⚠️ Optional |
| `scrape.py` | Web scraping utility | ✅ YES (for /get_grades) |

---

## Network Configuration

### Android Emulator Network Access
- To connect to local Flask server on emulator:
  - Use base URL: `http://10.0.2.2:5002/`
  - `10.0.2.2` is the special alias for localhost on Android emulator

### Physical Device Network Access
- Replace `10.0.2.2` with actual machine IP address
- Example: `http://192.168.1.100:5002/`
- Both device and server must be on same network

### Firewall Configuration
- Allow incoming connections on **port 5002**
- Allow outgoing connections from Android app to server
- If using Ollama: Allow access to **port 11434** (local only)

---

## Verification Checklist

### Android Setup
- [ ] Android Studio is installed and updated
- [ ] JDK 11+ is installed and configured
- [ ] Android SDK 35 is installed
- [ ] Gradle files sync without errors
- [ ] Android emulator or physical device is ready

### Server Setup
- [ ] Python 3.8+ is installed
- [ ] Virtual environment is created
- [ ] All Python dependencies installed successfully
- [ ] Flask server starts without errors on port 5002
- [ ] `odhgos_spoydwn.pdf` exists in `src/Server/`
- [ ] `transformed_courses.json` exists in `src/Server/`
- [ ] `course_labeled.csv` exists in `src/Server/`
- [ ] `labels.csv` exists in `src/Server/`

### Network Setup
- [ ] Firewall allows port 5002
- [ ] Android device/emulator can ping server
- [ ] Server base URL configured correctly in `MainActivity`

### Optional LLM
- [ ] Ollama installed (if using AI chat feature)
- [ ] Ollama running with Llama3 model
- [ ] Ollama accessible on `localhost:11434`

---

## Troubleshooting

### "App cannot connect to server"
```
✓ Ensure Flask server is running on port 5002
✓ Check firewall settings
✓ On emulator: Use 10.0.2.2:5002
✓ On device: Use machine IP:5002
```

### "Python import errors"
```
✓ Verify virtual environment is activated
✓ Run: pip install -r src/Server/requirements.txt
```

### "Gradle sync fails in Android Studio"
```
✓ File → Invalidate Caches → Restart
✓ Delete .gradle folder and resync
```

### "AI chat returns errors"
```
✓ Verify odhgos_spoydwn.pdf exists in src/Server/
✓ Ensure Ollama is running: ollama serve
✓ Check model exists: ollama list
```

### "Grade prediction fails"
```
✓ Verify course_labeled.csv and labels.csv in src/Server/
✓ Check CSV file format and encoding
```

### "Calendar events not saving"
```
✓ Check app file permissions
✓ Verify device has sufficient storage
```

### "Emulator cannot reach 10.0.2.2"
```
✓ Try using actual machine IP instead
✓ Restart emulator
✓ Check emulator network settings
```

---

## Deployment Notes

### Development
- Run Android Studio debugger for debugging
- Run Flask with `debug=True` for verbose logging
- Use Android emulator for testing

### Production
- Disable Flask debug mode (`debug=False`)
- Use proper database for event persistence
- Implement authentication security
- Use HTTPS for server communication
- Configure CORS properly
- Implement rate limiting on API endpoints

### Performance Optimization
- Cache course data locally in SQLite
- Implement image caching for course thumbnails
- Paginate API responses for large datasets
- Use connection pooling for database

---

## Support & Resources

### Android Development
- https://developer.android.com/
- https://square.github.io/retrofit/
- https://developer.android.com/guide

### Python & Flask
- https://flask.palletsprojects.com/
- https://langchain.com/
- https://www.ollama.ai/

### Machine Learning
- https://scikit-learn.org/
- https://huggingface.co/
- https://www.trychroma.com/

---

**Last Updated:** May 31, 2026
