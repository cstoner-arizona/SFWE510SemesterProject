package org.skate.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootConfiguration
@EnableConfigServer
public class ConfigurationServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigurationServerApplication.class, args);
    }
}
