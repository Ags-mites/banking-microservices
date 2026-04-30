package com.bank.bankingservice.infrastructure.messaging;

import com.bank.bankingservice.application.dto.ClienteCreadoEvent;
import com.bank.bankingservice.domain.ports.out.ClienteRefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ClienteCreadoEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClienteCreadoEventListener.class);

    private final ClienteRefRepository clienteRefRepository;

    public ClienteCreadoEventListener(ClienteRefRepository clienteRefRepository) {
        this.clienteRefRepository = clienteRefRepository;
    }

    @RabbitListener(queues = "${banking.messaging.queue.cliente-creado:cliente.creado.queue}")
    public void handleClienteCreadoEvent(ClienteCreadoEvent event) {
        validateEvent(event);
        clienteRefRepository.upsert(event.clienteId(), event.nombre(), event.identificacion());
        LOGGER.info("ClienteRef sincronizado para clienteId={}", event.clienteId());
    }

    private void validateEvent(ClienteCreadoEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Evento cliente.creado nulo");
        }
        if (event.clienteId() == null) {
            throw new IllegalArgumentException("clienteId es obligatorio en cliente.creado");
        }
        if (!StringUtils.hasText(event.nombre())) {
            throw new IllegalArgumentException("nombre es obligatorio en cliente.creado");
        }
        if (!StringUtils.hasText(event.identificacion())) {
            throw new IllegalArgumentException("identificacion es obligatoria en cliente.creado");
        }
    }
}
