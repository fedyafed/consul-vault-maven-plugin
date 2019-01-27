# Consul-Vault Maven plugin

[![Licence](https://img.shields.io/github/license/fedyafed/consul-vault-maven-plugin.svg)](https://github.com/fedyafed/consul-vault-maven-plugin/blob/master/LICENSE)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.fedyafed/consul-vault-maven-plugin/badge.svg?style=flat)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.fedyafed%22%20AND%20a%3A%22consul-vault-maven-plugin%22)
[![GitHub Release Date](https://img.shields.io/github/release-date/fedyafed/consul-vault-maven-plugin.svg)](https://github.com/fedyafed/consul-vault-maven-plugin/releases)
[![Build Status](https://travis-ci.org/fedyafed/consul-vault-maven-plugin.svg?branch=master)](https://travis-ci.org/fedyafed/consul-vault-maven-plugin)

Get properties for Maven project from [Consul](https://www.consul.io/) K/V store and 
[Vault](https://www.vaultproject.io/) secret store.

[Changelog](CHANGELOG.md)

## Usage:

Read from Consul KV-store all parameters from folders `config/common` and
`config/custom`. In case any name duplicates, the last one wins.

```xml
<plugin>
    <groupId>com.github.fedyafed</groupId>
    <artifactId>consul-vault-maven-plugin</artifactId>
    <version>0.2.0</version>
    <configuration>
        <prefixes>
            <prefix>config/common</prefix>
            <prefix>config/custom</prefix>
        </prefixes>
        <services>
            <service>vault</service>
            <service>custom-service</service>
        </services>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>read-consul</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```


## Building from Source
Consul-Vault Maven plugin can be built with the included
[maven wrapper](https://github.com/takari/maven-wrapper). You also need JDK 1.8.

```bash
	$ ./mvnw clean install
```

If you want to build with the regular `mvn` command, you will need
[Maven v3.2 or above](https://maven.apache.org/run-maven/index.html).


Also see [CONTRIBUTING.md](.github/CONTRIBUTING.md) if you wish to submit pull requests.

