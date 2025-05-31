#!/bin/bash

# Input: list of artifactIds (like users-api, collections)
MODULES=$1
declare -A name_to_path

# Map artifactId -> relative path
while IFS= read -r -d '' pom; do
  dir=$(dirname "$pom")
  artifact=$(mvn -f "$pom" help:evaluate -Dexpression=project.artifactId -q -DforceStdout 2>/dev/null)
  [[ -n "$artifact" ]] && name_to_path["$artifact"]="$dir"
done < <(find . -name pom.xml -print0)

# Match input names to paths
resolved_paths=()
for name in $MODULES; do
  echo "name: $name"
  if [[ -n "${name_to_path[$name]}" ]]; then
    # Strip leading ./ for clean output
    path="${name_to_path[$name]}"
    resolved_paths+=("${path#./}")
  else
    echo "Warning: module '$name' not found in reactor" >&2
  fi
done

# Output as --pl ready string
if [[ ${#resolved_paths[@]} -gt 0 ]]; then
  IFS=,; echo "${resolved_paths[*]}"
else
  echo "Error: no valid modules resolved" >&2
  exit 1
fi
