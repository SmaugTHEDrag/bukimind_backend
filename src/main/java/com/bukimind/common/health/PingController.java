package com.bukimind.common.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
@Tag(name = "Ping API", description = "API for monitoring backend uptime using UptimeRobot")
public class PingController {

    @Operation(summary = "Ping backend service")
    @GetMapping("/ping")
    public String ping(){
        return "OK";
    }
}
