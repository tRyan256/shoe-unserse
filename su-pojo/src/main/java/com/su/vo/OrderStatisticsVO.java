package com.su.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class OrderStatisticsVO implements Serializable {
    //寰呮帴鍗曟暟閲?
    private Integer toBeConfirmed;

    //寰呮淳閫佹暟閲?
    private Integer confirmed;

    //娲鹃€佷腑鏁伴噺
    private Integer deliveryInProgress;
}
