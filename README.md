# DB-MIGRATION

## command

### apply
Table create if not exists and data prepare.

```
java -jar db-migration-1.0.0.jar -rootdir ./database --spring.config.location=file:./application.yml
```

### rebuild
All table create and data prepare.

```
java -jar db-migration-1.0.0.jar -mode rebuild -rootdir ./database --spring.config.location=file:./application.yml
```

### drop-all
All table drop.

```
java -jar db-migration-1.0.0.jar -mode dropall -rootdir ./database --spring.config.location=file:./application.yml
```

### data-all
Apply data only. (Not execute DDL).

```
java -jar db-migration-1.0.0.jar -mode dataall -rootdir ./database --spring.config.location=file:./application.yml
```
