package com.bank.customerservice.infrastructure.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.bank.customerservice.application.dto.ClienteCreadoEvent;
import com.bank.customerservice.application.dto.ClienteActualizadoEvent;

@Component
public class ClienteEventPublisher {
    
    private static final String EXCHANGE_NAME = "customer.events";
    private static final String ROUTING_KEY_CLIENTE_CREADO = "cliente.creado";
    private static final String ROUTING_KEY_CLIENTE_ACTUALIZADO = "cliente.actualizado";
    
    private final RabbitTemplate rabbitTemplate;
    
    public ClienteEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    public void publicarClienteCreadoEvent(ClienteCreadoEvent event) {
        rabbitTemplate.convertAndSend(
            EXCHANGE_NAME,
            ROUTING_KEY_CLIENTE_CREADO,
            event
        );
    }

    public void publicarClienteActualizadoEvent(ClienteActualizadoEvent event) {
        rabbitTemplate.convertAndSend(
            EXCHANGE_NAME,
            ROUTING_KEY_CLIENTE_ACTUALIZADO,
            event
        );
    }
}
