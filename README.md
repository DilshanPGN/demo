# IBM MQ Consumer - Spring Boot Application

This Spring Boot application consumes messages from IBM MQ and prints them to the console.

## Prerequisites

- Java 21
- Maven 3.x
- IBM MQ Server (running locally or accessible)
- IBM MQ Queue Manager configured with the following details

## IBM MQ Configuration

The application is configured to connect to IBM MQ with the following settings:

- **Host**: localhost
- **Port**: 1414
- **Queue Manager**: QM1
- **Channel**: DEV.APP.SVRCONN
- **Queue**: DEV.QUEUE.1
- **User**: app (default, update in application.yml if needed)
- **Password**: passw0rd (default, update in application.yml if needed)

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/example/demo/
│   │       ├── DemoApplication.java           # Main application class
│   │       ├── config/
│   │       │   └── MQConfig.java              # JMS configuration
│   │       └── listener/
│   │           └── MessageListener.java       # Message consumer
│   └── resources/
│       ├── application.yml                    # Application configuration
│       └── logback-spring.xml                 # Logging configuration
```

## Configuration

Update `src/main/resources/application.yml` if your IBM MQ setup differs:

```yaml
ibm:
  mq:
    queue-manager: QM1
    channel: DEV.APP.SVRCONN
    conn-name: localhost(1414)
    user: app
    password: passw0rd

mq:
  queue-name: DEV.QUEUE.1
```

## How to Run

1. **Build the project:**
   ```bash
   mvnw clean install
   ```

2. **Run the application:**
   ```bash
   mvnw spring-boot:run
   ```

   Or using the compiled JAR:
   ```bash
   java -jar target/demo-0.0.1-SNAPSHOT.jar
   ```

3. **The application will:**
   - Connect to IBM MQ on startup
   - Listen to the `DEV.QUEUE.1` queue
   - Print any received messages to the console

## Testing the Application

To test the message consumption, you can send messages to the queue using:

1. **IBM MQ Explorer** - GUI tool for IBM MQ
2. **Command line** - Using `amqsput` tool:
   ```bash
   /opt/mqm/samp/bin/amqsput DEV.QUEUE.1 QM1
   ```
3. **Another Spring Boot application** - Create a producer application

## Expected Console Output

When a message is received, you'll see output similar to:

```
========================================
Received message from IBM MQ:
Message: Your message content here
========================================
```

## Dependencies

- Spring Boot 3.3.4
- IBM MQ Jakarta Client 9.4.3.1
- Spring JMS
- Jakarta JMS API 3.1.0
- Lombok (for logging)

## Troubleshooting

### Connection Issues

If you encounter connection errors:
1. Verify IBM MQ is running: Check if the queue manager is active
2. Check firewall settings: Ensure port 1414 is accessible
3. Verify credentials: Ensure the user/password in application.yml are correct
4. Check queue manager name: Ensure QM1 exists and is running

### Authentication Issues

If you get authentication errors, you may need to:
- Update the username and password in `application.yml`
- Or disable authentication for development (not recommended for production)

### Queue Not Found

Ensure the queue `DEV.QUEUE.1` exists in the queue manager `QM1`.

## Notes

- The listener uses auto-acknowledgment mode
- Concurrency is set to 1 (processes one message at a time)
- The application will automatically reconnect if the connection is lost
- Messages are logged using both SLF4J logger and System.out.println

## License

This is a demo project for IBM MQ integration with Spring Boot.

