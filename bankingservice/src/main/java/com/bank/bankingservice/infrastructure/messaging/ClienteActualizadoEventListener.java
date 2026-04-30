package com.bank.bankingservice.infrastructure.messaging;

import com.bank.bankingservice.application.dto.ClienteActualizadoEvent;
import com.bank.bankingservice.domain.ports.out.ClienteRefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ClienteActualizadoEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClienteActualizadoEventListener.class);

    private final ClienteRefRepository clienteRefRepository;

    public ClienteActualizadoEventListener(ClienteRefRepository clienteRefRepository) {
        this.clienteRefRepository = clienteRefRepository;
    }

    @RabbitListener(queues = "${banking.messaging.queue.cliente-actualizado:cliente.actualizado.queue}")
    public void handleClienteActualizadoEvent(ClienteActualizadoEvent event) {
        validateEvent(event);
        clienteRefRepository.upsert(event.clienteId(), event.nombre(), event.identificacion());
        LOGGER.info("ClienteRef actualizado (sincronizado) para clienteId={}", event.clienteId());
    }

    private void validateEvent(ClienteActualizadoEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Evento cliente.actualizado nulo");
        }
        if (event.clienteId() == null) {
            throw new IllegalArgumentException("clienteId es obligatorio en cliente.actualizado");
        }
        if (!StringUtils.hasText(event.nombre())) {
            throw new IllegalArgumentException("nombre es obligatorio en cliente.actualizado");
        }
        if (!StringUtils.hasText(event.identificacion())) {
            throw new IllegalArgumentException("identificacion es obligatoria en cliente.actualizado");
        }
    }
}
