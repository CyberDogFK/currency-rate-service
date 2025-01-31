#!/usr/bin/env bash
set -x
set -eo pipefail

./mvnw clean package

docker-compose up
