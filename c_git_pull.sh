#!/bin/bash

# Get current date and time
CURRENT_TIME=$(date "+%Y-%m-%d %H:%M:%S")

# Get current branch name
BRANCH=$(git rev-parse --abbrev-ref HEAD)

# Fetch remote updates
git fetch

# Pull latest code for the current branch
git pull orgin $BRANCH

echo "✅ Auto pull completed from '$BRANCH' at $CURRENT_TIME"
