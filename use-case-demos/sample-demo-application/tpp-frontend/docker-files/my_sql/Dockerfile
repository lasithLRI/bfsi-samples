FROM mysql:8.0

COPY abcde.sql /docker-entrypoint-initdb.d/01_dump.sql
COPY grants.sql /docker-entrypoint-initdb.d/02_grants.sql
