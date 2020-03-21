package io.pinect.azeron.example.client.config;

import io.pinect.azeron.client.AtomicNatsHolder;
import lombok.extern.log4j.Log4j2;
import nats.client.Message;
import nats.client.MessageHandler;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {
    private final AtomicNatsHolder atomicNatsHolder;

    public ApplicationStartupListener(AtomicNatsHolder atomicNatsHolder) {
        this.atomicNatsHolder = atomicNatsHolder;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        atomicNatsHolder.getNatsAtomicReference().get().subscribe("nats_raw", new MessageHandler() {
            @Override
            public void onMessage(Message message) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Got this message -> "+ message.getBody());
                message.reply("This is working");
            }
        });
    }


}
