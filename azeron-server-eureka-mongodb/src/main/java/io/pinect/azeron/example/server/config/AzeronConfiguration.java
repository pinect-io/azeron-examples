package io.pinect.azeron.example.server.config;

import io.pinect.azeron.server.EnableAzeronServer;
import io.pinect.azeron.server.decorator.MapCacheMessageRepositoryDecorator;
import io.pinect.azeron.server.domain.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;

@EnableAzeronServer
@Configuration
public class AzeronConfiguration {
    private final MessageRepository myAzeronServerMessageRepository;
    private MapCacheMessageRepositoryDecorator mapCacheMessageRepositoryDecorator;

    @Autowired
    public AzeronConfiguration(MessageRepository myAzeronServerMessageRepository) {
        this.myAzeronServerMessageRepository = myAzeronServerMessageRepository;
    }

    @Bean
    public MessageRepository messageRepository(){
        mapCacheMessageRepositoryDecorator = new MapCacheMessageRepositoryDecorator(myAzeronServerMessageRepository, new ConcurrentHashMap<>(), 1000, 20);
        return mapCacheMessageRepositoryDecorator;
    }

    // Flushes cache into main repository
    @PreDestroy
    public void destroy(){
        mapCacheMessageRepositoryDecorator.flush();
    }

}
