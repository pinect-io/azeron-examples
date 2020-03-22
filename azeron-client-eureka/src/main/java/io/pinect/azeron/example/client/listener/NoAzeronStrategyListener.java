package io.pinect.azeron.example.client.listener;

import io.pinect.azeron.client.domain.HandlerPolicy;
import io.pinect.azeron.client.domain.dto.out.MessageDto;
import io.pinect.azeron.client.service.listener.AzeronListener;
import io.pinect.azeron.client.service.listener.SimpleEventListener;
import io.pinect.azeron.example.client.dto.SimpleAzeronMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@AzeronListener(eventName = "no_azeron_event_name", ofClass = SimpleAzeronMessage.class, policy = HandlerPolicy.NO_AZERON)
public class NoAzeronStrategyListener implements SimpleEventListener<SimpleAzeronMessage> {
    private final String serviceName;

    @Autowired
    public NoAzeronStrategyListener(@Value("${spring.application.name}") String serviceName) {
        this.serviceName = serviceName;
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
    public String serviceName() {
        return serviceName;
    }

}
