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
    @Cacheable(value = "azeron_server", key = "'message_'.concat(#messageEntity.messageId)", unless="#result == null", sync = true)
    public MessageEntity addMessage(MessageEntity messageEntity) {
        MessageEntity result = null;
        try {
            log.trace("Saving message to repository -> "+ messageEntity.toString());
            mongoAzeronMessageRepository.save(new MongoAzeronMessageEntity(messageEntity));
        }catch (DuplicateKeyException | org.springframework.dao.DuplicateKeyException e){
            MongoAzeronMessageEntity mongoAzeronMessageEntity = mongoAzeronMessageRepository.findByMessageId(messageEntity.getMessageId());
            mongoAzeronMessageEntity.fill(messageEntity);
            mongoAzeronMessageRepository.save(mongoAzeronMessageEntity);
            log.trace("Duplicate message found, re-saving -> "+ messageEntity.toString());
            result = mongoAzeronMessageEntity;
        }
        return result;
    }

    @Override
    @CachePut(value = "azeron_server", key = "'message_'.concat(#messageId)", unless="#result == null")
    public MessageEntity seenMessage(String messageId, String serviceName) {
        log.trace("Adding seen for message "+ messageId);
        MongoAzeronMessageEntity messageEntity = mongoAzeronMessageRepository.findByMessageId(messageId);
        if(messageEntity == null){
            messageEntity = mongoAzeronMessageRepository.findByMessageId(messageId);

            List<String> seenSubscribers = messageEntity.getSeenSubscribers();
            if(seenSubscribers == null) {
                seenSubscribers = new ArrayList<>();
            }

            List<String> subscribers = messageEntity.getSeenSubscribers();
            if(subscribers == null)
                subscribers = clientTracker.getChannelsOfService(serviceName);

            seenSubscribers.add(serviceName);
            messageEntity.setSubscribers(subscribers);
            messageEntity.setSeenSubscribers(seenSubscribers);
            messageEntity.setSeenNeeded(subscribers.size());
            messageEntity.setSeenCount(messageEntity.getSeenCount() + 1);
            mongoAzeronMessageRepository.save(messageEntity);
        }

        mongoTemplate.upsert(Query.query(Criteria.where("messageId").is(messageId)),
                new Update().set("channel", messageEntity.getChannel()).set("message", messageEntity.getMessage()).set("sender",messageEntity.getSender()).addToSet("subscribers", messageEntity.getSubscribers()).set("seenNeeded", messageEntity.getSeenNeeded()).addToSet("seenSubscribers", serviceName).inc("seenCount", 1),
                MongoAzeronMessageEntity.class);

        return messageEntity;
    }

    @Override
    public void seenMessages(List<String> messageIds, String serviceName) {
        mongoTemplate.upsert(Query.query(Criteria.where("messageId").in(messageIds)),
                new Update().addToSet("seenSubscribers", serviceName).inc("seenCount", 1),
                MongoAzeronMessageEntity.class);
    }

    @Override
    @CacheEvict(value = "azeron_server", key = "'message_'.concat(#messageId)")
    public void removeMessage(String messageId) {
        mongoAzeronMessageRepository.deleteByMessageId(messageId);
    }

    //todo
    @Override
    public MessageResult getUnseenMessagesOfService(String serviceName, int offset, int limit, Date before) {
        List<MessageEntity> messageEntities = mongoAzeronMessageRepository.findAllBySubscribersInAndSeenSubscribersNotIn(serviceName, serviceName, new OffsetLimitPageable(offset, limit));
        int i = mongoAzeronMessageRepository.countAllBySubscribersInAndSeenSubscribersNotIn(serviceName, serviceName);
        return MessageResult.builder().hasMore(i > messageEntities.size()).messages(messageEntities).build();
    }
}
