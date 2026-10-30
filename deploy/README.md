# Deploy na VPS (Ubuntu) com Traefik + Cloudflare

Coloca o **Semeando Digital** no ar em `https://semeando-digital.21v1u5.com`, atrás do Traefik, **sem publicar nenhuma porta da aplicação** — só o Traefik escuta 80/443. TLS via **Cloudflare Origin Certificate** (modo *Full (strict)*).

```
Navegador ──HTTPS──> Cloudflare ──HTTPS──> Traefik (:443) ──> frontend (nginx :80)
                                                                      └─ /api ─> backend :8080 ─> PostgreSQL
```
Backend e PostgreSQL ficam só na rede interna — nunca expostos.

---

## 0. Pré-requisitos na VPS
```bash
# Docker + plugin compose
curl -fsSL https://get.docker.com | sh
# rede externa compartilhada do Traefik (uma vez só)
docker network create traefik-publico
# clonar o projeto
git clone https://github.com/21v1u5/semeando-digital2.git
cd semeando-digital2
```

## 1. Certificado de origem do Cloudflare
1. Cloudflare → **SSL/TLS → Origin Server → Create Certificate**.
2. Hosts do certificado: `21v1u5.com` **e** `*.21v1u5.com`. Formato PEM.
3. Salve os dois conteúdos na VPS:
   - o certificado em `deploy/traefik/certs/origin.pem`
   - a chave privada em `deploy/traefik/certs/origin.key`
4. Cloudflare → **SSL/TLS → Overview** → modo **Full (strict)**.

> Os CNAMEs `semeando-digital` e `traefik` já apontam para o host e estão *Proxied* (nuvem laranja) — está correto.

## 2. Subir o Traefik
```bash
cd deploy/traefik
cp .env.example .env
# gere o usuário/senha do painel:
docker run --rm httpd:alpine htpasswd -nbB admin 'SUA_SENHA_FORTE'
# cole o resultado em TRAEFIK_DASHBOARD_AUTH, DOBRANDO cada "$" -> "$$"
# (ex.: admin:$2y$05$abc...  vira  admin:$$2y$$05$$abc...)
nano .env
docker compose up -d
```
Confira o painel em `https://traefik.21v1u5.com` (usuário `admin`).
Não quer o painel exposto? Apague o bloco de `labels` do `traefik` no compose.

## 3. Subir o Semeando
```bash
cd ~/semeando-digital2          # raiz do projeto
cp .env.example .env
# defina uma senha forte do banco:
nano .env                       # DB_PASSWORD=...
docker compose -f docker-compose.prod.yml up -d --build
```

Pronto: **https://semeando-digital.21v1u5.com** (gestor `gestor@exemplo.com` / `123456` — **troque as senhas de demonstração**).

---

## Operação
```bash
# logs
docker compose -f docker-compose.prod.yml logs -f backend
# atualizar após um git pull
git pull && docker compose -f docker-compose.prod.yml up -d --build
# parar (dados permanecem no volume pgdata)
docker compose -f docker-compose.prod.yml down
# zerar o banco (apaga tudo)
docker compose -f docker-compose.prod.yml down -v
```

## Observações
- **`docker-compose.yml` (raiz)** continua sendo o de **desenvolvimento local** (publica 8080/5173). Em produção use **sempre** o `docker-compose.prod.yml`.
- Backup do banco:
  `docker compose -f docker-compose.prod.yml exec db pg_dump -U semeando semeando > backup.sql`
- Troque a senha padrão `123456` dos usuários de demonstração assim que entrar.
