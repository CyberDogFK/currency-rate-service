#!/usr/bin/env bash
set -x
set -eo pipefail

if ! [ -x "$(command -v psql)" ]; then
  echo >&2 "Error: psql is not installed."
  exit 1
fi

export $(cat .env-local | xargs)

echo "${DB_USER}"

./mvnw liquibase:update \
-Dliquibase.url="jdbc:postgresql://${DB_TEST_URL}" \
-Dliquibase.username=${DB_USER} \
-Dliquibase.password=${DB_PASSWORD} \
-Dliquibase.changeLogFile="/db/changelog/db.changelog-master.yaml"

./mvnw liquibase:dropAll \
-Dliquibase.url="jdbc:postgresql://${DB_TEST_URL}" \
-Dliquibase.username=${DB_USER} \
-Dliquibase.password=${DB_PASSWORD} \
-Dliquibase.changeLogFile="/db/changelog/db.changelog-master.yaml"

./mvnw clean package -Dmaven.test.skip=false
