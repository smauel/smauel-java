#!/bin/sh

# Exit immediately if a command exits with a non-zero status
set -e

# Check if the collection directory exists
COLLECTION_DIR="/app/tests/$COLLECTION"
if [ ! -d "$COLLECTION_DIR" ]; then
    echo "Error: Collection directory '$COLLECTION_DIR' does not exist."
    exit 1
fi

cd "$COLLECTION_DIR"

bru run --env "$ENV" --reporter-json /app/tests/results/results.json --reporter-html /app/tests/results/results.html

TEST_EXIT_CODE=$?

if [ $TEST_EXIT_CODE -ne 0 ]; then
  echo "Integration tests failed."
else
  echo "Integration tests passed."
fi

exit $TEST_EXIT_CODE
