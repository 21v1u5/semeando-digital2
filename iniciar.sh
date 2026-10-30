#!/usr/bin/env bash
# Sobe PostgreSQL + backend + frontend via Docker Compose.
# Pre-requisito: Docker instalado e em execucao. Ctrl+C encerra tudo.
cd "$(dirname "$0")"
[ -f .env ] || cp .env.example .env
docker compose up --build
