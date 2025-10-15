# Email Template for MQ Team

---

**Subject:** IBM MQ Configuration Request - MQRC_NOT_AUTHORIZED (2035) Issue

---

Hi [MQ Team],

We are developing a Spring Boot application that needs to connect to your IBM MQ server, but we're encountering an authorization error.

## Issue
Our application is getting the following error when attempting to connect:
```
MQRC_NOT_AUTHORIZED (2035) - The security authentication was not valid
```

## Root Cause
Based on our investigation, the server connection channel requires a valid `MCAUSER` configuration.

## Required Configuration Changes

Could you please configure the following on your MQ server:

### 1. Set MCAUSER on the Channel
```mqsc
ALTER CHANNEL('channel_name') CHLTYPE(SVRCONN) MCAUSER('valid_mq_user')
REFRESH SECURITY(*)
```

**Note:** The `MCAUSER` should be set to:
- An existing user with MQ permissions (e.g., `MUSR_MQADMIN` for dev/test)
- Or a dedicated service account for production

### 2. Grant Permissions
```bash
setmqaut -m QM_NAME -t qmgr -p username +connect +inq
setmqaut -m QM_NAME -n QUEUE_NAME -t queue -p username +put +get +browse +inq
```

## Information We Need

Please provide the following connection details:
1. **Queue Manager Name:** 
2. **Channel Name:** 
3. **Host/IP Address:** 
4. **Port Number:** 
5. **Queue Name(s):** 
6. **MCAUSER value you configured:** 
7. **SSL/TLS Required?** (Yes/No)

## Testing

Once configured, we can test immediately. Our application simply needs to send and receive messages from the specified queue.

## Documentation

For complete details and production security considerations, please see the attached document: `MQ_TEAM_CONFIGURATION_REQUEST.md`

## Timeline

Could you please let us know:
- When these changes can be implemented?
- When we can schedule a testing session?

Thank you for your assistance!

Best regards,  
[Your Name]  
[Your Contact Information]

---

**Attachments:**
- MQ_TEAM_CONFIGURATION_REQUEST.md (detailed configuration guide)
- MQ_TEAM_QUICK_REQUEST.md (quick reference)

