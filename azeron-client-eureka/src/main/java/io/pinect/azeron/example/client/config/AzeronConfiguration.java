package io.pinect.azeron.example.client.config;

import io.pinect.azeron.client.EnableAzeronClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableAzeronClient(basePackages = "io.pinect.azeron.example.client.publisher")
@Configuration
@EnableFeignClients(basePackages = "io.pinect.azeron.example.client.azeron")
public class AzeronConfiguration {
}
