# Docker Desktop Setup Guide

Follow these steps to ensure your Docker environment is ready to run the Medical ML Platform.

## 1. Start the Engine
*   **Open Docker Desktop** from your Start Menu.
*   **Check the bottom-left corner**: Look for the small whale icon. It must be **green** and say **"Engine Running"**. 
*   *Note: If it's orange ("Starting"), wait about 60 seconds. If it asks you to "Accept" a Service Agreement, click Accept.*

## 2. Handle the WSL 2 Prompt (If it appears)
On many Windows machines, Docker may show a popup saying **"WSL 2 installation is incomplete."** 
*   If you see this, **click the link in the popup**.
*   It will take you to a Microsoft page to download the Linux kernel update (usually `wsl_update_x64.msi`).
*   Install the update, then click **Restart** on the Docker popup.

## 3. Verify Settings
Ensure the backend engine is correctly configured:
1. Click the **Gear icon (Settings)** at the top-right.
2. Go to **General** → Ensure **"Use the WSL 2 based engine"** is checked.
3. Go to **Resources** → **WSL Integration** → Ensure your default distro (or "Ubuntu" if you use it) is toggled to **On**.

---

**Once the icon is green**, you are ready! Minimize Docker and run the launcher:
```powershell
.\launch.bat
```
