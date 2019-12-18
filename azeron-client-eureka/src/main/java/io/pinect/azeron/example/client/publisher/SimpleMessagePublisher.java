package io.pinect.azeron.example.client.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.domain.repository.FallbackRepository;
import io.pinect.azeron.client.service.AzeronServerStatusTracker;
import io.pinect.azeron.client.service.publisher.EventMessagePublisher;
import io.pinect.azeron.example.client.dto.SimpleAzeronMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class SimpleMessagePublisher extends EventMessagePublisher {

    @Autowired
    public SimpleMessagePublisher(AtomicNatsHolder atomicNatsHolder, ObjectMapper objectMapper, AzeronServerStatusTracker azeronServerStatusTracker, FallbackRepository fallbackRepository, RetryTemplate eventPublishRetryTemplate, @Value("${spring.application.name}") String serviceName) {
        super(atomicNatsHolder, objectMapper, azeronServerStatusTracker, fallbackRepository, eventPublishRetryTemplate, serviceName);
    }

    @Async
    public void publishSimpleTextMessage(String text, String channelName){
        try {
            String value = getObjectMapper().writeValueAsString(new SimpleAzeronMessage(text));
            log.trace("Publishing message "+ value + " to channel `"+channelName+"`");
            sendMessage(channelName, value, PublishStrategy.AZERON);
        } catch (Exception e) {
            log.catching(e);
        }
    }
}
