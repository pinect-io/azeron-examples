package io.pinect.azeron.example.client.azeron;

import io.pinect.azeron.client.service.AzeronServerStatusTracker;
import io.pinect.azeron.client.service.api.Pinger;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class FeignPinger implements Pinger {
    private final AzeronServerFeign azeronServerFeign;

    @Autowired
    public FeignPinger(AzeronServerFeign azeronServerFeign) {
        this.azeronServerFeign = azeronServerFeign;
    }

    @Override
    public AzeronServerStatusTracker.Status ping() {
        try {
            String res = azeronServerFeign.ping();
            if(res.toLowerCase().equals("pong"))
                return AzeronServerStatusTracker.Status.UP;
        }catch (Exception e){
            log.error(e);
        }
        return AzeronServerStatusTracker.Status.DOWN;
    }
}
