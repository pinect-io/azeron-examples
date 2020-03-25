package io.pinect.azeron.example.client.publisher;

import io.pinect.azeron.client.service.publisher.EventMessagePublisher;
import io.pinect.azeron.client.service.publisher.EventPublisher;
import io.pinect.azeron.client.service.publisher.Publisher;
import io.pinect.azeron.example.client.dto.SimpleAzeronMessage;
import nats.client.MessageHandler;

@Publisher(
        publishStrategy = EventMessagePublisher.PublishStrategy.NATS,
        eventName = "nats_raw",
        forClass = SimpleAzeronMessage.class,
        raw = true
)
public interface NatsRawMessagePublisher {
    void publish(SimpleAzeronMessage simpleAzeronMessage, MessageHandler messageHandler);
    void publish(SimpleAzeronMessage simpleAzeronMessage);
}
