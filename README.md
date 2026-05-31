
# UniHub

UniHub is an integrated student support ecosystem designed to streamline academic management. The platform consists of a native Android client application providing a modern user interface, backed by a robust Python Flask server that leverages Machine Learning for grade prediction and a Retrieval-Augmented Generation (RAG) pipeline for AI-powered academic advisory responses.

## Key Features

* **Academic Performance Tracking:** Securely retrieve and archive course grades directly via automated portal scraping.
* **Predictive Analytics:** Grade forecasting built on cosine similarity models to project academic performance in upcoming semesters.
* **Intelligent Study Guide (RAG):** An AI assistant capable of answering complex curriculum and policy queries using institutional documentation (`odhgos_spoydwn.pdf`).

---

## Project Architecture

```text
unihub/
├── src/
│   ├── Android/
│   │   └── UniHub/                # Native Android Studio Project (Java)
│   │       └── app/               # Application module source and assets
│   └── Server/                    # Python Flask Microservice Backend
│       ├── app_connection.py      # WSGI/Flask Application Entry Point
│       ├── grade_prediction.py    # Cosine Similarity Engine
│       ├── model.py               # LangChain & Vector Embeddings Pipeline
│       └── scrape.py              # Portal Integration Script

```

---

## Tech Stack

### Android Client

* **Language:** Java
* **Target SDK:** 35 (Minimum SDK: 23)
* **Networking:** Retrofit 2.9.0 with Type-Safe Gson Converters
* **UI Architecture:** Material Components, ConstraintLayout, Jetpack Navigation Components

### Server Backend

* **Core Framework:** Python Flask
* **Data Science:** Pandas, Scikit-Learn
* **AI Engine:** LangChain, HuggingFace Embeddings, ChromaDB, ChatOllama

---

## Getting Started

### Backend Deployment

#### Prerequisites

* Python 3.10 or higher
* An active local instance of **Ollama** running the target LLM configuration.

#### Installation & Execution

1. Navigate to the server repository and initialize a virtual environment:

```bash
   cd src/Server
   python -m venv venv
   source venv/bin/activate  # On Windows use: venv\Scripts\activate

```

2. Install the production dependencies:

```bash
   pip install flask pandas scikit-learn langchain langchain-huggingface langchain-community chromadb

```

3. Ensure the following mandatory data assets are placed in `src/Server/`:
* `odhgos_spoydwn.pdf` (Institutional Study Guide)
* `transformed_courses.json` (Formatted Course Data)
* `course_labeled.csv` & `labels.csv` (Training datasets for grade prediction)
* `direction.json` & `scrape.py`


4. Launch the application server:

```bash
   python app_connection.py

```

> **Note:** The server binds to `http://0.0.0.0:5002` by default. Initial startup may take a moment while loading the vector embeddings and the HuggingFace transformer model into memory.

### Android Client Deployment

1. Launch **Android Studio** (Ladybug or newer recommended).
2. Select **Open An Existing Project** and direct it to `src/Android/UniHub`.
3. Synchronize the project with its Gradle files using the Kotlin DSL configuration.
4. Set up an Android Virtual Device (AVD) running API Level 23 or higher.
5. Deploy the application by executing the `app` module.

---

## API Reference Manual

### Core Endpoints

| Method | Endpoint | Payload Format | Description |
| --- | --- | --- | --- |
| `POST` | `/predict_grade` | `{"target_course": "String", "grades": {}}` | Computes the predicted grade using peer matrix correlation and lists top contributing modules. |
| `POST` | `/ai_model` | `{"input": "String"}` | Queries the vector database and generates context-aware replies via the RAG pipeline. |
| `GET` | `/get_courses` | *None* | Exposes cached curriculum structures parsed from `transformed_courses.json`. |
| `POST` | `/get_grades` | `{"username": "String", "password": "String"}` | Authenticates against the external student portal using `scrape.py` to extract realtime grades. |

---

## Troubleshooting

### Connectivity & Network Failures

* **Emulator Isolation:** Cleartext HTTP traffic or local loops (`127.0.0.1`) are restricted by default in Android SDK 35. For local debugging on an emulator, configure your Retrofit base URL to use the host loopback alias: `http://10.0.2.2:5002`.
* **Production Deployment:** Ensure proper network routing or update the network security configuration file (`network_security_config.xml`) within the Android project settings to permit unsecured HTTP testing if applicable.

### Missing Dependency Failures

* **Scraper Errors:** If the `/get_grades` route fails with 500-series status codes, confirm that `scrape.py` is present in the execution path and that browser drivers or structural updates to the student portal have not broken parsing rules.
* **Vector Engine Failures:** Ensure that `odhgos_spoydwn.pdf` is fully legible, text-extractable, and not password-locked. If Chroma initialization fails, remove the local persistent index directory and restart the server to allow rebuilding the database embeddings.

```

```
