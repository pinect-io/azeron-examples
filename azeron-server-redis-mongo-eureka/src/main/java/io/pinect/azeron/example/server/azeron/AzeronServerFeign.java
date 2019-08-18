package io.pinect.azeron.example.server.azeron;

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
    @GetMapping("/info")
    @ResponseBody
    InfoResultDto getServersInfo();

    @GetMapping("/nats") @ResponseBody
    NatsConfigModel getNatsDetails();

    @GetMapping("/ping")
    @ResponseBody String ping();

    @PutMapping("/seen")
    @ResponseBody
    SeenResponseDto seenMessages(@RequestBody SeenDto seenDto);
}
