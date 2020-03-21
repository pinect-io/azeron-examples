package io.pinect.azeron.example.client.publisher;

import io.pinect.azeron.client.service.publisher.EventMessagePublisher;
import io.pinect.azeron.client.service.publisher.EventPublisher;
import io.pinect.azeron.client.service.publisher.Publisher;
import io.pinect.azeron.example.client.dto.SimpleAzeronMessage;
import nats.client.MessageHandler;

@Publisher(
        publishStrategy = EventMessagePublisher.PublishStrategy.AZERON,
        eventName = "full_event_name",
        forClass = SimpleAzeronMessage.class
)
public class FullMessagePublisher implements EventPublisher<SimpleAzeronMessage> {
    @Override
    public void publish(SimpleAzeronMessage simpleAzeronMessage, MessageHandler messageHandler) {
        System.out.println("So it is called!");
    }
}
