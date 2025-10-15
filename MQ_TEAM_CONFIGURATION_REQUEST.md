# IBM MQ Server Configuration Request for Spring Boot Application

## Document Purpose
This document outlines the required IBM MQ server-side configurations to enable a Spring Boot application to connect and send/receive messages without authentication issues.

---

## Issue Summary
Our Spring Boot application encounters the following error when connecting to IBM MQ:
```
MQRC_NOT_AUTHORIZED (2035) - The security authentication was not valid
```

This occurs even though:
- Connection credentials are not provided (as per your requirement)
- The application needs to connect using a server connection channel

---

## Required MQ Server Configuration

### 1. Server Connection Channel Configuration

**Required Action:** Configure the server connection channel with a valid MCAUSER

**Channel Name:** Your server connection channel (e.g., `DEV.APP.SVRCONN`)

**MQSC Command:**
```mqsc
ALTER CHANNEL('YOUR_CHANNEL_NAME') CHLTYPE(SVRCONN) MCAUSER('your_mq_admin_user') TRPTYPE(TCP)
```

**Explanation:**
- `MCAUSER` specifies which user ID the channel will adopt for connections
- This should be set to a user that has appropriate MQ permissions
- Common options:
  - `MCAUSER('MUSR_MQADMIN')` - uses the MQ admin user (for development/testing)
  - `MCAUSER('app_service_user')` - uses a dedicated application service account (recommended for production)
  - `MCAUSER('')` - blank means use the connecting user (requires client authentication)

**⚠️ Important:** The user specified in MCAUSER must:
- Exist as a valid user/service account in your environment
- Have appropriate MQ permissions (see section 3)

---

### 2. Refresh Security Cache

**Required Action:** Refresh the security cache to apply changes

**MQSC Command:**
```mqsc
REFRESH SECURITY(*)
```

**Explanation:**
- This ensures all security changes take effect immediately without restarting the queue manager

---

### 3. User Permissions (if not already configured)

**Required Action:** Grant necessary permissions to the user specified in MCAUSER

**MQSC Commands:**

For Queue Manager access:
```mqsc
* Grant connect and inquire permissions on queue manager
setmqaut -m <QUEUE_MANAGER_NAME> -t qmgr -p <username> +connect +inq
```

For Queue access:
```mqsc
* Grant permissions on specific queue
setmqaut -m <QUEUE_MANAGER_NAME> -n <QUEUE_NAME> -t queue -p <username> +put +get +browse +inq

* Or grant permissions on all queues (for development)
setmqaut -m <QUEUE_MANAGER_NAME> -n '**' -t queue -p <username> +put +get +browse +inq
```

**Note:** Replace:
- `<QUEUE_MANAGER_NAME>` with your queue manager name
- `<username>` with the user from MCAUSER
- `<QUEUE_NAME>` with specific queue names

---

### 4. Connection Authentication (Optional - if currently blocking connections)

**If CONNAUTH is currently enabled and causing issues:**

**MQSC Commands:**
```mqsc
* Temporarily disable connection authentication (for development/testing only)
ALTER QMGR CONNAUTH('')
REFRESH SECURITY TYPE(CONNAUTH)
```

**⚠️ Warning:** This completely disables connection authentication. Only use for troubleshooting or in secure development environments.

**For Production:** Keep CONNAUTH enabled and configure proper authentication instead.

---

### 5. Channel Authentication Records (CHLAUTH)

**Verify existing CHLAUTH rules:**
```mqsc
DISPLAY CHLAUTH('YOUR_CHANNEL_NAME')
```

**If needed, ensure the channel is not blocked:**
```mqsc
* Allow connections (remove blocks)
SET CHLAUTH('YOUR_CHANNEL_NAME') TYPE(BLOCKUSER) USERLIST(ALLOWANY)

* Refresh security
REFRESH SECURITY(*)
```

---

## Verification Steps

After making the configuration changes, please verify:

### 1. Display Channel Configuration
```mqsc
DISPLAY CHANNEL('YOUR_CHANNEL_NAME')
```

**Expected Output:**
- `CHLTYPE(SVRCONN)`
- `MCAUSER(your_specified_user)` - should show the user you configured

### 2. Check Channel Status
```mqsc
DISPLAY CHSTATUS('YOUR_CHANNEL_NAME') CURRENT
```

### 3. Review Error Logs
After our application attempts to connect, check:
- Queue Manager error logs: `<MQ_DATA_PATH>/qmgrs/<QM_NAME>/errors/AMQERR01.LOG`

Look for:
- ✅ Successful connections (no AMQ9557E or AMQ7026E errors)
- ❌ Authorization failures (if still occurring)

---

## Required Information from Your Team

Please provide the following details for our Spring Boot application configuration:

1. **Queue Manager Name:** `_________________`
2. **Server Connection Channel Name:** `_________________`
3. **Host/IP Address:** `_________________`
4. **Port Number:** `_________________`
5. **Queue Name(s) to use:** `_________________`
6. **MCAUSER value you configured:** `_________________`
7. **SSL/TLS Required?** ☐ Yes ☐ No
   - If Yes, please provide:
     - Cipher Spec: `_________________`
     - Certificate requirements: `_________________`

---

## Example Configuration (for your reference)

### Minimal Working Configuration:
```mqsc
* Define or alter the channel
DEFINE CHANNEL('YOUR_CHANNEL_NAME') CHLTYPE(SVRCONN) TRPTYPE(TCP) MCAUSER('MUSR_MQADMIN') REPLACE

* Allow all connections
SET CHLAUTH('YOUR_CHANNEL_NAME') TYPE(BLOCKUSER) USERLIST(ALLOWANY)

* Disable CONNAUTH (development/testing only)
ALTER QMGR CONNAUTH('')

* Refresh security
REFRESH SECURITY(*)

* Define a test queue (if needed)
DEFINE QLOCAL('YOUR.QUEUE.NAME') REPLACE

* Grant permissions
setmqaut -m YOUR_QM_NAME -t qmgr -p MUSR_MQADMIN +connect +inq +alladm
setmqaut -m YOUR_QM_NAME -n '**' -t queue -p MUSR_MQADMIN +allmqi

* Display configuration
DISPLAY CHANNEL('YOUR_CHANNEL_NAME')
DISPLAY CHLAUTH('YOUR_CHANNEL_NAME')
```

---

## Security Considerations

### For Development/Testing Environments:
- Setting `MCAUSER('MUSR_MQADMIN')` or disabling CONNAUTH is acceptable
- Focus on getting connectivity working

### For Production Environments:
Please implement:
1. **Dedicated Service Account:** Create a specific user for the application (not admin)
2. **Minimal Permissions:** Grant only necessary permissions (connect, specific queues)
3. **CONNAUTH Enabled:** Use proper authentication mechanism
4. **CHLAUTH Rules:** Configure specific address maps and user restrictions
5. **SSL/TLS:** Enable encrypted communication
6. **Audit Logging:** Enable security event logging

Example Production Configuration:
```mqsc
* Create dedicated channel with service account
ALTER CHANNEL('YOUR_CHANNEL_NAME') CHLTYPE(SVRCONN) MCAUSER('app_service_user') TRPTYPE(TCP)

* Restrict by IP address
SET CHLAUTH('YOUR_CHANNEL_NAME') TYPE(ADDRESSMAP) ADDRESS('your.app.ip.address') USERSRC(CHANNEL) MCAUSER('app_service_user')

* Grant minimal permissions
setmqaut -m YOUR_QM_NAME -t qmgr -p app_service_user +connect +inq
setmqaut -m YOUR_QM_NAME -n YOUR.SPECIFIC.QUEUE -t queue -p app_service_user +put +get +browse +inq
```

---

## Contact Information

**Application Team Contact:**
- Name: `_________________`
- Email: `_________________`
- Phone: `_________________`

**Expected Timeline:**
- Configuration Request Date: `_________________`
- Required By Date: `_________________`
- Testing Window: `_________________`

---

## Support and Troubleshooting

If issues persist after configuration:

1. **Check MQ Error Logs** for specific error codes
2. **Verify Permissions** using:
   ```bash
   dspmqaut -m <QM_NAME> -t qmgr -p <username>
   dspmqaut -m <QM_NAME> -n <QUEUE_NAME> -t queue -p <username>
   ```
3. **Test with MQ Sample Programs** (amqsputc/amqsgetc) to isolate application issues
4. **Review Channel Status** for connection attempts

---

## Acknowledgment

Please confirm receipt and expected completion date:

- ☐ Configuration changes reviewed and approved
- ☐ Changes scheduled for: `_________________`
- ☐ Testing coordination scheduled for: `_________________`

**MQ Team Contact:**
- Name: `_________________`
- Email: `_________________`
- Date: `_________________`

---

## Appendix: Common Error Codes

| Error Code | Reason | Solution |
|------------|--------|----------|
| 2035 | MQRC_NOT_AUTHORIZED | Set proper MCAUSER and permissions |
| 2059 | MQRC_Q_MGR_NOT_AVAILABLE | Check QM is running and network accessible |
| 2538 | MQRC_HOST_NOT_AVAILABLE | Verify hostname/IP and port |
| 2059 | MQRC_Q_MGR_NAME_ERROR | Verify queue manager name |
| 2161 | MQRC_CHANNEL_NOT_AVAILABLE | Check channel definition and listener |

---

**Document Version:** 1.0  
**Last Updated:** October 15, 2025  
**Prepared By:** Development Team

