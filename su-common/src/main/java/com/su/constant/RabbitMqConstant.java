package com.su.constant;

public class RabbitMqConstant {
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_DLX_EXCHANGE = "order.dlx.exchange";

    public static final String ORDER_CREATE_QUEUE = "order.create.queue";
    public static final String ORDER_CREATE_DLQ = "order.create.dlq";

    public static final String ORDER_CREATE_ROUTING_KEY = "order.create";
    public static final String ORDER_CREATE_DLQ_ROUTING_KEY = "order.create.dlq";

    public static final String AIRDROP_EXCHANGE = "airdrop.exchange";
    public static final String AIRDROP_DLX_EXCHANGE = "airdrop.dlx.exchange";

    public static final String AIRDROP_CLAIM_QUEUE = "airdrop.claim.queue";
    public static final String AIRDROP_CLAIM_DLQ = "airdrop.claim.dlq";

    public static final String AIRDROP_CLAIM_ROUTING_KEY = "airdrop.claim";
    public static final String AIRDROP_CLAIM_DLQ_ROUTING_KEY = "airdrop.claim.dlq";

    public static final String DRAW_JOIN_EXCHANGE = "draw.join.exchange";
    public static final String DRAW_JOIN_DLX_EXCHANGE = "draw.join.dlx.exchange";

    public static final String DRAW_JOIN_PERSIST_QUEUE = "draw.join.persist.queue";
    public static final String DRAW_JOIN_PERSIST_DLQ = "draw.join.persist.dlq";

    public static final String DRAW_JOIN_PERSIST_ROUTING_KEY = "draw.join.persist";
    public static final String DRAW_JOIN_PERSIST_DLQ_ROUTING_KEY = "draw.join.persist.dlq";

    public static final String DRAW_ORDER_EXCHANGE = "draw.order.exchange";
    public static final String DRAW_ORDER_DLX_EXCHANGE = "draw.order.dlx.exchange";

    public static final String DRAW_ORDER_CREATE_QUEUE = "draw.order.create.queue";
    public static final String DRAW_ORDER_CREATE_DLQ = "draw.order.create.dlq";

    public static final String DRAW_ORDER_CREATE_ROUTING_KEY = "draw.order.create";
    public static final String DRAW_ORDER_CREATE_DLQ_ROUTING_KEY = "draw.order.create.dlq";

    // Experience System - Statistics Update Queue
    public static final String EXPERIENCE_STATISTICS_EXCHANGE = "experience.statistics.exchange";
    public static final String EXPERIENCE_STATISTICS_DLX_EXCHANGE = "experience.statistics.dlx.exchange";

    public static final String EXPERIENCE_STATISTICS_UPDATE_QUEUE = "experience.statistics.update.queue";
    public static final String EXPERIENCE_STATISTICS_UPDATE_DLQ = "experience.statistics.update.dlq";

    public static final String EXPERIENCE_STATISTICS_UPDATE_ROUTING_KEY = "statistics.update";
    public static final String EXPERIENCE_STATISTICS_UPDATE_DLQ_ROUTING_KEY = "statistics.update.dlq";

    // Experience System - Notification Queue
    public static final String EXPERIENCE_NOTIFICATION_EXCHANGE = "experience.notification.exchange";
    public static final String EXPERIENCE_NOTIFICATION_DLX_EXCHANGE = "experience.notification.dlx.exchange";

    public static final String EXPERIENCE_NOTIFICATION_QUEUE = "experience.notification.queue";
    public static final String EXPERIENCE_NOTIFICATION_DLQ = "experience.notification.dlq";

    public static final String EXPERIENCE_NOTIFICATION_ROUTING_KEY = "notification.send";
    public static final String EXPERIENCE_NOTIFICATION_DLQ_ROUTING_KEY = "notification.send.dlq";
}
