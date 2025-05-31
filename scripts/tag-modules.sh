#!/bin/bash
set -e

MODULES=$1

tag_module() {
  VERSION_PROPERTY="${module}.auto.version"
  NEW_VERSION=$(grep "<$VERSION_PROPERTY>" pom.xml | sed -E "s/^.*<$VERSION_PROPERTY>(.*)<\/$VERSION_PROPERTY>.*$/\1/")
  TAG_NAME="${module}v${NEW_VERSION}"

  echo "Creating tag $TAG_NAME"
  git tag -f "$TAG_NAME"
}

# Tag all affected modules
for module in  $MODULES; do
  tag_module "$module"
done

git push --tags