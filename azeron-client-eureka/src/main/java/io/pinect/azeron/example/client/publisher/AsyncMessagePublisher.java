package io.pinect.azeron.example.client.publisher;

import io.pinect.azeron.client.service.publisher.EventMessagePublisher;
import io.pinect.azeron.client.service.publisher.EventPublisher;
import io.pinect.azeron.client.service.publisher.Publisher;
import io.pinect.azeron.example.client.dto.SimpleAzeronMessage;
import nats.client.MessageHandler;

@Publisher(
        publishStrategy = EventMessagePublisher.PublishStrategy.AZERON,
        eventName = "async_event_name",
        forClass = SimpleAzeronMessage.class
)
public interface AsyncMessagePublisher extends EventPublisher<SimpleAzeronMessage> {
    void publish(SimpleAzeronMessage simpleAzeronMessage, MessageHandler messageHandler);
    void publish(SimpleAzeronMessage simpleAzeronMessage);
}