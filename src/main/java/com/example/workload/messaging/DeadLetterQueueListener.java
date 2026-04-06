package com.example.workload.messaging;

import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeadLetterQueueListener {

    @JmsListener(destination = "${workload.queue.dlq-name:workload.dlq}")
    public void handleDeadLetter(Message message) {
        try {
            String messageContent = "";
            if (message instanceof TextMessage textMessage) {
                messageContent = textMessage.getText();
            }

            log.error("Message moved to DLQ. Message ID: {}, Content: {}",
                    message.getJMSMessageID(), messageContent);

            // Additional handling can be added here:
            // - Store in database for manual review
            // - Send alert notification
            // - Retry with different strategy

        } catch (Exception e) {
            log.error("Error processing DLQ message", e);
        }
    }
}

