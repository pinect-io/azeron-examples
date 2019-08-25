package io.pinect.azeron.example.client.azeron;

import io.pinect.azeron.client.domain.dto.in.InfoResultDto;
import io.pinect.azeron.client.domain.model.NatsConfigModel;
import io.pinect.azeron.client.service.api.NatsConfigProvider;
import io.pinect.azeron.client.util.NatsConfigurationMerge;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

@Service("natsConfigProvider")
@Log4j2
public class FeignNatsConfigProvider implements NatsConfigProvider {
    private final AzeronServerFeign azeronServerFeign;
    private final RetryTemplate retryTemplate;

    @Autowired
    public FeignNatsConfigProvider(AzeronServerFeign azeronServerFeign, RetryTemplate retryTemplate) {
        this.azeronServerFeign = azeronServerFeign;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public NatsConfigModel getNatsConfig() {
        log.trace("Getting nats config model");
        try {
            return (NatsConfigModel) this.retryTemplate.execute(new RetryCallback<NatsConfigModel, Throwable>() {
                public NatsConfigModel doWithRetry(RetryContext retryContext) throws Throwable {
                    InfoResultDto infoResultDto = azeronServerFeign.getServersInfo();
                    return NatsConfigurationMerge.getMergedNatsConfig(infoResultDto.getResults());
                }
            });
        } catch (Throwable var2) {
            log.error(var2);
            throw new RuntimeException(var2);
        }
    }
}
