package com.example.demo;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.msg.client.jakarta.wmq.common.CommonConstants;
import jakarta.jms.JMSException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class MqConfig {

    @Value("${ibm.mq.queueManager}")
    private String queueManager;

    @Value("${ibm.mq.channel}")
    private String channel;

    @Value("${ibm.mq.connName}")
    private String connName;


    @Bean
    public MQConnectionFactory mqConnectionFactory() throws JMSException {
        MQConnectionFactory factory = new MQConnectionFactory();
        factory.setHostName(connName.split("\\(")[0]);
        factory.setPort(Integer.parseInt(connName.split("\\(")[1].replace(")", "")));
        factory.setQueueManager(queueManager);
        factory.setChannel(channel);
//        factory.setTransportType(CommonConstants.WMQ_CM_CLIENT);
        
        // Completely disable MQCSP authentication - this is critical for your setup
//        factory.setBooleanProperty(CommonConstants.USER_AUTHENTICATION_MQCSP, false);
        
        // Don't set any user credentials - let the channel handle it with USERSRC(CHANNEL)
        // This matches your CHLAUTH configuration with USERSRC(CHANNEL)
        
        return factory;
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory(MQConnectionFactory mqConnectionFactory) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(mqConnectionFactory);
        cachingConnectionFactory.setSessionCacheSize(10);
        cachingConnectionFactory.setCacheConsumers(false);
        cachingConnectionFactory.setCacheProducers(true);
        return cachingConnectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate(CachingConnectionFactory cachingConnectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory);
        jmsTemplate.setReceiveTimeout(5000);
        return jmsTemplate;
    }
}

