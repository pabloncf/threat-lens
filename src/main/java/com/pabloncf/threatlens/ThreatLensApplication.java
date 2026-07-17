package com.pabloncf.threatlens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ThreatLensApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThreatLensApplication.class, args);
    }

}
