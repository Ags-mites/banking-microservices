package com.bank.bankingservice.infrastructure.output.client;

import com.bank.bankingservice.domain.ports.out.ClientVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomerRestClient implements ClientVerifier {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public CustomerRestClient(@Value("${customerservice.base-url:http://localhost:8081}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public boolean existsById(Long clienteId) {
        try {
            String url = String.format("%s/api/clientes/%d", baseUrl, clienteId);
            var response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException.NotFound nf) {
            return false;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) return false;
            throw e;
        }
    }
}
