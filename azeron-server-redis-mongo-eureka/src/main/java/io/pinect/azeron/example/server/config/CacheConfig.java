package io.pinect.azeron.example.server.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableCaching
public class CacheConfig {

    private RedisConnectionFactory redisConnectionFactory(){
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(new RedisStandaloneConfiguration());
        lettuceConnectionFactory.setDatabase(1);
        lettuceConnectionFactory.afterPropertiesSet();
        lettuceConnectionFactory.initConnection();
        return lettuceConnectionFactory;
    }

    @Bean
    public CacheManager cacheManager(){
        Set<String> cacheNames = new HashSet<>();
        cacheNames.add("azeron_server");

        RedisCacheConfiguration redisCacheConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .prefixKeysWith("spring:cache")
                        .disableCachingNullValues()
                        .entryTtl(Duration.ofSeconds(30));
        RedisCacheManager cacheManager = RedisCacheManager.builder(redisConnectionFactory())
                .cacheDefaults(redisCacheConfiguration)
                .initialCacheNames(cacheNames)
                .build();
        cacheManager.initializeCaches();
        cacheManager.afterPropertiesSet();
        return cacheManager;
    }

}
