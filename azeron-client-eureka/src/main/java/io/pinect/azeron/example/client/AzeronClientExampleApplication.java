package io.pinect.azeron.example.client;

import io.pinect.azeron.example.client.dto.SimpleAzeronMessage;
import io.pinect.azeron.example.client.publisher.*;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import nats.client.Message;
import nats.client.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AzeronClientExampleApplication {
    private final AsyncMessagePublisher asyncMessagePublisher;
    private final FullMessagePublisher fullMessagePublisher;
    private final NoAzeronMessagePublisher noAzeronMessagePublisher;
    private final SeenFirstMessagePublisher seenFirstMessagePublisher;
    private final NatsRawMessagePublisher natsRawMessagePublisher;

    @Autowired
    public AzeronClientExampleApplication(@Lazy AsyncMessagePublisher asyncMessagePublisher, FullMessagePublisher fullMessagePublisher, NoAzeronMessagePublisher noAzeronMessagePublisher, SeenFirstMessagePublisher seenFirstMessagePublisher, NatsRawMessagePublisher natsRawMessagePublisher) {
        this.asyncMessagePublisher = asyncMessagePublisher;
        this.fullMessagePublisher = fullMessagePublisher;
        this.noAzeronMessagePublisher = noAzeronMessagePublisher;
        this.seenFirstMessagePublisher = seenFirstMessagePublisher;
        this.natsRawMessagePublisher = natsRawMessagePublisher;
    }

    public static void main(String[] args) {
        SpringApplication.run(AzeronClientExampleApplication.class, args);
    }

    @GetMapping("/full")
    public @ResponseBody String sendSimpleMessage(@RequestParam("text") String text){
        System.out.println(Thread.currentThread().getId());
        fullMessagePublisher.publish(new SimpleAzeronMessage(text));
        return "OK";
    }

    @Getter
    @Setter
    private class MessageResult {
        boolean ok = false;
    }

    @SneakyThrows
    @GetMapping("/nats")
    public @ResponseBody String sendSimpleNatsMessageWithHandler(@RequestParam("text") String text){
        MessageResult messageResult = new MessageResult();

        natsRawMessagePublisher.publish(new SimpleAzeronMessage(text), new MessageHandler() {
            @Override
            public void onMessage(Message message) {
                System.out.println(message.getBody());
                synchronized (messageResult){
                    messageResult.setOk(true);
                    messageResult.notify();
                }
            }
        });

        synchronized (messageResult){
            messageResult.wait(10000);
        }

        return "OK";
    }

    @GetMapping("/async")
    public @ResponseBody String sendSimpleMessageAsyncSeen(@RequestParam("text") String text){
        System.out.println(Thread.currentThread().getId());
        asyncMessagePublisher.publish(new SimpleAzeronMessage("hi - async"), null);
        return "OK";
    }


    @GetMapping("/loosable")
    public @ResponseBody String sendSimpleMessageSyncFirst(@RequestParam("text") String text){
        seenFirstMessagePublisher.publish(new SimpleAzeronMessage("hi - seen first"), null);
        return "OK";
    }

    @GetMapping("/noAzeron")
    public @ResponseBody String sendSimpleNatsMessage(@RequestParam("text") String text){
        noAzeronMessagePublisher.publish(new SimpleAzeronMessage("hi - no azeron"), null);
        return "OK";
    }
}
