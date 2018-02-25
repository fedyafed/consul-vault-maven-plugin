Update pom version:
```
mvn versions:set -DnewVersion=1.0.1-SNAPSHOT
mvn versions:commit
```

Update dependencies:
```
mvn versions:display-dependency-updates
mvn versions:display-property-updates
mvn versions:display-plugin-updates
```
