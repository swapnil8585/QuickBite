package com.quickbite.common.events;

public final class KafkaTopics {
    private KafkaTopics() {}
    public static final String ORDER_PLACED         = "order-placed";
    public static final String ORDER_STATUS_UPDATED = "order-status-updated";
    public static final String PAYMENT_PROCESSED    = "payment-processed";
    public static final String DRIVER_ASSIGNED      = "driver-assigned";
    public static final String DRIVER_LOCATION      = "driver-location-updated";
}
