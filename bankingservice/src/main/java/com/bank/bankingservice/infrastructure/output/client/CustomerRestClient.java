package com.bank.bankingservice.infrastructure.output.client;

import com.bank.bankingservice.domain.ports.out.ClientVerifier;
import com.bank.bankingservice.domain.ports.out.ClienteLookup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomerRestClient implements ClientVerifier, ClienteLookup {

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

    @Override
    public String findNombreById(Long clienteId) {
        try {
            String url = String.format("%s/api/clientes/%d", baseUrl, clienteId);
            String response = restTemplate.getForObject(url, String.class);
            return extractNombre(response);
        } catch (HttpClientErrorException.NotFound nf) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible consultar el cliente con id: " + clienteId, e);
        }
    }

    private String extractNombre(String response) {
        if (response == null || response.isBlank()) {
            return null;
        }

        String marker = "\"nombre\"";
        int fieldIndex = response.indexOf(marker);
        if (fieldIndex < 0) {
            return null;
        }

        int colonIndex = response.indexOf(':', fieldIndex + marker.length());
        if (colonIndex < 0) {
            return null;
        }

        int startQuote = response.indexOf('"', colonIndex + 1);
        if (startQuote < 0) {
            return null;
        }

        int endQuote = response.indexOf('"', startQuote + 1);
        if (endQuote < 0) {
            return null;
        }

        return response.substring(startQuote + 1, endQuote);
    }
}
