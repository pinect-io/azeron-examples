package io.pinect.azeron.example.server.azeron;

import com.mongodb.DuplicateKeyException;
import io.pinect.azeron.example.server.azeron.domain.MongoAzeronMessageEntity;
import io.pinect.azeron.example.server.azeron.domain.OffsetLimitPageable;
import io.pinect.azeron.example.server.azeron.domain.repository.MongoAzeronMessageRepository;
import io.pinect.azeron.server.domain.entity.MessageEntity;
import io.pinect.azeron.server.domain.repository.MessageRepository;
import io.pinect.azeron.server.service.tracker.ClientTracker;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("messageRepository")
@Log4j2
public class MyAzeronServerMessageRepository implements MessageRepository {
    private final MongoAzeronMessageRepository mongoAzeronMessageRepository;
    private final ClientTracker clientTracker;
    private final MongoTemplate mongoTemplate;

    public MyAzeronServerMessageRepository(MongoAzeronMessageRepository mongoAzeronMessageRepository, ClientTracker clientTracker, MongoTemplate mongoTemplate) {
        this.mongoAzeronMessageRepository = mongoAzeronMessageRepository;
        this.clientTracker = clientTracker;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public MessageEntity addMessage(MessageEntity messageEntity) {
        try {
            log.trace("Saving message to repository -> "+ messageEntity.toString());
            return mongoAzeronMessageRepository.save(new MongoAzeronMessageEntity(messageEntity));
        }catch (DuplicateKeyException | org.springframework.dao.DuplicateKeyException e){
            log.trace("Duplicate message found. Maybe seen has reached sooner. Updating message body -> "+ messageEntity.toString());
            mongoTemplate.upsert(Query.query(Criteria.where("messageId").is(messageEntity.getMessageId())),
                    new Update()
                            .set("message",messageEntity.getMessage())
                            .set("date",messageEntity.getDate())
                            .set("channel",messageEntity.getChannel())
                            .set("subscribers",messageEntity.getSubscribers())
                            .set("seenNeeded",messageEntity.getSeenNeeded())
                            .set("sender",messageEntity.getSender()),
                    MongoAzeronMessageEntity.class);
            return mongoAzeronMessageRepository.findByMessageId(messageEntity.getMessageId());
        }
    }

    @Override
    public void seenMessage(String messageId, String serviceName) {
        log.trace("Adding seen for message "+ messageId);
        mongoTemplate.upsert(Query.query(Criteria.where("messageId").is(messageId)),
                new Update().addToSet("seenSubscribers", serviceName).inc("seenCount", 1),
                MongoAzeronMessageEntity.class);
    }

    @Override
    public void seenMessages(List<String> messageIds, String serviceName) {
        mongoTemplate.upsert(Query.query(Criteria.where("messageId").in(messageIds)),
                new Update().addToSet("seenSubscribers", serviceName).inc("seenCount", 1),
                MongoAzeronMessageEntity.class);
    }

    @Override
    public void removeMessage(String messageId) {
        mongoAzeronMessageRepository.deleteByMessageId(messageId);
    }

    //todo
    @Override
    public MessageResult getUnseenMessagesOfService(String serviceName, int offset, int limit, Date before) {
        List<MessageEntity> messageEntities = mongoAzeronMessageRepository.findAllBySubscribersInAndSeenSubscribersNotInAndDateBefore(serviceName, serviceName, before, new OffsetLimitPageable(offset, limit));
        int i = mongoAzeronMessageRepository.countAllBySubscribersInAndSeenSubscribersNotIn(serviceName, serviceName);
        return MessageResult.builder().hasMore(i > messageEntities.size()).messages(messageEntities).build();
    }

    @Override
    public MessageEntity getMessage(String messageId) {
        return mongoAzeronMessageRepository.findByMessageId(messageId);
    }
}
