# Hazard-Project
Smart Hazard Risk Prediction and Relocation System (SIH 2026 — SIH26191)

## 🚀 Quick Start (1-Command Run)

Run this single command from the project root:

```bash
./start.sh
```
*(or `npm start`)*

This automated script will:
1. Ensure PostgreSQL is active.
2. Start the Spring Boot backend on **port 8080**.
3. Start the Frontend web server on **port 3000**.
4. Automatically open your browser to **http://localhost:3000**.
5. Stop all services cleanly when you press `Ctrl+C`.

---

## 🌐 Endpoints & Ports

- **Web Dashboard:** [http://localhost:3000](http://localhost:3000)
- **Backend API:** [http://localhost:8080](http://localhost:8080)
- **Swagger / OpenAPI Docs:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Health Check:** [http://localhost:8080/api/v1/hazards/health](http://localhost:8080/api/v1/hazards/health)

---

## 🧪 Testing

```bash
# Run Frontend Test Suites
npm test
```
