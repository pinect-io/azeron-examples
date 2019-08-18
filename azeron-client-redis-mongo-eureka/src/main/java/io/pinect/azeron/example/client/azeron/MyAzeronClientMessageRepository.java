package io.pinect.azeron.example.client.azeron;

import io.pinect.azeron.client.domain.entity.MessageEntity;
import io.pinect.azeron.client.domain.repository.MessageRepository;
import io.pinect.azeron.example.client.azeron.domain.MongoAzeronMessageEntity;
import io.pinect.azeron.example.client.azeron.domain.OffsetLimitPageable;
import io.pinect.azeron.example.client.azeron.domain.repository.MongoAzeronMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MyAzeronClientMessageRepository implements MessageRepository<MessageEntity> {
    private final MongoAzeronMessageRepository mongoAzeronMessageRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MyAzeronClientMessageRepository(MongoAzeronMessageRepository mongoAzeronMessageRepository, MongoTemplate mongoTemplate) {
        this.mongoAzeronMessageRepository = mongoAzeronMessageRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    @Cacheable(value = "azeron_client", key = "'message_'.concat(#messageEntity.messageId)", unless="#result == null", sync = true)
    public MessageEntity save(MessageEntity messageEntity) {
        MessageEntity result = null;
        try {
            result = mongoAzeronMessageRepository.save(new MongoAzeronMessageEntity(messageEntity));
        }catch (DuplicateKeyException | com.mongodb.DuplicateKeyException e){
            result = findById(messageEntity.getMessageId());
        }
        return result;
    }

    @Override
    public boolean exists(String messageId) {
        return false;
    }

    @Override
    @CachePut(value = "azeron_client", key = "'message_'.concat(#messageEntity.messageId)", unless="#result == null")
    public MessageEntity seen(MessageEntity messageEntity) {
        messageEntity.setSeen(true);
        mongoTemplate.updateFirst(Query.query(
                Criteria.where("messageId").is(messageEntity.getMessageId())
        ), Update.update("seen", true), MongoAzeronMessageEntity.class);
        return messageEntity;
    }

    @Override
    @CachePut(value = "azeron_client", key = "'message_'.concat(#messageEntity.messageId)", unless="#result == null")
    public MessageEntity processed(MessageEntity messageEntity) {
        messageEntity.setProcessed(true);
        mongoTemplate.updateFirst(Query.query(
                Criteria.where("messageId").is(messageEntity.getMessageId())
        ), Update.update("processed", true), MongoAzeronMessageEntity.class);
        return messageEntity;
    }

    @Override
    @CacheEvict(value = "azeron_client", key = "'message_'.concat(#messageEntity.messageId)")
    public void delete(MessageEntity messageEntity) {
        mongoAzeronMessageRepository.deleteByMessageId(messageEntity.getMessageId());
    }

    @Override
    public List<String> getUnseenMessageIds(long offset, int limit) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("seen").is(false)),
                Aggregation.project("messageId"),
                Aggregation.skip(offset),
                Aggregation.limit(limit)
        );
        AggregationResults<MongoAzeronMessageEntity> aggregate = mongoTemplate.aggregate(aggregation, MongoAzeronMessageEntity.class, MongoAzeronMessageEntity.class);
        List<MongoAzeronMessageEntity> mappedResults = aggregate.getMappedResults();
        List<String> list = new ArrayList<>();
        for(MongoAzeronMessageEntity mongoAzeronMessageEntity: mappedResults){
            list.add(mongoAzeronMessageEntity.getMessageId());
        }
        return list;
    }

    @Override
    public List<MessageEntity> getUnseenMessages(long offset, int limit) {
        return mongoAzeronMessageRepository.findAllBySeen(false, new OffsetLimitPageable((int) offset, limit));
    }

    @Override
    public List<String> getUnProcessedMessageIds(long offset, int limit) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("processed").is(false)),
                Aggregation.project("messageId"),
                Aggregation.skip(offset),
                Aggregation.limit(limit)
        );
        AggregationResults<MongoAzeronMessageEntity> aggregate = mongoTemplate.aggregate(aggregation, MongoAzeronMessageEntity.class, MongoAzeronMessageEntity.class);
        List<MongoAzeronMessageEntity> mappedResults = aggregate.getMappedResults();
        List<String> list = new ArrayList<>();
        for(MongoAzeronMessageEntity mongoAzeronMessageEntity: mappedResults){
            list.add(mongoAzeronMessageEntity.getMessageId());
        }
        return list;
    }

    @Override
    public List<MessageEntity> getUnProcessedMessages(long offset, int limit) {
        return mongoAzeronMessageRepository.findAllByProcessed(false, new OffsetLimitPageable((int) offset, limit));
    }

    @Override
    public int countUnProcessed() {
        return mongoAzeronMessageRepository.countAllByProcessed(false);
    }

    @Override
    @Cacheable(value = "azeron_client", key = "'message_'.concat(#id)", unless="#result == null", sync = true)
    public MessageEntity findById(String id) {
        return mongoAzeronMessageRepository.findByMessageId(id);
    }

    @Override
    public List<MessageEntity> findByIdIn(List<String> ids) {
        return mongoAzeronMessageRepository.findAllByMessageIdIn(ids);
    }
}
