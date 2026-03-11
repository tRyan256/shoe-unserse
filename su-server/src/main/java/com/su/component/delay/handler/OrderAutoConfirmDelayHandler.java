package com.su.component.delay.handler;

import com.su.component.delay.DelayTaskHandler;
import com.su.constant.DelayTaskTypeConstant;
import com.su.service.OrderAutoConfirmService;
import org.springframework.stereotype.Component;

@Component
public class OrderAutoConfirmDelayHandler implements DelayTaskHandler {
    private final OrderAutoConfirmService orderAutoConfirmService;

    public OrderAutoConfirmDelayHandler(
            OrderAutoConfirmService orderAutoConfirmService
    ) {
        this.orderAutoConfirmService = orderAutoConfirmService;
    }

    @Override
    public boolean supports(String type) {
        return DelayTaskTypeConstant.ORDER_AUTO_CONFIRM.equals(type);
    }

    @Override
    public void handle(String taskId, String payloadJson) {
        String orderNumber = bizKey(taskId);
        orderAutoConfirmService.autoConfirmByOrderNumber(orderNumber, "系统自动确认收货", "系统");
    }

    private String bizKey(String taskId) {
        int idx = taskId == null ? -1 : taskId.indexOf(':');
        return idx < 0 ? taskId : taskId.substring(idx + 1);
    }
}
