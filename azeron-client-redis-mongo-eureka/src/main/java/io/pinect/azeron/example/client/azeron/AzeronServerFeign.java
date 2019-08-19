package io.pinect.azeron.example.client.azeron;

import io.pinect.azeron.client.domain.dto.in.InfoResultDto;
import io.pinect.azeron.client.domain.dto.in.SeenResponseDto;
import io.pinect.azeron.client.domain.dto.out.SeenDto;
import io.pinect.azeron.client.domain.model.NatsConfigModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient("azeron-server")
public interface AzeronServerFeign {
    @GetMapping("/api/v1/info")
    @ResponseBody
    InfoResultDto getServersInfo();

    @GetMapping("/api/v1/nats") @ResponseBody
    NatsConfigModel getNatsDetails();

    @GetMapping("/api/v1/ping")
    @ResponseBody String ping();

    @PutMapping("/api/v1/seen")
    @ResponseBody
    SeenResponseDto seenMessages(@RequestBody SeenDto seenDto);
}
