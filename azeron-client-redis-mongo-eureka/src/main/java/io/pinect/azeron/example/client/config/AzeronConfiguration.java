package io.pinect.azeron.example.client.config;

import io.pinect.azeron.client.EnableAzeronClient;
import io.pinect.azeron.client.service.EventListenerRegistry;
import io.pinect.azeron.example.client.listener.FullStrategyListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@EnableAzeronClient
@Configuration
@EnableFeignClients(basePackages = "io.pinect.azeron.example.client.azeron")
public class AzeronConfiguration {
    private final FullStrategyListener fullStrategyListener;
    private final EventListenerRegistry eventListenerRegistry;

    @Autowired
    public AzeronConfiguration(FullStrategyListener fullStrategyListener, EventListenerRegistry eventListenerRegistry) {
        this.fullStrategyListener = fullStrategyListener;
        this.eventListenerRegistry = eventListenerRegistry;
    }

    @PostConstruct
    public void registerServices(){
        eventListenerRegistry.register(fullStrategyListener);
    }
}
