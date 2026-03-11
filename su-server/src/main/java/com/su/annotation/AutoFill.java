package com.su.annotation;

import com.su.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 鑷畾涔夋敞瑙ｏ紝鐢ㄤ簬鏍囪瘑闇€瑕佽繘琛屽叕鍏卞瓧娈佃嚜鍔ㄥ～鍏呯殑灞炴€?
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //娉ㄨВ鍙傛暟
    OperationType value();
}
