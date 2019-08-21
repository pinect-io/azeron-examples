package io.pinect.azeron.example.client.domain;

import io.pinect.azeron.client.domain.repository.FallbackRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service("fallbackRepository")
@Log4j2
public class InMemoryFallbackRepository implements FallbackRepository {
    private final List<FallbackEntity> list = new CopyOnWriteArrayList<>();

    @Override
    public synchronized void saveMessage(FallbackEntity fallbackEntity) {
        if(!list.contains(fallbackEntity)){
            list.add(fallbackEntity);
            log.trace("Saved message "+ fallbackEntity.getId() + " to fallback repository");
        }
    }

    @Override
    public void deleteMessage(String id) {
        list.forEach(fallbackEntity -> {
            if(fallbackEntity.getId().equals(id))
                list.remove(fallbackEntity);
        });
    }

    @Override
    public void deleteMessage(FallbackEntity fallbackEntity) {
        list.remove(fallbackEntity);
    }

    @Override
    public List<FallbackEntity> getMessages(int offset, int limit) {
        return list.stream().skip(offset).limit(limit).collect(Collectors.toList());
    }

    @Override
    public int countAll() {
        return list.size();
    }
}
