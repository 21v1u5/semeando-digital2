@echo off
REM Sobe PostgreSQL + backend + frontend via Docker Compose.
REM Pre-requisito: Docker Desktop instalado e em execucao.
if not exist .env copy .env.example .env
docker compose up --build
echo Acesse http://localhost:5173
