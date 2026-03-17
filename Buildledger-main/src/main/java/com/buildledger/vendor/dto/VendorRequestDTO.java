package com.buildledger.vendor.dto;

import com.buildledger.vendor.enums.VendorCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorRequestDTO {

    private String name;
    private String contactInfo;
    private VendorCategory category;
}
