SSH key cache:
```
eval `ssh-agent`
ssh-add
```

Release:
```
mvn release:clean release:prepare -B
mvn release:perform
git push && git push --tags
```

Before run test project, remove jar from local store:
```
mvn build-helper:remove-project-artifact
```

Build and deploy site:
```
mvn clean verify site-deploy
```
