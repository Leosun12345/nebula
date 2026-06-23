package io.nebula.common.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastConnect {
    private String uid;
    private String host;
    private String port;
    private String serviceId;
    private long lastConnectTime;
}
