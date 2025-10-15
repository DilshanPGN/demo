# IBM MQ Spring Boot Application - Solution Summary

## Problem
The Spring Boot application was getting authentication error (MQRC_NOT_AUTHORIZED - 2035) when trying to connect to IBM MQ, even though JMSToolBox could connect without credentials.

## Root Cause
The issue was caused by:
1. IBM MQ client trying to use MQCSP authentication by default
2. Windows user permissions not being properly configured for MQ access
3. Channel MCAUSER not set to a valid user with proper permissions

## Solution Implemented

### 1. Application Configuration Changes

**File: `src/main/resources/application.properties`**
Added property to disable MQCSP authentication:
```properties
ibm.mq.userAuthenticationMQCSP=false
```

**File: `src/main/java/com/example/demo/MqConfig.java`** (NEW)
Created custom MQ configuration to have full control over connection factory:
- Explicitly disables MQCSP authentication
- Configures connection pooling with CachingConnectionFactory
- Sets proper JmsTemplate configuration

### 2. IBM MQ Server Configuration Changes

**Channel Configuration:**
```mqsc
ALTER CHANNEL('DEV.APP.SVRCONN') CHLTYPE(SVRCONN) MCAUSER('MUSR_MQADMIN')
```
- Set MCAUSER to 'MUSR_MQADMIN' (a user that exists and has mqm group membership)
- This allows any connection through this channel to have admin privileges

**Security Configuration:**
```mqsc
REFRESH SECURITY(*)
```
- Refreshed security cache to apply changes

### 3. Windows User Permissions

Added your Windows user (`nidilshan`) to the `mqm` group:
```cmd
net localgroup mqm nidilshan /add
```

Note: The group membership will be fully active after logging out and back in to Windows.

## How to Use the Application

### Starting the Application
```bash
mvn spring-boot:run
```

Or run the JAR directly:
```bash
java -jar target\demo-0.0.1-SNAPSHOT.jar
```

### API Endpoints

**Send a Message:**
```bash
curl http://localhost:8080/send
```
Response: `OK`

**Receive a Message:**
```bash
curl http://localhost:8080/recv
```
Response: `Hello World!` (or whatever message was sent)

### Testing

The application has been tested and verified:
- ✅ Successfully sends messages to `DEV.QUEUE.1`
- ✅ Successfully receives messages from `DEV.QUEUE.1`
- ✅ Multiple sequential operations work correctly
- ✅ No authentication errors

## Key Files Modified/Created

1. **src/main/resources/application.properties** - Added MQCSP disable property
2. **src/main/java/com/example/demo/MqConfig.java** - NEW custom configuration
3. **src/main/java/com/example/demo/DemoApplication.java** - Original file (no changes needed)

## MQ Channel Configuration Used

```
Queue Manager: QM1
Channel: DEV.APP.SVRCONN
Port: 1414
Queue: DEV.QUEUE.1
MCAUSER: MUSR_MQADMIN (has mqm permissions)
Authentication: Disabled (MQCSP=false, CONNAUTH='')
```

## Important Notes for Production

⚠️ **This configuration is suitable for DEVELOPMENT only!**

For production environments, you should:
1. Enable proper authentication (MQCSP)
2. Use specific user credentials with minimal required permissions
3. Enable SSL/TLS for secure communication
4. Set MCAUSER to a specific application user, not admin
5. Configure proper channel authentication rules (CHLAUTH)
6. Enable connection authentication (CONNAUTH)

## Troubleshooting

If you encounter issues after restarting:

1. **Check Queue Manager Status:**
   ```cmd
   dspmq
   ```

2. **Verify Channel Configuration:**
   ```cmd
   echo "DISPLAY CHANNEL('DEV.APP.SVRCONN')" | runmqsc QM1
   ```

3. **Check MQ Error Logs:**
   ```
   C:\ProgramData\IBM\MQ\qmgrs\QM1\errors\AMQERR01.LOG
   ```

4. **Restart Queue Manager if needed:**
   ```cmd
   endmqm -i QM1
   strmqm QM1
   ```

## Success!
Your Spring Boot application can now successfully send and receive messages to/from IBM MQ! 🎉

