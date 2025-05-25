#!/bin/bash
set -e

# Find the most recent "last-release" tag
LAST_RELEASE_TAG=$(git describe --tags --abbrev=0 --match="*v[0-9]*.[0-9]*.[0-9]*" 2>/dev/null || echo "")

# Default to the first commit if no previous version tag exists
if [[ -z "$LAST_RELEASE_TAG" ]]; then
  echo "No previous version tag found. Defaulting to first commit." >&2
  LAST_RELEASE_TAG=$(git rev-list --max-parents=0 HEAD)
fi

echo "Comparing HEAD to $LAST_RELEASE_TAG" >&2

# Get a list of all changed files since the last release
CHANGED_FILES=$(git diff --name-only "$LAST_RELEASE_TAG"..HEAD)

# Prepare a list of affected modules
AFFECTED_MODULES=()

# Function to find the nearest parent directory containing a pom.xml
find_nearest_pom_dir() {
  local dir=$(dirname "$1")
  while [[ "$dir" != "/" && "$dir" != "." ]]; do
    if [[ -f "$dir/pom.xml" ]]; then
      basename "$dir"
      return
    fi
    dir=$(dirname "$dir")
  done
  echo "" # No parent directory with pom.xml found
}

# Root pom.xml changes require the "parent" module version to be updated
ROOT_POM_CHANGED=false

# Iterate over the changed files and determine affected modules
for file in $CHANGED_FILES; do
  if [[ "$file" == "pom.xml" ]]; then
    ROOT_POM_CHANGED=true
    continue
  fi

  MODULE_DIR=$(find_nearest_pom_dir "$file")

  if [[ -n "$MODULE_DIR" ]]; then
    AFFECTED_MODULES+=("$MODULE_DIR")
  fi
done

if [[ "$ROOT_POM_CHANGED" == "true" ]]; then
  AFFECTED_MODULES+=("parent")
fi

# Remove duplicates and print to stdout
printf "%s\n" "${AFFECTED_MODULES[@]}" | sort -u