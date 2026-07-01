package br.edu.ifce.biblioteca.front.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${autores.api.url}")
    private String autoresApiUrl;

    @Value("${livros.api.url}")
    private String livrosApiUrl;

    @Bean
    public RestClient autorRestClient() {
        return RestClient.builder()
                .baseUrl(autoresApiUrl)
                .build();
    }

    @Bean
    public RestClient livroRestClient() {
        return RestClient.builder()
                .baseUrl(livrosApiUrl)
                .build();
    }
}
