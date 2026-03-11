package com.su.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * DTO for address book operations (create/update)
 */
@Data
public class AddressBookDTO implements Serializable {
    private Long id;
    private String consignee;
    private String phone;
    private String sex;
    private String provinceCode;
    private String provinceName;
    private String cityCode;
    private String cityName;
    private String districtCode;
    private String districtName;
    private String detail;
    private String label;
    private Integer isDefault;
}
