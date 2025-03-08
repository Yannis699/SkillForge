package com.skillforge.api.config;

import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient fichiersWebClient(LoadBalancedExchangeFilterFunction lbFunction) {
        return WebClient.builder()
                .baseUrl("http://fichiers-service")
                .filter(lbFunction)
                .build();
    }
}
