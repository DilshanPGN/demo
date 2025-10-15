package com.example.demo.listener;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.jms.annotation.JmsListener;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//public class MessageListener {
//
//    @JmsListener(destination = "${mq.queue-name}")
//    public void receiveMessage(String message) {
//        log.info("========================================");
//        log.info("Received message from IBM MQ:");
//        log.info("Message: {}", message);
//        log.info("========================================");
//        System.out.println("========================================");
//        System.out.println("Received message from IBM MQ:");
//        System.out.println("Message: " + message);
//        System.out.println("========================================");
//    }
//}
//
