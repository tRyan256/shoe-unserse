package com.su.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserOrderStatisticsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer toBePaid;

    private Integer toBeShipped;

    private Integer toBeReceived;

    private Integer toBeReviewed;
}
