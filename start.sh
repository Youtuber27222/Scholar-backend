#!/bin/sh
set -e

echo "Building with Maven wrapper..."
./mvnw -DskipTests package

echo "Starting app..."
java -jar target/scholarmatch-api-0.1.0.jar
