package io.pinect.azeron.example.client.azeron.domain;

import io.pinect.azeron.client.domain.entity.MessageEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Getter
@Setter
public class MongoAzeronMessageEntity extends MessageEntity {
    @Id
    private String id;

    public MongoAzeronMessageEntity() {
    }

    public MongoAzeronMessageEntity(MessageEntity messageEntity) {
        setMessageId(messageEntity.getMessageId());
        setMessage(messageEntity.getMessage());
        setSeen(messageEntity.isSeen());
        setChannelName(messageEntity.getChannelName());
        setProcessed(messageEntity.isProcessed());
        setDate(messageEntity.getDate());
        setServiceName(messageEntity.getServiceName());
    }

    public MongoAzeronMessageEntity(String messageId, String message, Date date, String channelName, String serviceName, boolean isSeen, boolean isProcessed) {
        super(messageId, message, date, channelName, serviceName, isSeen, isProcessed);
    }
}
