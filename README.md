# Consul-Vault Maven plugin

[![Build Status](https://travis-ci.org/fedyafed/consul-vault-maven-plugin.svg?branch=master)](https://travis-ci.org/fedyafed/consul-vault-maven-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.fedyafed/consul-vault-maven-plugin/badge.svg?style=flat)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.fedyafed%22%20AND%20a%3A%22consul-vault-maven-plugin%22)

Get properties for Maven project from [Consul](https://www.consul.io/) K/V store and 
[Vault](https://www.vaultproject.io/) secret store.

[Changelog](CHANGELOG.md)

## Building from Source
Consul-Vault Maven plugin can be built with the included
[maven wrapper](https://github.com/takari/maven-wrapper). You also need JDK 1.8.

```bash
	$ ./mvnw clean install
```

If you want to build with the regular `mvn` command, you will need
[Maven v3.5.2 or above](https://maven.apache.org/run-maven/index.html).


Also see [CONTRIBUTING.md](.github/CONTRIBUTING.md) if you wish to submit pull requests.

