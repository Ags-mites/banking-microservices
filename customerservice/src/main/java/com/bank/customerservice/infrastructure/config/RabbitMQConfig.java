package com.bank.customerservice.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class RabbitMQConfig {
    
    public static final String EXCHANGE_NAME = "customer.events";

    public static final String ROUTING_KEY_CLIENTE_CREADO = "cliente.creado";
    
    public static final String QUEUE_CLIENTE_CREADO = "cliente.creado.queue";
    
    @Bean
    public DirectExchange customerEventsExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }
    
    @Bean
    public Queue clienteCreadoQueue() {
        return new Queue(QUEUE_CLIENTE_CREADO, true);
    }
    
    @Bean
    public Binding clienteCreadoBinding(Queue clienteCreadoQueue, DirectExchange customerEventsExchange) {
        return BindingBuilder.bind(clienteCreadoQueue)
            .to(customerEventsExchange)
            .with(ROUTING_KEY_CLIENTE_CREADO);
    }
    
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(
        ConnectionFactory connectionFactory,
        MessageConverter messageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
