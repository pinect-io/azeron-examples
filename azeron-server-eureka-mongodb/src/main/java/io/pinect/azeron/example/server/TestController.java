package io.pinect.azeron.example.server;

import io.pinect.azeron.example.server.azeron.MyAzeronServerMessageRepository;
import io.pinect.azeron.server.decorator.MapCacheMessageRepositoryDecorator;
import io.pinect.azeron.server.domain.entity.MessageEntity;
import io.pinect.azeron.server.domain.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class TestController {
    private final MyAzeronServerMessageRepository myAzeronServerMessageRepository;
    private final MessageRepository cacheMessageRepository;

    @Autowired
    public TestController(MyAzeronServerMessageRepository myAzeronServerMessageRepository) {
        this.myAzeronServerMessageRepository = myAzeronServerMessageRepository;
        cacheMessageRepository = new MapCacheMessageRepositoryDecorator(myAzeronServerMessageRepository, new ConcurrentHashMap<>(), 12, 10000);
    }

    @GetMapping("/test/1")
    public @ResponseBody
    String test1(){
        String serviceName1 = "sv1";
        String serviceName2 = "sv2";
        String messageId = UUID.randomUUID().toString();
        Set<String> subsNeeded = new HashSet<>();
        subsNeeded.add(serviceName1);
        subsNeeded.add(serviceName2);

        myAzeronServerMessageRepository.seenMessage(messageId, serviceName2);
        myAzeronServerMessageRepository.seenMessage(messageId, serviceName2);


        MessageEntity messageEntity = myAzeronServerMessageRepository.addMessage(MessageEntity.builder()
                .channel("---")
                .date(new Date())
                .message("temp message")
                .messageId(messageId)
                .seenNeeded(subsNeeded.size())
                .subscribers(subsNeeded)
                .sender("sn3")
                .build());

        Assert.isTrue(messageEntity.getSeenCount() > 0, "Seen Count Not > 0");
        System.out.println(messageEntity.toString());
        return "OK";
    }


    @GetMapping("/test/2")
    public @ResponseBody
    String test2(){
        String serviceName1 = "sv_cache_1";
        String serviceName2 = "sv_cache_2";
        String messageId = UUID.randomUUID().toString();
        Set<String> subsNeeded = new HashSet<>();
        subsNeeded.add(serviceName1);
        subsNeeded.add(serviceName2);

        cacheMessageRepository.seenMessage(messageId, serviceName1);
        cacheMessageRepository.seenMessage(messageId, serviceName2);


        MessageEntity messageEntity = cacheMessageRepository.addMessage(MessageEntity.builder()
                .channel("---")
                .date(new Date())
                .message("temp message")
                .messageId(messageId)
                .seenNeeded(subsNeeded.size())
                .subscribers(subsNeeded)
                .sender("sn3")
                .build());

        System.out.println(messageEntity.toString());
        Assert.isTrue(messageEntity.getSeenCount() > 0, "Seen Count Not > 0");
        return "OK";
    }


    @GetMapping("/test/3")
    public @ResponseBody
    String test3(){
        for(int i = 0; i < 15; i++){
            String serviceName1 = "sv_cache_1";
            String serviceName2 = "sv_cache_2";
            String messageId = UUID.randomUUID().toString();
            Set<String> subsNeeded = new HashSet<>();
            subsNeeded.add(serviceName1);
            subsNeeded.add(serviceName2);

            cacheMessageRepository.seenMessage(messageId, serviceName1);
            cacheMessageRepository.seenMessage(messageId, serviceName2);


            MessageEntity messageEntity = cacheMessageRepository.addMessage(MessageEntity.builder()
                    .channel("---")
                    .date(new Date())
                    .message("temp message")
                    .messageId(messageId)
                    .seenNeeded(subsNeeded.size())
                    .subscribers(subsNeeded)
                    .sender("sn3")
                    .build());

            System.out.println(messageEntity.toString());
        }
        return "OK";
    }


    @GetMapping("/test/4")
    public @ResponseBody
    String test4(){
        String serviceName1 = "sv_cache_1";
        String serviceName2 = "sv_cache_2";
        String messageId = UUID.randomUUID().toString();
        Set<String> subsNeeded = new HashSet<>();
        subsNeeded.add(serviceName1);
        subsNeeded.add(serviceName2);

        MessageEntity messageEntity = cacheMessageRepository.addMessage(MessageEntity.builder()
                .channel("---")
                .date(new Date())
                .message("temp message")
                .messageId(messageId)
                .seenNeeded(subsNeeded.size())
                .subscribers(subsNeeded)
                .sender("sn3")
                .build());

        System.out.println(messageEntity);
        MessageRepository.MessageResult unseenMessagesOfService = cacheMessageRepository.getUnseenMessagesOfService(serviceName1, 0, 10, new Date(new Date().getTime() + 100000));
        Assert.isTrue(unseenMessagesOfService.getMessages().size() == 1, "unseen result is not right");
        return "OK";
    }

    @GetMapping("/test/5")
    public @ResponseBody
    String test5(){
        String serviceName1 = "sv_cache_1";
        String serviceName2 = "sv_cache_2";
        String messageId = UUID.randomUUID().toString();
        Set<String> subsNeeded = new HashSet<>();
        subsNeeded.add(serviceName1);
        subsNeeded.add(serviceName2);

        MessageEntity messageEntity = cacheMessageRepository.addMessage(MessageEntity.builder()
                .channel("---")
                .date(new Date())
                .message("temp message")
                .messageId(messageId)
                .seenNeeded(subsNeeded.size())
                .subscribers(subsNeeded)
                .sender("sn3")
                .build());

        cacheMessageRepository.seenMessage(messageId, serviceName1);
        MessageRepository.MessageResult unseenMessagesOfService = cacheMessageRepository.getUnseenMessagesOfService(serviceName1, 0, 10, new Date());
        Assert.isTrue(unseenMessagesOfService.getMessages().size() == 0, "unseen result is not right. its: "+ unseenMessagesOfService.getMessages().size());
        return "OK";
    }

    @GetMapping("/test/6")
    public @ResponseBody
    String test6(){
        String serviceName1 = "sv_cache_1";
        String serviceName2 = "sv_cache_2";
        String messageId = UUID.randomUUID().toString();
        Set<String> subsNeeded = new HashSet<>();
        subsNeeded.add(serviceName1);
        subsNeeded.add(serviceName2);

        MessageEntity messageEntity = myAzeronServerMessageRepository.addMessage(MessageEntity.builder()
                .channel("---")
                .date(new Date())
                .message("temp message")
                .messageId(messageId)
                .seenNeeded(subsNeeded.size())
                .subscribers(subsNeeded)
                .sender("sn3")
                .build());

        System.out.println(messageEntity);
        MessageRepository.MessageResult unseenMessagesOfService = myAzeronServerMessageRepository.getUnseenMessagesOfService(serviceName1, 0, 10, new Date(new Date().getTime() + 100000));
        Assert.isTrue(unseenMessagesOfService.getMessages().size() == 1, "unseen result is not right");
        return "OK";
    }

    @GetMapping("/test/7")
    public @ResponseBody
    String test7(){
        String serviceName1 = "sv_cache_1";
        String serviceName2 = "sv_cache_2";
        String messageId = UUID.randomUUID().toString();
        Set<String> subsNeeded = new HashSet<>();
        subsNeeded.add(serviceName1);
        subsNeeded.add(serviceName2);

        MessageEntity messageEntity = myAzeronServerMessageRepository.addMessage(MessageEntity.builder()
                .channel("---")
                .date(new Date())
                .message("temp message")
                .messageId(messageId)
                .seenNeeded(subsNeeded.size())
                .subscribers(subsNeeded)
                .sender("sn3")
                .build());


        MessageRepository.MessageResult unseenMessagesOfService = cacheMessageRepository.getUnseenMessagesOfService(serviceName1, 0, 10, new Date());
        myAzeronServerMessageRepository.seenMessage(messageId, serviceName1);
        unseenMessagesOfService = myAzeronServerMessageRepository.getUnseenMessagesOfService(serviceName1, 0, 10, new Date());
        Assert.isTrue(unseenMessagesOfService.getMessages().size() == 0, "unseen result is not right");
        return "OK";
    }

}
