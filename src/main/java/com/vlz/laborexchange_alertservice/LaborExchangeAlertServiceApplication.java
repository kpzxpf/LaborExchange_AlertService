package com.vlz.laborexchange_alertservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LaborExchangeAlertServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LaborExchangeAlertServiceApplication.class, args);
    }
}
