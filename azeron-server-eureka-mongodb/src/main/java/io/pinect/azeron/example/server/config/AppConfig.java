package io.pinect.azeron.example.server.config;

import com.mongodb.*;
import com.mongodb.lang.NonNull;
import io.pinect.azeron.example.server.azeron.domain.MongoAzeronMessageEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;

@Configuration
@EnableMongoRepositories(basePackages = {"io.pinect.azeron.example.server.azeron.domain.repository"})
public class AppConfig {
    @Bean
    public SimpleMongoDbFactory mongoDbFactory(){
        MongoClientOptions settings = MongoClientOptions.builder().connectionsPerHost(40)
                .maxWaitTime(2000).retryWrites(true).heartbeatFrequency(5000).threadsAllowedToBlockForConnectionMultiplier(10)
                .connectTimeout(5000).cursorFinalizerEnabled(true)
                .writeConcern(WriteConcern.ACKNOWLEDGED).readPreference(ReadPreference.nearest())
                .readConcern(ReadConcern.DEFAULT)
                .codecRegistry(MongoClient.getDefaultCodecRegistry()).build();
        return new SimpleMongoDbFactory(new MongoClient("127.0.0.1", settings), "azeronServerExampleDb");
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
        mongoTemplate.indexOps(MongoAzeronMessageEntity.class).ensureIndex(new Index().on("messageId", Sort.Direction.DESC).unique().named("azeron_message_id"));
        return mongoTemplate;
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        // we use exact UTC date pointing to 00:00 of given day to store LocalDate
        ArrayList<Object> list = new ArrayList<>();
        list.add(new Converter<LocalDate, Date>() {
            @Override
            public Date convert(@NonNull LocalDate source) {
                return new Date(source.atStartOfDay().atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
            }
        });
        list.add(new Converter<Date, LocalDate>() {
            @Override
            public LocalDate convert(@NonNull Date source) {
                return source.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
            }
        });
        return new MongoCustomConversions(list);
    }
}
