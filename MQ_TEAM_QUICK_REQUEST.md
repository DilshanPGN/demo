# Quick MQ Configuration Request

## Problem
Spring Boot application getting **MQRC_NOT_AUTHORIZED (2035)** error when connecting to IBM MQ.

---

## Required MQ Server Changes

### 1. Set MCAUSER on the Channel (CRITICAL)

```mqsc
ALTER CHANNEL('your_channel_name') CHLTYPE(SVRCONN) MCAUSER('valid_mq_user')
REFRESH SECURITY(*)
```

**Options for MCAUSER:**
- **Development:** Use `MUSR_MQADMIN` or equivalent admin user
- **Production:** Use dedicated service account like `app_service_user`

### 2. Grant Permissions to the User

```bash
# Queue Manager permissions
setmqaut -m QM_NAME -t qmgr -p username +connect +inq

# Queue permissions
setmqaut -m QM_NAME -n QUEUE_NAME -t queue -p username +put +get +browse +inq
```

### 3. Verify Configuration

```mqsc
DISPLAY CHANNEL('your_channel_name')
```
Should show: `MCAUSER(your_configured_user)`

---

## What We Need from You

1. **Queue Manager Name:** _____________
2. **Channel Name:** _____________
3. **Host/Port:** _____________
4. **Queue Names:** _____________
5. **What MCAUSER value did you set?** _____________

---

## Key Point

The **MCAUSER** in the channel definition must be:
✅ A valid user that exists in your environment  
✅ A user with proper MQ permissions  
✅ NOT blank (unless you want to use client-provided credentials)

---

## Example Complete Working Configuration

```mqsc
# For Development/Testing
ALTER CHANNEL('DEV.APP.SVRCONN') CHLTYPE(SVRCONN) MCAUSER('MUSR_MQADMIN')
SET CHLAUTH('DEV.APP.SVRCONN') TYPE(BLOCKUSER) USERLIST(ALLOWANY)
ALTER QMGR CONNAUTH('')
REFRESH SECURITY(*)
```

---

**This is the minimum configuration needed to resolve the 2035 error.**

For detailed instructions and production considerations, see: `MQ_TEAM_CONFIGURATION_REQUEST.md`

