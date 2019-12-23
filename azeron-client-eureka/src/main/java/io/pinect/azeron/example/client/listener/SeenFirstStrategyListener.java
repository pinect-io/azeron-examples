package io.pinect.azeron.example.client.listener;

import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.dto.out.MessageDto;
import io.pinect.azeron.client.domain.model.ClientConfig;
import io.pinect.azeron.client.service.handler.AbstractAzeronMessageHandler;
import io.pinect.azeron.client.service.handler.AzeronMessageHandlerDependencyHolder;
import io.pinect.azeron.example.client.dto.SimpleAzeronMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class SeenFirstStrategyListener extends AbstractAzeronMessageHandler<SimpleAzeronMessage> {
    private final String serviceName;
    @Autowired
    public SeenFirstStrategyListener(AzeronMessageHandlerDependencyHolder azeronMessageHandlerDependencyHolder, @Value("${spring.application.name}") String serviceName) {
        super(azeronMessageHandlerDependencyHolder);
        this.serviceName = serviceName;
    }

    @Override
    public HandlerPolicy policy() {
        return HandlerPolicy.SEEN_FIRST;
    }

    @Override
    public Class<SimpleAzeronMessage> eClass() {
        return SimpleAzeronMessage.class;
    }

    @Override
    public AzeronMessageProcessor<SimpleAzeronMessage> azeronMessageProcessor() {
        return new AzeronMessageProcessor<SimpleAzeronMessage>() {
            @Override
            public void process(SimpleAzeronMessage simpleAzeronMessage) {
                String text = simpleAzeronMessage.getText();
                log.info("Processing text: "+ text);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    log.catching(e);
                }
                log.info("Finished processing text: "+ text);
            }
        };
    }

    @Override
    public AzeronErrorHandler azeronErrorHandler() {
        return new AzeronErrorHandler() {
            @Override
            public void onError(Exception e, MessageDto messageDto) {
                log.error("Error while handling message -> "+ messageDto, e);
            }
        };
    }

    @Override
    public String eventName() {
        return "seen_first_event_name";
    }

    @Override
    public ClientConfig clientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setServiceName(serviceName);
        clientConfig.setUseQueueGroup(true);
        clientConfig.setVersion(1);
        return clientConfig;
    }
}
