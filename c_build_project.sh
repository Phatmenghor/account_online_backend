#!/bin/bash

echo "👉 Cleaning project..."
mvn clean

echo "🔨 Compiling project..."
mvn compile

echo "📦 Installing project to local Maven repository..."
mvn install

echo "✅ Done."
