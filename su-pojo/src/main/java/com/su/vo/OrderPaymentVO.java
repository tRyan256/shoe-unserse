package com.su.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentVO implements Serializable {

    private String nonceStr; //闅忔満瀛楃涓?
    private String paySign; //绛惧悕
    private String timeStamp; //鏃堕棿鎴?
    private String signType; //绛惧悕绠楁硶
    private String packageStr; //缁熶竴涓嬪崟鎺ュ彛杩斿洖鐨?prepay_id 鍙傛暟鍊?

}
