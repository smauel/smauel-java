#!/bin/bash
set -e

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <mode> <modules>"
  echo "Modes: 'release' or 'snapshot'"
  exit 1
fi

MODE=$1
MODULES=$2

# Update version in the main pom.xml
update_version() {
  module="$1"
  mode="$2"

  VERSION_PROPERTY="${module}.auto.version"
  CURRENT_VERSION=$(grep "<$VERSION_PROPERTY>" pom.xml | sed -E "s/^.*<$VERSION_PROPERTY>(.*)<\/$VERSION_PROPERTY>.*$/\1/")

  # For release: remove -SNAPSHOT
  if [[ "$mode" == "release" ]]; then
    NEW_VERSION="${CURRENT_VERSION/-SNAPSHOT/}"
  fi

  # For snapshot: increment version and re-append -SNAPSHOT
  if [[ "$mode" == "snapshot" ]]; then
    BASE_VERSION="${CURRENT_VERSION/-SNAPSHOT/}"
    MAJOR=$(echo "$BASE_VERSION" | cut -d. -f1)
    MINOR=$(echo "$BASE_VERSION" | cut -d. -f2)
    PATCH=$(echo "$BASE_VERSION" | cut -d. -f3)
    NEW_VERSION="$MAJOR.$((MINOR + 1)).$PATCH-SNAPSHOT"
  fi

  # Replace in the main pom.xml
  sed -i "s|<$VERSION_PROPERTY>$CURRENT_VERSION</$VERSION_PROPERTY>|<$VERSION_PROPERTY>$NEW_VERSION</$VERSION_PROPERTY>|" pom.xml
  echo "Updated $module: $CURRENT_VERSION -> $NEW_VERSION" >&2
}

# Process all affected modules
for module in $MODULES; do
  update_version "$module" "$MODE"
done