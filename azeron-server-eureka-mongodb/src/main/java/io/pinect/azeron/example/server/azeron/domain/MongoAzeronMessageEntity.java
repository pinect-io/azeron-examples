package io.pinect.azeron.example.server.azeron.domain;

import io.pinect.azeron.server.domain.entity.MessageEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@ToString
public class MongoAzeronMessageEntity extends MessageEntity {
    @Id
    private String id;

    public MongoAzeronMessageEntity() {
    }

    public MongoAzeronMessageEntity(MessageEntity messageEntity) {
        fill(messageEntity);
    }


    public void fill(MessageEntity messageEntity){
        setMessageId(messageEntity.getMessageId());
        setMessage(messageEntity.getMessage());
        if(this.getSubscribers() == null)
            setSubscribers(messageEntity.getSubscribers());
        setChannel(messageEntity.getChannel());
        setCompleted(messageEntity.isCompleted());
        setDate(messageEntity.getDate());
        setMessage(messageEntity.getMessage());
        setSeenNeeded(messageEntity.getSeenNeeded());
        setSeenCount(messageEntity.getSeenCount());
        if(getSeenSubscribers() == null)
            setSeenSubscribers(messageEntity.getSeenSubscribers());
        setSender(messageEntity.getSender());
    }
}
