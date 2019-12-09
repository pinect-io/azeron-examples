package io.pinect.azeron.example.server.azeron;

import com.mongodb.DuplicateKeyException;
import io.pinect.azeron.example.server.azeron.domain.MongoAzeronMessageEntity;
import io.pinect.azeron.example.server.azeron.domain.OffsetLimitPageable;
import io.pinect.azeron.example.server.azeron.domain.repository.MongoAzeronMessageRepository;
import io.pinect.azeron.server.domain.entity.MessageEntity;
import io.pinect.azeron.server.domain.repository.MessageRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service("myAzeronServerMessageRepository")
@Log4j2
public class MyAzeronServerMessageRepository implements MessageRepository {
    private final MongoAzeronMessageRepository mongoAzeronMessageRepository;
    private final MongoTemplate mongoTemplate;


    @Autowired
    public MyAzeronServerMessageRepository(MongoAzeronMessageRepository mongoAzeronMessageRepository, MongoTemplate mongoTemplate) {
        this.mongoAzeronMessageRepository = mongoAzeronMessageRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public MessageEntity addMessage(MessageEntity messageEntity) {
        try {
            log.trace("Saving message to repository -> "+ messageEntity.toString());
            return mongoAzeronMessageRepository.save(new MongoAzeronMessageEntity(messageEntity));
        }catch (DuplicateKeyException | org.springframework.dao.DuplicateKeyException e){
            log.trace("Duplicate message found. Maybe seen has reached sooner. Updating message body -> "+ messageEntity.toString());
            MongoAzeronMessageEntity mongoAzeronMessageEntity = mongoAzeronMessageRepository.findByMessageId(messageEntity.getMessageId());
            Update update = new Update()
                    .set("message", messageEntity.getMessage())
                    .set("date", messageEntity.getDate())
                    .set("channel", messageEntity.getChannel())
                    .set("subscribers", messageEntity.getSubscribers())
                    .set("seenNeeded", messageEntity.getSeenNeeded())
                    .set("sender", messageEntity.getSender())
                    .set("dirty", false);

            if(mongoAzeronMessageEntity.isDirty()){
                update.set("seenCount", mongoAzeronMessageEntity.getSeenCount());
                mongoAzeronMessageEntity.getSeenSubscribers().forEach(s -> {
                    update.addToSet("seenSubscribers", s);
                });
            }

            mongoTemplate.upsert(Query.query(Criteria.where("messageId").is(messageEntity.getMessageId())),
                    update,
                    MongoAzeronMessageEntity.class);
            updateObjectFields(mongoAzeronMessageEntity, messageEntity);

            return mongoAzeronMessageEntity;
        }
    }

    private void updateObjectFields(MongoAzeronMessageEntity mongoAzeronMessageEntity, MessageEntity messageEntity) {
        mongoAzeronMessageEntity.setMessage(messageEntity.getMessage());
        mongoAzeronMessageEntity.setDate(messageEntity.getDate());
        mongoAzeronMessageEntity.setChannel(messageEntity.getChannel());
        mongoAzeronMessageEntity.setSubscribers(messageEntity.getSubscribers());
        mongoAzeronMessageEntity.setSeenNeeded(messageEntity.getSeenNeeded());
        mongoAzeronMessageEntity.setSender(messageEntity.getSender());
        if(mongoAzeronMessageEntity.isDirty()){
            Set<String> seenSubscribers = mongoAzeronMessageEntity.getSeenSubscribers();
            seenSubscribers.addAll(mongoAzeronMessageEntity.getSeenSubscribers());
            mongoAzeronMessageEntity.setSeenSubscribers(seenSubscribers);
        }
        mongoAzeronMessageEntity.setDirty(false);
    }

    @Override
    public synchronized void seenMessage(String messageId, String serviceName) {
        log.trace("Adding seen for message "+ messageId + " from service "+ serviceName);
        mongoTemplate.upsert(Query.query(Criteria.where("messageId").is(messageId)),
                new Update().addToSet("seenSubscribers", serviceName).inc("seenCount", 1).set("dirty", true),
                MongoAzeronMessageEntity.class);
    }

    @Override
    public void seenMessages(List<String> messageIds, String serviceName) {
        mongoTemplate.upsert(Query.query(Criteria.where("messageId").in(messageIds)),
                new Update().push("seenSubscribers", serviceName).inc("seenCount", 1),
                MongoAzeronMessageEntity.class);
    }

    @Override
    public void removeMessage(String messageId) {
        mongoAzeronMessageRepository.deleteByMessageId(messageId);
    }

    @Override
    public MessageResult getUnseenMessagesOfService(String serviceName, int offset, int limit, Date before) {
        log.trace("Getting seen for service "+ serviceName + " | offset -> " + offset + " limit -> "+ limit + " before -> " + before);
        List<MessageEntity> messageEntities = mongoAzeronMessageRepository.findAllBySubscribersInAndSeenSubscribersNotInAndDateBefore(serviceName, serviceName, before, new OffsetLimitPageable(offset, limit));
        int i = mongoAzeronMessageRepository.countAllBySubscribersInAndSeenSubscribersNotIn(serviceName, serviceName);
        log.trace("Results for "+ serviceName + " unseen query | count -> "+ i + " current size -> "+ messageEntities.size());
        return MessageResult.builder().hasMore(i > messageEntities.size()).messages(messageEntities).build();
    }

    @Override
    public MessageEntity getMessage(String messageId) {
        return mongoAzeronMessageRepository.findByMessageId(messageId);
    }
}
