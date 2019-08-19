package io.pinect.azeron.example.client;

import io.pinect.azeron.example.client.publisher.SimpleMessagePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AzeronClientExampleApplication {
    private final SimpleMessagePublisher simpleMessagePublisher;

    @Autowired
    public AzeronClientExampleApplication(SimpleMessagePublisher simpleMessagePublisher) {
        this.simpleMessagePublisher = simpleMessagePublisher;
    }

    public static void main(String[] args) {
        SpringApplication.run(AzeronClientExampleApplication.class, args);
    }

    @GetMapping("/full")
    public @ResponseBody String sendSimpleMessage(@RequestParam("text") String text){
        simpleMessagePublisher.publishSimpleTextMessage(text, "full_event_name");
        return "OK";
    }

    @GetMapping("/async")
    public @ResponseBody String sendSimpleMessageAsyncSeen(@RequestParam("text") String text){
        simpleMessagePublisher.publishSimpleTextMessage(text, "async_event_name");
        return "OK";
    }


    @GetMapping("/loosable")
    public @ResponseBody String sendSimpleMessageSyncFirst(@RequestParam("text") String text){
        simpleMessagePublisher.publishSimpleTextMessage(text, "seen_first_event_name");
        return "OK";
    }
}
