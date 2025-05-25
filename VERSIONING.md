# 🚢 smauel Versioning

The smauel monorepo uses a custom versioning scheme to allow us to version each module individually. The
maven-release-plugin does not support this and can only version the entire project.

To facilitate this, we make use of the versions-maven-plugin, and a set of custom properties. When a new version is
required, the property can be updated, and this will cascade to version both the module itself, plus any references to
that particular version of the module.

These custom properties are not intended to be updated manually (although they can be), but rather to
be updated as part of the CI/CD pipelines.

## Versioning Scheme Followed

```
<major>.<minor>.<hotfix>
```

- **major**: a breaking change
- **minor**: any other non-breaking change
- **hotfix**: a fix applied to a specific `<major>.<minor>` version

## Overview

In the main parent pom.xml we define a section of properties that lists the _target_ version for each of the modules:

```xml

<properties>
    ...
    <parent.version>1.0.0-SNAPSHOT</parent.version>
    <boms.version>1.0.0-SNAPSHOT</boms.version>
    <logging-bom.version>1.0.0-SNAPSHOT</logging-bom.version>
    <testing-bom.version>1.0.0-SNAPSHOT</testing-bom.version>
    <backend.version>1.0.0-SNAPSHOT</backend.version>
    <users-api.version>1.0.0-SNAPSHOT</users-api.version>
    ...
</properties>
```

In addition, each module defines another property for use in the versions-maven-plugin:

This property defined the _target_ version of that particular module, using the module versions defined above in the
parent pom.xml

```xml

<properties>
    ...
    <module.auto.version>${parent.version}</module.auto.version>
    ...
</properties>
```

This is all combined via the versions-maven-plugin configuration:

```xml

<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>versions-maven-plugin</artifactId>
    <version>${maven.versions.version}</version>
    <executions>
        <execution>
            <id>update-version</id>
            <goals>
                <goal>set</goal>
            </goals>
            <phase>generate-sources</phase>
            <configuration>
                <generateBackupPoms>false</generateBackupPoms>
                <artifactId>${project.artifactId}</artifactId>
                <newVersion>${module.auto.version}</newVersion>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Here we configure the versions-maven-plugin to **set** the version for a given artifact (module), to the
_module.auto.version_. This is bound to the generate-sources phase prior to compilation (so that the new build will run
with
the new versions).

This will run for every module, and during normal day-to-day development, the target and current states of versions will
match, so no updates will be made. But once a change to the target version state is made (via the versions defined in
the parent pom.xml), the versions-maven-plugin will trigger, and module versions and uses of that dependency will be
updated.

## Automation

Ideally, we want to configure our CI/CD pipelines such that a manual version update never needs to be made. The
following
section describes how the above versioning scheme works within our pipelines.

On any merge to the `main` branch, the `Version` GitHub action will trigger which performs a diff of the patchset to
determine which modules have been updated. Once the modules have been identified, the affected module versions are
updated to drop the `-SNAPSHOT` in the main pom.xml. A new build is triggered, and if the build passes, the updates are
committed.

A second update to the module versions is made to increment the version and re-append the `-SNAPSHOT`, ready for the
next round of development. This time, only the module version itself is updated, leaving any dependent modules on the
newly created release version.

### Example

A PR is merged with the following diff (assume here that there is some update being made in logging-bom and this is
being consumed in users-api):

```bash
backend/users-api/pom.xml |  3 +--
boms/logging-bom/pom.xml  |  3 +--
```

The current state of our main pom.xml module versions is:

```xml

<properties>
    ...
    <testing-bom.version>1.11.0-SNAPSHOT</testing-bom.version>
    <backend.version>1.1.0-SNAPSHOT</backend.version>
    <logging-bom.version>1.21.0-SNAPSHOT</logging-bom.version>
    <users-api.version>1.3.0-SNAPSHOT</users-api.version>
    ...
</properties>
```

When this PR is merged, the automation is triggered and the module versions are updated as such:

```xml

<properties>
    ...
    <testing-bom.version>1.11.0-SNAPSHOT</testing-bom.version>
    <backend.version>1.1.0-SNAPSHOT</backend.version>
    <logging-bom.version>1.21.0</logging-bom.version>
    <users-api.version>1.3.0</users-api.version>
    ...
</properties>
```

A `mvn -PversionRelease` is triggered, which will notice the changes in the target versions and make three updates:

1. The version of `logging-bom` will be updated from `1.21.0-SNAPSHOT` -> `1.21.0`
2. The version of the `logging-bom` dependency in `users-api` will also be updated to `1.21.0`
3. The version of `users-api` will be updated from `1.3.0-SNAPSHOT` -> `1.3.0`

The build passes, and the change is committed and released.

A second update will be made to the module versions in the main pom.xml, like so:

```xml

<properties>
    ...
    <testing-bom.version>1.11.0-SNAPSHOT</testing-bom.version>
    <backend.version>1.1.0-SNAPSHOT</backend.version>
    <logging-bom.version>1.22.0-SNAPSHOT</logging-bom.version>
    <users-api.version>1.4.0-SNAPSHOT</users-api.version>
    ...
</properties>
```

A `mvn -PversionNext` is triggered, which will notice the changes in the target versions and make two updates this
time:

1. The version of `logging-bom` will be updated from `1.21.0` -> `1.22.0-SNAPSHOT`
2. The version of `users-api` will be updated from `1.3.0` -> `1.4.0-SNAPSHOT`

| NOTE: On this update the consumed version of `logging-bom` in `users-api` is left at `1.21.0` as intended.

Again, the build passes, and the change is committed and released.

