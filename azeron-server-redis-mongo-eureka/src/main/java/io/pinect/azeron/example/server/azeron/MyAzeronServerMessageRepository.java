package io.pinect.azeron.example.server.azeron;

import com.mongodb.DuplicateKeyException;
import io.pinect.azeron.example.server.azeron.domain.MongoAzeronMessageEntity;
import io.pinect.azeron.example.server.azeron.domain.OffsetLimitPageable;
import io.pinect.azeron.example.server.azeron.domain.repository.MongoAzeronMessageRepository;
import io.pinect.azeron.server.domain.entity.MessageEntity;
import io.pinect.azeron.server.domain.repository.MessageRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("messageRepository")
public class MyAzeronServerMessageRepository implements MessageRepository {
    private final MongoAzeronMessageRepository mongoAzeronMessageRepository;
    private final MongoTemplate mongoTemplate;

    public MyAzeronServerMessageRepository(MongoAzeronMessageRepository mongoAzeronMessageRepository, MongoTemplate mongoTemplate) {
        this.mongoAzeronMessageRepository = mongoAzeronMessageRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    @Cacheable(value = "azeron_server", key = "'message_'.concat(#messageEntity.messageId)", unless="#result == null", sync = true)
    public MessageEntity addMessage(MessageEntity messageEntity) {
        MessageEntity result = null;
        try {
            mongoAzeronMessageRepository.save(new MongoAzeronMessageEntity(messageEntity));
        }catch (DuplicateKeyException | org.springframework.dao.DuplicateKeyException e){
            MongoAzeronMessageEntity mongoAzeronMessageEntity = mongoAzeronMessageRepository.findByMessageId(messageEntity.getMessageId());
            mongoAzeronMessageEntity.fill(messageEntity);
            mongoAzeronMessageRepository.save(mongoAzeronMessageEntity);
            result = mongoAzeronMessageEntity;
        }
        return result;
    }

    @Override
    @CachePut(value = "azeron_server", key = "'message_'.concat(#messageId)", unless="#result == null")
    public MessageEntity seenMessage(String messageId, String serviceName) {
        MongoAzeronMessageEntity messageEntity = mongoAzeronMessageRepository.findByMessageId(messageId);
        if(messageEntity == null){
            //todo: queue to add seen when for message later
        }
        mongoTemplate.upsert(Query.query(Criteria.where("messageId").is(messageId)),
                new Update().addToSet("seenSubscribers", serviceName).inc("seenCount", 1),
                MongoAzeronMessageEntity.class);
        List<String> seenSubscribers = messageEntity.getSeenSubscribers();
        if(seenSubscribers == null)
            seenSubscribers = new ArrayList<>();

        seenSubscribers.add(serviceName);
        messageEntity.setSeenCount(messageEntity.getSeenCount() + 1);
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

    @Override
    public MessageResult getUnseenMessagesOfService(String serviceName, int offset, int limit) {
        List<MessageEntity> messageEntities = mongoAzeronMessageRepository.findAllBySubscribersInAndSeenSubscribersNin(serviceName, serviceName, new OffsetLimitPageable(offset, limit));
        int i = mongoAzeronMessageRepository.countAllBySubscribersInAndSeenSubscribersNin(serviceName, serviceName);
        return MessageResult.builder().hasMore(i > messageEntities.size()).messages(messageEntities).build();
    }
}
