# Configuration Summary - Local vs Remote IBM MQ

## Overview
This document explains what worked locally and what needs to be configured on the external MQ team's server.

---

## What We Configured LOCALLY (Your Machine)

### 1. MQ Server Configuration (Using runmqsc QM2)
```mqsc
ALTER CHANNEL('DEV2.APP.SVRCONN') CHLTYPE(SVRCONN) MCAUSER('MUSR_MQADMIN')
REFRESH SECURITY(*)
```

**This was the KEY FIX** - Setting MCAUSER to a valid user with permissions.

### 2. Windows Permissions
```cmd
net localgroup mqm nidilshan /add
```
Added your Windows user to the `mqm` group (for local testing only).

### 3. Application Configuration (Spring Boot)
**File:** `src/main/resources/application.properties`
```properties
ibm.mq.queueManager=QM2
ibm.mq.channel=DEV2.APP.SVRCONN
ibm.mq.connName=localhost(1414)
```

**File:** `src/main/java/com/example/demo/MqConfig.java`
```java
// Basic connection factory configuration
// No special authentication settings needed once MQ is properly configured
```

---

## What the EXTERNAL MQ TEAM Needs to Configure

### CRITICAL: Channel MCAUSER Configuration

**This is the ONLY thing that matters:**

```mqsc
ALTER CHANNEL('their_channel_name') CHLTYPE(SVRCONN) MCAUSER('valid_user_name')
REFRESH SECURITY(*)
```

### Why This Matters

1. **Without MCAUSER set properly:**
   - MQ tries to use the connecting user's credentials
   - If the user doesn't exist in MQ → Error 2035
   
2. **With MCAUSER set correctly:**
   - All connections through this channel adopt the MCAUSER identity
   - No need to send credentials from the client
   - Matches how JMSToolBox was able to connect without credentials

### The User Permissions

The user specified in MCAUSER must have permissions:

```bash
# Queue Manager access
setmqaut -m QM_NAME -t qmgr -p username +connect +inq

# Queue access
setmqaut -m QM_NAME -n QUEUE_NAME -t queue -p username +put +get +browse +inq
```

---

## What Your Application Needs

### Minimal Configuration (application.properties)
```properties
ibm.mq.queueManager=THEIR_QM_NAME
ibm.mq.channel=THEIR_CHANNEL_NAME
ibm.mq.connName=their.host.com(1414)
```

### No Authentication Needed
- ❌ No username/password
- ❌ No SSL certificates (unless they require SSL)
- ❌ No special authentication properties

**Why?** Because the channel's MCAUSER handles the identity.

---

## Testing Checklist

### Before Contacting MQ Team:
- ✅ Application works with local MQ
- ✅ Send API returns "OK"
- ✅ Receive API returns messages
- ✅ Configuration is minimal

### After MQ Team Makes Changes:
1. Update `application.properties` with their connection details
2. Run application: `java -jar target/demo-0.0.1-SNAPSHOT.jar`
3. Test send: `curl http://localhost:8080/send`
4. Test receive: `curl http://localhost:8080/recv`

### If It Doesn't Work:
Ask MQ team to check:
1. Is MCAUSER set on the channel?
   ```mqsc
   DISPLAY CHANNEL('channel_name')
   ```
2. Does the MCAUSER user exist?
3. Does the user have permissions?
   ```bash
   dspmqaut -m QM_NAME -t qmgr -p username
   dspmqaut -m QM_NAME -n QUEUE_NAME -t queue -p username
   ```
4. Check MQ error logs for specific error messages

---

## Key Takeaway

**The 2035 error is 100% an MQ server-side configuration issue, not an application issue.**

The fix is simple:
1. Set MCAUSER on the channel to a valid user
2. Ensure that user has proper permissions
3. Refresh security

That's it. No complex application configuration needed.

---

## Documents for MQ Team

Send these documents to the external MQ team:

1. **MQ_TEAM_CONFIGURATION_REQUEST.md** - Complete detailed guide
2. **MQ_TEAM_QUICK_REQUEST.md** - Quick reference (1 page)
3. **EMAIL_TO_MQ_TEAM.md** - Email template

All documents explain the SAME requirement: **Set MCAUSER properly**

---

## Comparison: Local vs Remote

| Aspect | Local (Working) | Remote (Needed) |
|--------|----------------|-----------------|
| Channel | DEV2.APP.SVRCONN | (Their channel name) |
| MCAUSER | MUSR_MQADMIN | (Their valid user) |
| QM Name | QM2 | (Their QM name) |
| Host | localhost | (Their hostname) |
| Port | 1414 | (Their port) |
| User Permissions | mqm group member | setmqaut commands |

The configuration is **identical**, just with different names/values.

---

## Additional Notes

### If They Ask About Security
- For **development**: MCAUSER with admin user is fine
- For **production**: They should use a dedicated service account with minimal permissions

### If They Ask About SSL/TLS
- If they require SSL, they'll need to provide:
  - Cipher spec
  - Certificate files
  - Additional configuration instructions

### If They Say "It Should Work Without MCAUSER"
- It only works without MCAUSER if:
  - CONNAUTH is configured to accept your credentials
  - You provide valid username/password in the application
  - Your Windows user exists in their MQ system
- Since you're not providing credentials, MCAUSER is **required**

---

**Bottom Line:** The external MQ team needs to configure MCAUSER on their channel. That's the entire solution.

