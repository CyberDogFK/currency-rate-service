#!/usr/bin/env bash
set -x
set -eo pipefail

if ! [ -x "$(command -v psql)" ]; then
  echo >&2 "Error: psql is not installed."
  exit 1
fi

export $(cat .env-local | xargs)

echo "${DB_USER}"

./mvnw clean package

./mvnw spring-boot:run
