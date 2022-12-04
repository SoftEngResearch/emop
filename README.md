# eMOP
eMOP is a Maven plugin that realizes evolution-aware runtime verification through the following techniques:

1. Regression Property Selection (RPS)
2. Violation Message Suppression (VMS)
3. Regression Property Prioritization (RPP)

## Table of Contents

1. [Prerequisites](#Prerequisites)
2. [Installation](#Installation)
3. [Integration](#Integration)
4. [Usages](#Usages)
5. [Options](#Options)

## Prerequisites

### Java

Use Java 8 (Oracle JDK, Open JDK)

### STARTS Maven Plugin

Make sure you install the `impacted-both-ways` branch of [STARTS](https://github.com/TestingResearchIllinois/starts) by following its installation guide, or run the following:

```bash
bash scripts/install-starts.sh
```

### Javamop & Javamop Agent

> TODO:
>
> 1. Finish writing up Javamop & Javamop Agent
> 2. Requirements for Maven and Surefire
> 3. Requirements for OS?

## Installation

To install eMOP as a plugin in your Maven local repository, run

```bash
mvn install
```

## Integration

To integrate eMOP as a plugin into your Maven project, add the following segment to the plugins section under the build section in your `pom.xml`:

```xml
<project>
  ...
  <build>
    ...
    <plugins>
      ...
      <plugin>
      	<groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven−sureﬁre−plugin</artifactId>
        <version>2.20 or greater</version>
        <configuration>
        	<argLine>−javaagent:${JavaMOP jar}</argLine>
        </configuration>
      </plugin>
      <plugin>
        <artifactId>emop-maven-plugin</artifactId>
        <groupId>edu.cornell</groupId>
        <version>${latest eMOP version}</version>
      </plugin>
    </plugins>
  </build>
</project>
```

> TODO: Need to release versions for users

## Usages

Invoke various eMOP goals with the following commands:

1. `mvn emop:help`: list all goals
2. `mvn emop:impacted`: list impacted classes
3. `mvn emop:affected-specs`: list affected speciﬁcations
4. `mvn emop:rps`: run RPS
5. `mvn emop:vms`: run VMS
6. `mvn emop:rpp`: run RPP
7. `mvn emop:rps−vms`: run RPS+VMS
8. `mvn emop:rps−rpp`: run RPS+RPP
9. `mvn emop:clean`: delete all metadata
