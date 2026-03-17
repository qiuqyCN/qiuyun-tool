package dev.qiuyun.qiuyuntoolbackend.model.dto;

import lombok.Data;

@Data
public class TaobaoIpResponse {
    private int code;
    private String msg;
    private TaobaoIpData data;
}
