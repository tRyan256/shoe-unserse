package com.su.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrdersConfirmBatchDTO implements Serializable {
    private List<Long> ids;
}

