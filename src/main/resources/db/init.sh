#!/bin/sh
# Creeaza baza sala_fitness (daca lipseste) si ruleaza scriptul cu tabele, view-uri, triggere si date initiale.
# Utilizare:  ./src/main/resources/db/init.sh [port]        (implicit 5432; parola se cere de psql)
# Variabile:  DB_USER (implicit postgres), PSQL (implicit psql din PostgreSQL 16)
set -e

PSQL="${PSQL:-/Library/PostgreSQL/16/bin/psql}"
PORT="${1:-5432}"
USER="${DB_USER:-postgres}"
DIR="$(cd "$(dirname "$0")" && pwd)"

echo "SELECT 'CREATE DATABASE sala_fitness WITH ENCODING ''UTF8'' TEMPLATE template0'
      WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'sala_fitness') \gexec" \
    | "$PSQL" -h localhost -p "$PORT" -U "$USER" -d postgres -v ON_ERROR_STOP=1

"$PSQL" -h localhost -p "$PORT" -U "$USER" -d sala_fitness -v ON_ERROR_STOP=1 -q -f "$DIR/01_sala_fitness.sql"
echo "Baza sala_fitness este gata (port $PORT)."
