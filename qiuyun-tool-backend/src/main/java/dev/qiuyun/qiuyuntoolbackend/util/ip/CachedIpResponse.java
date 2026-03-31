package dev.qiuyun.qiuyuntoolbackend.util.ip;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CachedIpResponse {
    private String ip;
    private String country;
    private String region;
    private String city;
    private String isp;
    private String regionId;
    private String cityId;
    private String countryId;
    private String source;
    private Long queryTime;
}
