package io.pinect.azeron.example.client.azeron.domain.repository;


import io.pinect.azeron.client.domain.entity.MessageEntity;
import io.pinect.azeron.example.client.azeron.domain.MongoAzeronMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoAzeronMessageRepository extends MongoRepository<MongoAzeronMessageEntity, String> {
    MongoAzeronMessageEntity findByMessageId(String messageId);
    boolean existsByMessageId(String messageId);
    void deleteByMessageId(String messageId);
    List<MessageEntity> findAllBySeen(boolean seen, Pageable pageable);
    List<MessageEntity> findAllByProcessed(boolean processed, Pageable pageable);
    List<MongoAzeronMessageEntity> findAllByProcessedAndSeen(boolean processed, boolean seen, Pageable pageable);
    int countAllByProcessed(boolean processed);
    List<MessageEntity> findAllByMessageIdIn(List<String> ids);
}
