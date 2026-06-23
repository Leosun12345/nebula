package io.nebula.logic.core.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 房间节点信息 (存储在Redis)
 * 节点唯一ID = roomId (房间ID = 地图ID)
 *
 * @author leo
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomNode {

    /** 房间ID (地图ID) */
    private String roomId;

    /** 服务名称 */
    private String serviceId;

    /** 节点IP */
    private String host;

    /** 节点端口 */
    private String port;

    /** 当前玩家数量 */
    @Builder.Default
    private int playerCount = 0;

    /** 最后心跳时间 */
    @Builder.Default
    private long lastHeartbeat = System.currentTimeMillis();

    /** 房间状态: 0-初始化 1-运行中 2-已关闭 */
    @Builder.Default
    private int status = 1;

    public String getRedisKey() {
        return "room:node:" + roomId;
    }

    public String getAddress() {
        return host + ":" + port;
    }

    public void heartbeat() {
        this.lastHeartbeat = System.currentTimeMillis();
    }

    public boolean isTimeout(long timeoutMs) {
        return System.currentTimeMillis() - lastHeartbeat > timeoutMs;
    }
}
