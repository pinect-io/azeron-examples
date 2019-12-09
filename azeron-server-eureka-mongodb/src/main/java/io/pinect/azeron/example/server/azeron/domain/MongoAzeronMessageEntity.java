package io.pinect.azeron.example.server.azeron.domain;

import io.pinect.azeron.server.domain.entity.MessageEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

@Getter
@Setter
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
        if((this.getSubscribers() == null || this.getSeenSubscribers().size() == 0) && messageEntity.getSubscribers() != null)
            setSubscribers(messageEntity.getSubscribers());
        setChannel(messageEntity.getChannel());
        setCompleted(messageEntity.isCompleted());
        setDate(messageEntity.getDate());
        setMessage(messageEntity.getMessage());
        setSeenNeeded(messageEntity.getSeenNeeded());
        setSeenCount(messageEntity.getSeenCount());
        if((this.getSeenSubscribers() == null || this.getSeenSubscribers().size() == 0) && messageEntity.getSeenSubscribers() != null)
            setSeenSubscribers(messageEntity.getSeenSubscribers());
        setSender(messageEntity.getSender());
    }

    @Override
    @Transient
    public void setCompleted(boolean completed) {
        super.setCompleted(completed);
    }

    @Override
    @Transient
    public boolean isCompleted() {
        return super.isCompleted();
    }

    @Override
    @Transient
    public void setLocked(boolean locked) {
        super.setLocked(locked);
    }

    @Override
    @Transient
    public boolean isLocked() {
        return super.isLocked();
    }

    @Override
    public String toString(){
        return super.toString();
    }
}
