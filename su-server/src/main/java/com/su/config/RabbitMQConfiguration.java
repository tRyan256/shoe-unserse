package com.su.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.constant.RabbitMqConstant;
import com.su.mq.support.MqFailureRecorder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(RabbitMqConstant.ORDER_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(RabbitMqConstant.ORDER_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderCreateQueue() {
        return QueueBuilder.durable(RabbitMqConstant.ORDER_CREATE_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstant.ORDER_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstant.ORDER_CREATE_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue orderCreateDlq() {
        return QueueBuilder.durable(RabbitMqConstant.ORDER_CREATE_DLQ).build();
    }

    @Bean
    public Binding orderCreateBinding(DirectExchange orderExchange, Queue orderCreateQueue) {
        return BindingBuilder.bind(orderCreateQueue).to(orderExchange).with(RabbitMqConstant.ORDER_CREATE_ROUTING_KEY);
    }

    @Bean
    public Binding orderCreateDlqBinding(DirectExchange orderDlxExchange, Queue orderCreateDlq) {
        return BindingBuilder.bind(orderCreateDlq).to(orderDlxExchange).with(RabbitMqConstant.ORDER_CREATE_DLQ_ROUTING_KEY);
    }

    @Bean
    public DirectExchange airdropExchange() {
        return new DirectExchange(RabbitMqConstant.AIRDROP_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange airdropDlxExchange() {
        return new DirectExchange(RabbitMqConstant.AIRDROP_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue airdropClaimQueue() {
        return QueueBuilder.durable(RabbitMqConstant.AIRDROP_CLAIM_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstant.AIRDROP_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstant.AIRDROP_CLAIM_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue airdropClaimDlq() {
        return QueueBuilder.durable(RabbitMqConstant.AIRDROP_CLAIM_DLQ).build();
    }

    @Bean
    public Binding airdropClaimBinding(DirectExchange airdropExchange, Queue airdropClaimQueue) {
        return BindingBuilder.bind(airdropClaimQueue).to(airdropExchange).with(RabbitMqConstant.AIRDROP_CLAIM_ROUTING_KEY);
    }

    @Bean
    public Binding airdropClaimDlqBinding(DirectExchange airdropDlxExchange, Queue airdropClaimDlq) {
        return BindingBuilder.bind(airdropClaimDlq).to(airdropDlxExchange).with(RabbitMqConstant.AIRDROP_CLAIM_DLQ_ROUTING_KEY);
    }

    @Bean
    public DirectExchange drawJoinExchange() {
        return new DirectExchange(RabbitMqConstant.DRAW_JOIN_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange drawJoinDlxExchange() {
        return new DirectExchange(RabbitMqConstant.DRAW_JOIN_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue drawJoinPersistQueue() {
        return QueueBuilder.durable(RabbitMqConstant.DRAW_JOIN_PERSIST_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstant.DRAW_JOIN_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstant.DRAW_JOIN_PERSIST_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue drawJoinPersistDlq() {
        return QueueBuilder.durable(RabbitMqConstant.DRAW_JOIN_PERSIST_DLQ).build();
    }

    @Bean
    public Binding drawJoinPersistBinding(DirectExchange drawJoinExchange, Queue drawJoinPersistQueue) {
        return BindingBuilder.bind(drawJoinPersistQueue).to(drawJoinExchange).with(RabbitMqConstant.DRAW_JOIN_PERSIST_ROUTING_KEY);
    }

    @Bean
    public Binding drawJoinPersistDlqBinding(DirectExchange drawJoinDlxExchange, Queue drawJoinPersistDlq) {
        return BindingBuilder.bind(drawJoinPersistDlq).to(drawJoinDlxExchange).with(RabbitMqConstant.DRAW_JOIN_PERSIST_DLQ_ROUTING_KEY);
    }

    @Bean
    public DirectExchange drawOrderExchange() {
        return new DirectExchange(RabbitMqConstant.DRAW_ORDER_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange drawOrderDlxExchange() {
        return new DirectExchange(RabbitMqConstant.DRAW_ORDER_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue drawOrderCreateQueue() {
        return QueueBuilder.durable(RabbitMqConstant.DRAW_ORDER_CREATE_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstant.DRAW_ORDER_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstant.DRAW_ORDER_CREATE_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue drawOrderCreateDlq() {
        return QueueBuilder.durable(RabbitMqConstant.DRAW_ORDER_CREATE_DLQ).build();
    }

    @Bean
    public Binding drawOrderCreateBinding(DirectExchange drawOrderExchange, Queue drawOrderCreateQueue) {
        return BindingBuilder.bind(drawOrderCreateQueue).to(drawOrderExchange).with(RabbitMqConstant.DRAW_ORDER_CREATE_ROUTING_KEY);
    }

    @Bean
    public Binding drawOrderCreateDlqBinding(DirectExchange drawOrderDlxExchange, Queue drawOrderCreateDlq) {
        return BindingBuilder.bind(drawOrderCreateDlq).to(drawOrderDlxExchange).with(RabbitMqConstant.DRAW_ORDER_CREATE_DLQ_ROUTING_KEY);
    }

    @Bean
    public DirectExchange experienceStatisticsExchange() {
        return new DirectExchange(RabbitMqConstant.EXPERIENCE_STATISTICS_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange experienceStatisticsDlxExchange() {
        return new DirectExchange(RabbitMqConstant.EXPERIENCE_STATISTICS_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue experienceStatisticsUpdateQueue() {
        return QueueBuilder.durable(RabbitMqConstant.EXPERIENCE_STATISTICS_UPDATE_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstant.EXPERIENCE_STATISTICS_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstant.EXPERIENCE_STATISTICS_UPDATE_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue experienceStatisticsUpdateDlq() {
        return QueueBuilder.durable(RabbitMqConstant.EXPERIENCE_STATISTICS_UPDATE_DLQ).build();
    }

    @Bean
    public Binding experienceStatisticsUpdateBinding(DirectExchange experienceStatisticsExchange, Queue experienceStatisticsUpdateQueue) {
        return BindingBuilder.bind(experienceStatisticsUpdateQueue).to(experienceStatisticsExchange).with(RabbitMqConstant.EXPERIENCE_STATISTICS_UPDATE_ROUTING_KEY);
    }

    @Bean
    public Binding experienceStatisticsUpdateDlqBinding(DirectExchange experienceStatisticsDlxExchange, Queue experienceStatisticsUpdateDlq) {
        return BindingBuilder.bind(experienceStatisticsUpdateDlq).to(experienceStatisticsDlxExchange).with(RabbitMqConstant.EXPERIENCE_STATISTICS_UPDATE_DLQ_ROUTING_KEY);
    }

    @Bean
    public DirectExchange experienceNotificationExchange() {
        return new DirectExchange(RabbitMqConstant.EXPERIENCE_NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange experienceNotificationDlxExchange() {
        return new DirectExchange(RabbitMqConstant.EXPERIENCE_NOTIFICATION_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue experienceNotificationQueue() {
        return QueueBuilder.durable(RabbitMqConstant.EXPERIENCE_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstant.EXPERIENCE_NOTIFICATION_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstant.EXPERIENCE_NOTIFICATION_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue experienceNotificationDlq() {
        return QueueBuilder.durable(RabbitMqConstant.EXPERIENCE_NOTIFICATION_DLQ).build();
    }

    @Bean
    public Binding experienceNotificationBinding(DirectExchange experienceNotificationExchange, Queue experienceNotificationQueue) {
        return BindingBuilder.bind(experienceNotificationQueue).to(experienceNotificationExchange).with(RabbitMqConstant.EXPERIENCE_NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding experienceNotificationDlqBinding(DirectExchange experienceNotificationDlxExchange, Queue experienceNotificationDlq) {
        return BindingBuilder.bind(experienceNotificationDlq).to(experienceNotificationDlxExchange).with(RabbitMqConstant.EXPERIENCE_NOTIFICATION_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter rabbitMessageConverter, MqFailureRecorder mqFailureRecorder) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(rabbitMessageConverter);
        rabbitTemplate.setConfirmCallback(mqFailureRecorder.confirmCallback());
        rabbitTemplate.setReturnsCallback(mqFailureRecorder.returnsCallback());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory drawJoinListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageConverter rabbitMessageConverter,
            @Value("${SU_DRAW_JOIN_RABBIT_BATCH_SIZE:200}") int batchSize,
            @Value("${SU_DRAW_JOIN_RABBIT_CONCURRENCY:16}") int concurrency,
            @Value("${SU_DRAW_JOIN_RABBIT_MAX_CONCURRENCY:64}") int maxConcurrency,
            @Value("${SU_DRAW_JOIN_RABBIT_PREFETCH:400}") int prefetch
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setConsumerBatchEnabled(true);
        factory.setBatchListener(true);
        int safeBatchSize = Math.max(1, batchSize);
        int safeConcurrency = Math.max(1, concurrency);
        int safeMaxConcurrency = Math.max(safeConcurrency, maxConcurrency);
        factory.setBatchSize(safeBatchSize);
        factory.setConcurrentConsumers(safeConcurrency);
        factory.setMaxConcurrentConsumers(safeMaxConcurrency);
        factory.setPrefetchCount(Math.max(safeBatchSize, prefetch));
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory airdropClaimListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageConverter rabbitMessageConverter,
            @Value("${SU_AIRDROP_CLAIM_RABBIT_BATCH_SIZE:200}") int batchSize,
            @Value("${SU_AIRDROP_CLAIM_RABBIT_CONCURRENCY:8}") int concurrency,
            @Value("${SU_AIRDROP_CLAIM_RABBIT_MAX_CONCURRENCY:32}") int maxConcurrency,
            @Value("${SU_AIRDROP_CLAIM_RABBIT_PREFETCH:200}") int prefetch
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setConsumerBatchEnabled(true);
        factory.setBatchListener(true);
        int safeBatchSize = Math.max(1, batchSize);
        int safeConcurrency = Math.max(1, concurrency);
        int safeMaxConcurrency = Math.max(safeConcurrency, maxConcurrency);
        factory.setBatchSize(safeBatchSize);
        factory.setConcurrentConsumers(safeConcurrency);
        factory.setMaxConcurrentConsumers(safeMaxConcurrency);
        factory.setPrefetchCount(Math.max(safeBatchSize, prefetch));
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageConverter rabbitMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}





