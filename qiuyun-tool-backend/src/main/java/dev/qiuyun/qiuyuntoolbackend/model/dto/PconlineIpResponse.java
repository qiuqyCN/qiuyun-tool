package dev.qiuyun.qiuyuntoolbackend.model.dto;

import lombok.Data;

@Data
public class PconlineIpResponse {
    private String ip;
    private String pro;
    private String proCode;
    private String city;
    private String cityCode;
    private String region;
    private String regionCode;
    private String addr;
    private String regionNames;
    private String err;
}
