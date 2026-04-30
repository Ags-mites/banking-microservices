package com.bank.bankingservice.infrastructure.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "customer.events";
    public static final String ROUTING_KEY_CLIENTE_CREADO = "cliente.creado";
    public static final String QUEUE_CLIENTE_CREADO = "cliente.creado.queue";
    public static final String DEADLETTER_EXCHANGE = "customer.events.dlx";
    public static final String DEADLETTER_QUEUE = "cliente.creado.deadletter.queue";
    public static final String DEADLETTER_ROUTING_KEY = "cliente.creado.deadletter";

    @Bean
    public DirectExchange customerEventsExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEADLETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue clienteCreadoQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", DEADLETTER_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", DEADLETTER_ROUTING_KEY);
        return new Queue(QUEUE_CLIENTE_CREADO, true, false, false, arguments);
    }

    @Bean
    public Queue clienteCreadoDeadLetterQueue() {
        return new Queue(DEADLETTER_QUEUE, true);
    }

    @Bean
    public Binding clienteCreadoBinding(Queue clienteCreadoQueue, DirectExchange customerEventsExchange) {
        return BindingBuilder.bind(clienteCreadoQueue)
                .to(customerEventsExchange)
                .with(ROUTING_KEY_CLIENTE_CREADO);
    }

    @Bean
    public Binding clienteCreadoDeadLetterBinding(Queue clienteCreadoDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(clienteCreadoDeadLetterQueue)
                .to(deadLetterExchange)
                .with(DEADLETTER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public RetryTemplate rabbitRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000L);
        backOffPolicy.setMaxInterval(10000L);
        backOffPolicy.setMultiplier(2.0);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(org.springframework.dao.CannotAcquireLockException.class, true);
        retryableExceptions.put(org.springframework.dao.DataAccessResourceFailureException.class, true);
        retryableExceptions.put(org.springframework.amqp.AmqpConnectException.class, true);
        retryableExceptions.put(ListenerExecutionFailedException.class, true);

        RetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions, true);
        retryTemplate.setRetryPolicy(retryPolicy);
        return retryTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            RetryTemplate rabbitRetryTemplate
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(
                org.springframework.amqp.rabbit.config.RetryInterceptorBuilder.stateless()
                        .retryOperations(rabbitRetryTemplate)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );
        return factory;
    }
}
