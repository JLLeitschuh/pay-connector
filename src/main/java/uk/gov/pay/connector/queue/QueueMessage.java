package uk.gov.pay.connector.queue;

import com.amazonaws.services.sqs.model.ReceiveMessageResult;

import java.util.List;
import java.util.stream.Collectors;

public class QueueMessage {

    private String messageId;
    private String receiptHandle;
    private String messageBody;

    private QueueMessage(String messageId, String receiptHandle, String messageBody) {
        this.messageId = messageId;
        this.receiptHandle = receiptHandle;
        this.messageBody = messageBody;
    }

    public static List<QueueMessage> of(ReceiveMessageResult receiveMessageResult) {

        List<QueueMessage> queueMessage = receiveMessageResult.getMessages()
                .stream()
                .map(c -> new QueueMessage(c.getMessageId(), c.getReceiptHandle(), c.getBody()))
                .collect(Collectors.toList());

        return queueMessage;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getReceiptHandle() {
        return receiptHandle;
    }

    public String getMessageBody() {
        return messageBody;
    }
}
