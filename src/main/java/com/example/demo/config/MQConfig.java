package com.example.demo.config;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableJms
public class MQConfig {

    @Value("${ibm.mq.queue-manager}")
    private String queueManager;

    @Value("${ibm.mq.channel}")
    private String channel;

    @Value("${ibm.mq.conn-name}")
    private String connName;

    @Value("${mq.queue-name}")
    private String queueName;

    @Bean
    public ConnectionFactory connectionFactory() {
        MQConnectionFactory mqConnectionFactory = new MQConnectionFactory();
        try {
            mqConnectionFactory.setQueueManager(queueManager);
            mqConnectionFactory.setChannel(channel);
            mqConnectionFactory.setConnectionNameList(connName);
            mqConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create MQ Connection Factory", e);
        }
        
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(mqConnectionFactory);
        cachingConnectionFactory.setSessionCacheSize(10);
        return cachingConnectionFactory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrency("1-1");
        factory.setSessionTransacted(false);
        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }

    public String getQueueName() {
        return queueName;
    }
}

