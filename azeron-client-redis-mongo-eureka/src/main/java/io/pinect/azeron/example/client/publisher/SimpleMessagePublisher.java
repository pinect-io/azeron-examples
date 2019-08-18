package io.pinect.azeron.example.client.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinect.azeron.client.AtomicNatsHolder;
import io.pinect.azeron.client.domain.repository.FallbackRepository;
import io.pinect.azeron.client.service.AzeronServerStatusTracker;
import io.pinect.azeron.client.service.publisher.EventMessagePublisher;
import io.pinect.azeron.example.client.dto.SimpleAzeronMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

@Service
public class SimpleMessagePublisher extends EventMessagePublisher {

    @Autowired
    public SimpleMessagePublisher(AtomicNatsHolder atomicNatsHolder, ObjectMapper objectMapper, AzeronServerStatusTracker azeronServerStatusTracker, FallbackRepository fallbackRepository, RetryTemplate eventPublishRetryTemplate, @Value("${spring.application.name}") String serviceName) {
        super(atomicNatsHolder, objectMapper, azeronServerStatusTracker, fallbackRepository, eventPublishRetryTemplate, serviceName);
    }

    public void publishSimpleTextMessage(String text){
        try {
            String value = getObjectMapper().writeValueAsString(new SimpleAzeronMessage(text));
            sendMessage("full_event_name", value, PublishStrategy.BLOCKED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
