# spring-boot-rpm-maven-plugin
Maven plugin that builds an RPM with upstart service job

Intention
=========
When creating applications using Spring Boot you need connections to the underlying operating system:
* packaging
* service start/stop
* logging

This glue code is often the same between applications and because of this copied around and then slightly modified.
The Spring Boot RPM Maven plugin tries to give this stuff a home where it can be collaboratively maintained.

Features
========
Based on the Maven project it builds an RPM containing:
* the artifact: `/usr/share/${project.artifactId}/${artifact}`
* pre-install scriptlet that creates a dedicated service user for the application
* upstart service job that runs the artifact under the previously created service user using `java -server` at system start: `/etc/init/${project.artifactId}`
* CloudWatch Logs compatible logging configuration:
 * Logback: `/etc/${project.artifactId}/default-logback.xml`
 * CloudWatch Logs client: `/etc/awslogs.conf.d/${project.artifactId}.conf`

Usage
=====

    <plugin>
        <groupId>de.is24.aws</groupId>
        <artifactId>spring-boot-rpm-maven-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>package</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <requires>
                <!-- additional dependencies -->
                <require>aws-cli</require>
            </requires>
        </configuration>
    </plugin>

Prerequisites
=============
* Java >= 1.7.0 in your `$PATH`
* Maven >= 3.2.3 (did not test with earlier versions)

License
=======
The Spring Boot RPM Maven Plugin is licensed under [Apache License, Version 2.0](https://github.com/ImmobilienScout24/spring-boot-rpm-maven-plugin/blob/master/LICENSE.txt).
