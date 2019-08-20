package io.pinect.azeron.example.client.azeron;

import io.pinect.azeron.client.domain.dto.ResponseStatus;
import io.pinect.azeron.client.domain.dto.in.PongDto;
import io.pinect.azeron.client.service.api.Pinger;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class FeignPinger implements Pinger {
    private final AzeronServerFeign azeronServerFeign;
    private final String serviceName;

    @Autowired
    public FeignPinger(AzeronServerFeign azeronServerFeign, @Value("${spring.application.name}") String serviceName) {
        this.azeronServerFeign = azeronServerFeign;
        this.serviceName = serviceName;
    }

    @Override
    public PongDto ping() {
        try {
            return azeronServerFeign.ping(serviceName);
        }catch (Exception e){
            log.error(e);
        }
        return PongDto.builder().status(ResponseStatus.FAILED).build();
    }
}
