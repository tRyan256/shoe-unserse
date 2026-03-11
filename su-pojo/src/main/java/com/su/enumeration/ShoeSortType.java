package com.su.enumeration;

import lombok.Getter;

@Getter
public enum ShoeSortType {

    NEWEST("newest", "最新发布"),
    PRICE_ASC("price_asc", "价格升序"),
    PRICE_DESC("price_desc", "价格降序"),
    SALES("sales", "销量优先");

    private final String code;
    private final String desc;

    ShoeSortType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ShoeSortType fromCode(String code) {
        if (code == null) {
            return NEWEST;
        }
        for (ShoeSortType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return NEWEST;
    }
}
