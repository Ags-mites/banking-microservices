package com.bank.bankingservice.infrastructure.output.client;

import com.bank.bankingservice.domain.ports.out.ClientVerifier;
import com.bank.bankingservice.domain.ports.out.ClienteLookup;
import com.bank.bankingservice.domain.ports.out.ClienteRefRepository;
import org.springframework.stereotype.Component;

@Component
public class CustomerRestClient implements ClientVerifier, ClienteLookup {

    private final ClienteRefRepository clienteRefRepository;

    public CustomerRestClient(ClienteRefRepository clienteRefRepository) {
        this.clienteRefRepository = clienteRefRepository;
    }

    @Override
    public boolean existsById(Long clienteId) {
        return clienteRefRepository.findByClienteId(clienteId).isPresent();
    }

    @Override
    public String findNombreById(Long clienteId) {
        return clienteRefRepository.findByClienteId(clienteId)
                .map(clienteRef -> clienteRef.nombre())
                .orElse(null);
    }
}
