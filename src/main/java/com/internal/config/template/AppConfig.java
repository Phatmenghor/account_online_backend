package com.internal.config.template;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.config.RequestConfig;
import java.nio.charset.StandardCharsets;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Configure HTTP client with UTF-8 charset support
        // 5 minutes (300s) timeout for slow operations: Account Opening, T24, Activator, CAMDX
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(300000)
                .setSocketTimeout(300000)
                .setConnectionRequestTimeout(300000)
                .build();

        org.apache.http.client.HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .build();

        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(
                new HttpComponentsClientHttpRequestFactory(httpClient));

        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }
}
