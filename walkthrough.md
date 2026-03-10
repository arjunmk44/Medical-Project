# Medical ML Platform: Getting Started Guide

Follow these steps to launch the entire platform on your machine.

## 1. Prerequisites
Ensure the following are installed (we handles these in the previous steps):
- **Docker Desktop**: Must be open and running.
- **Node.js**: For frontend development (optional if using Docker).
- **Python & Maven**: For backend/ML development (optional if using Docker).

![MedicalML Login Page](file:///C:/Users/mbvis/.gemini/antigravity/brain/5c31cdb3-8ee1-4cfe-bdc4-41c96ebe9126/frontend_check_1773131914753.webp)

## 2. Launching the Platform

### Option A: The Easy Way (Recommended)
The project includes a launcher script that handles environment configuration and service startup:

1. Open **PowerShell** or **Command Prompt** in the project root:
   `c:\Users\mbvis\OneDrive\Desktop\Engineering\Projects\Medical-Project`
2. Run the launch script:
   ```powershell
   .\launch.bat
   ```
   *This script will create your `.env` file, build the images, and start the containers.*

---

### Option B: Manual Docker Commands
If you prefer to run the commands manually:

1. **Prepare Environment**:
   ```powershell
   copy .env.example .env
   ```
2. **Start Services**:
   ```powershell
   docker-compose up -d --build
   ```

## 3. Accessing the Platform
Once the services are started (it may take 2-5 minutes for the database and backend to initialize on the first run):

- **Main Platform**: [http://localhost:80](http://localhost:80)
- **Backend API Docs**: [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
- **ML Service**: [http://localhost:8001/docs](http://localhost:8001/docs)

## 4. Default Credentials
Use these to log in for the first time:
- **Username**: `admin`
- **Password**: `admin123`

## 5. Monitoring & Stopping
- **To see logs**: `docker-compose logs -f`
- **To stop everything**: `docker-compose down`
