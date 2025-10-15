# IBM MQ Server Configuration for No Authentication

If you're still getting **MQRC_NOT_AUTHORIZED (2035)** error, you need to configure the IBM MQ server to allow connections without authentication.

## Option 1: Using IBM MQ Commands (Recommended)

Connect to your IBM MQ server and run these commands:

```bash
# Connect to the queue manager
runmqsc QM1

# Disable channel authentication for DEV.APP.SVRCONN
SET CHLAUTH('DEV.APP.SVRCONN') TYPE(ADDRESSMAP) ADDRESS('*') USERSRC(CHANNEL) ACTION(REPLACE)

# Disable connection authentication (allow without credentials)
ALTER AUTHINFO(SYSTEM.DEFAULT.AUTHINFO.IDPWOS) AUTHTYPE(IDPWOS) CHCKCLNT(OPTIONAL)
REFRESH SECURITY TYPE(CONNAUTH)

# Set channel MCAUSER to blank (runs under Queue Manager authority)
ALTER CHANNEL('DEV.APP.SVRCONN') CHLTYPE(SVRCONN) MCAUSER(' ')

# Grant all users access to Queue Manager
SET AUTHREC OBJTYPE(QMGR) GROUP('*') AUTHADD(CONNECT,INQ,DSP)

# Grant all users access to DEV queues
SET AUTHREC PROFILE('DEV.**') OBJTYPE(QUEUE) GROUP('*') AUTHADD(ALLMQI)

# Exit
END
```

## Option 2: Using Docker IBM MQ (Easiest)

If you're using Docker, run IBM MQ Developer edition with authentication disabled:

```bash
docker run --name ibm-mq ^
  --env LICENSE=accept ^
  --env MQ_QMGR_NAME=QM1 ^
  --env MQ_APP_PASSWORD=passw0rd ^
  --env MQ_ADMIN_PASSWORD=passw0rd ^
  --publish 1414:1414 ^
  --publish 9443:9443 ^
  --detach ^
  icr.io/ibm-messaging/mq:latest
```

Then run the commands from Option 1 inside the container:

```bash
docker exec -it ibm-mq runmqsc QM1
# Then run the SET CHLAUTH commands from above
```

## Option 3: Quick Docker Setup with No Auth

```bash
docker run --name ibm-mq ^
  --env LICENSE=accept ^
  --env MQ_QMGR_NAME=QM1 ^
  --env MQ_DISABLE_AUTH=yes ^
  --publish 1414:1414 ^
  --publish 9443:9443 ^
  --detach ^
  icr.io/ibm-messaging/mq:latest
```

## Option 4: Use Valid Credentials Instead

If you prefer to keep authentication enabled, add credentials to your `application.yml`:

```yaml
ibm:
  mq:
    queue-manager: QM1
    channel: DEV.APP.SVRCONN
    conn-name: localhost(1414)
    user: app        # or admin
    password: passw0rd
```

And update `MQConfig.java`:

```java
@Value("${ibm.mq.user}")
private String user;

@Value("${ibm.mq.password}")
private String password;

// In connectionFactory() method:
mqConnectionFactory.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
mqConnectionFactory.setStringProperty(WMQConstants.USERID, user);
mqConnectionFactory.setStringProperty(WMQConstants.PASSWORD, password);
```

## Verify Channel Configuration

Check your channel configuration:

```bash
runmqsc QM1
DISPLAY CHANNEL('DEV.APP.SVRCONN')
DISPLAY CHLAUTH('DEV.APP.SVRCONN')
END
```

## Common Issues

1. **MCAUSER is not set**: The channel needs MCAUSER set to a valid user or 'nobody'
2. **CHLAUTH rules blocking**: Channel authentication rules might be blocking your connection
3. **CONNAUTH enabled**: Connection authentication is enabled and requires credentials

## After Making Changes

After making any changes to IBM MQ configuration:

1. Refresh security: `REFRESH SECURITY TYPE(CONNAUTH)`
2. Restart your Spring Boot application
3. Check IBM MQ error logs if issues persist

## Testing Connection

You can test the connection using IBM MQ sample programs:

```bash
# Windows
"C:\Program Files\IBM\MQ\Tools\Samples\bin\amqsputc.exe" DEV.QUEUE.1 QM1

# Linux/Mac
/opt/mqm/samp/bin/amqsputc DEV.QUEUE.1 QM1
```

If this works without authentication, your Spring Boot application should also work.

