package io.pinect.azeron.example.client.publisher;

import io.pinect.azeron.client.service.publisher.EventMessagePublisher;
import io.pinect.azeron.client.service.publisher.EventPublisher;
import io.pinect.azeron.client.service.publisher.Publisher;
import io.pinect.azeron.example.client.dto.SimpleAzeronMessage;
import nats.client.MessageHandler;
import org.springframework.scheduling.annotation.Async;

@Publisher(
        publishStrategy = EventMessagePublisher.PublishStrategy.AZERON,
        eventName = "full_event_name",
        forClass = SimpleAzeronMessage.class
)
public interface FullMessagePublisher {
    void publish(SimpleAzeronMessage simpleAzeronMessage, MessageHandler messageHandler);
    @Async
    void publish(SimpleAzeronMessage simpleAzeronMessage);
}
