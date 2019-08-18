package io.pinect.azeron.example.server.azeron.domain.repository;


import io.pinect.azeron.example.server.azeron.domain.MongoAzeronMessageEntity;
import io.pinect.azeron.server.domain.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoAzeronMessageRepository extends MongoRepository<MongoAzeronMessageEntity, String> {
    MongoAzeronMessageEntity findByMessageId(String messageId);
    boolean existsByMessageId(String messageId);
    void deleteByMessageId(String messageId);
    List<MessageEntity> findAllBySubscribersInAndSeenSubscribersNin(String service, String ninService, Pageable pageable);
    int countAllBySubscribersInAndSeenSubscribersNin(String service, String ninService);
}
