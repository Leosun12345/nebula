package io.nebula.logic.remote;

import com.alibaba.fastjson.JSONObject;
import io.nebula.common.exception.BusinessException;
import io.nebula.common.exception.HttpErrorCodeEnum;
import io.nebula.common.util.AssertUtil;
import io.nebula.logic.core.room.CacheRoom;
import io.nebula.logic.core.room.Room;
import io.nebula.logic.core.service.LogicService;
import io.nebula.logic.handler.Handler;
import io.nebula.logic.handler.HandlerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Logic 层远程调用入口
 * 接收来自 Connect 层的所有 RPC 请求
 *
 * @author leo
 * @since 1.0.0
 */
@RestController
public class RemoteLogic {
    private static final Logger log = LoggerFactory.getLogger(RemoteLogic.class);

    @Autowired
    private LogicService logicService;

    @Autowired
    private CacheRoom cacheRoom;

    // ==================== 消息转发 ====================

    /**
     * 转发业务消息到对应的 Handler
     */
    @PostMapping("/api/c2g/dispatch-msg")
    public DispatchResponse dispatchMsg(@RequestBody DispatchRequest request) {
        log.info("Dispatch message: userId={}, subCode={}, roomId={}",
            request.userId, request.subCode, request.roomId);

        try {
            // 1. 获取房间
            Room room = cacheRoom.getRoomById(request.roomId);
            AssertUtil.notNull(room, HttpErrorCodeEnum.ROOM_NOT_EXIST);

            // 2. 获取用户信息 (实际应从缓存中获取)
            Object user = getUser(request.userId);

            // 3. 查找 Handler
            Object handler = HandlerManager.getInstance().getHandler(request.subCode);
            AssertUtil.notNull(handler, HttpErrorCodeEnum.UNKNOWN_ERROR, "Handler not found: " + request.subCode);

            // 4. 执行业务逻辑
            if (handler instanceof Handler) {
                Handler h = (Handler) handler;
                h.init(room, user, request.sequence, request.params);
                h.run();
            }

            return DispatchResponse.success();

        } catch (BusinessException e) {
            log.warn("Dispatch failed: {}", e.getMessage());
            return DispatchResponse.fail(e.getErrorCode().getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Dispatch error", e);
            return DispatchResponse.fail(HttpErrorCodeEnum.UNKNOWN_ERROR.getCode(), e.getMessage());
        }
    }

    // ==================== 房间管理 ====================

    /**
     * 创建房间
     */
    @PostMapping("/api/c2g/room-create")
    public CreateRoomResponse roomCreate(@RequestBody CreateRoomRequest request) {
        log.info("Create room: userId={}, gameId={}, configId={}",
            request.userId, request.gameId, request.configId);

        try {
            // 1. 校验参数
            AssertUtil.notEmpty(request.roomId, HttpErrorCodeEnum.PARAM_ERROR, "roomId is required");
            AssertUtil.notEmpty(request.gameId, HttpErrorCodeEnum.PARAM_ERROR, "gameId is required");

            // 2. 创建房间
            Room room = cacheRoom.roomCreate(request.roomId, request.gameId);

            // 3. 设置房间配置
            room.setConfigId(request.configId);
            room.setMaxPlayer(request.maxPlayer != null ? request.maxPlayer : 6);

            // 4. 加入房间
            Object user = getUser(request.userId);
            logicService.joinRoom(room, user);

            return CreateRoomResponse.success(room.getRoomId());

        } catch (BusinessException e) {
            log.warn("Create room failed: {}", e.getMessage());
            return CreateRoomResponse.fail(e.getErrorCode().getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Create room error", e);
            return CreateRoomResponse.fail(HttpErrorCodeEnum.UNKNOWN_ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 加入房间
     */
    @PostMapping("/api/c2g/room-join")
    public JoinRoomResponse roomJoin(@RequestBody JoinRoomRequest request) {
        log.info("Join room: userId={}, roomId={}", request.userId, request.roomId);

        try {
            // 1. 获取房间
            Room room = cacheRoom.getRoomById(request.roomId);
            AssertUtil.notNull(room, HttpErrorCodeEnum.ROOM_NOT_EXIST);

            // 2. 加入房间
            Object user = getUser(request.userId);
            logicService.joinRoom(room, user);

            return JoinRoomResponse.success(room.getRoomId());

        } catch (BusinessException e) {
            log.warn("Join room failed: {}", e.getMessage());
            return JoinRoomResponse.fail(e.getErrorCode().getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Join room error", e);
            return JoinRoomResponse.fail(HttpErrorCodeEnum.UNKNOWN_ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 快速加入房间 (自动分配房间)
     */
    @PostMapping("/api/c2g/room-quick-join")
    public QuickJoinResponse roomQuickJoin(@RequestBody QuickJoinRequest request) {
        log.info("Quick join: userId={}, gameId={}", request.userId, request.gameId);

        try {
            // 1. 查找可用房间
            Room room = cacheRoom.findAvailableRoom(request.gameId);
            if (room == null) {
                // 没有可用房间，创建新房间
                room = cacheRoom.roomCreate(generateRoomId(), request.gameId);
                room.setMaxPlayer(request.maxPlayer != null ? request.maxPlayer : 6);
            }

            // 2. 加入房间
            Object user = getUser(request.userId);
            logicService.joinRoom(room, user);

            return QuickJoinResponse.success(room.getRoomId());

        } catch (BusinessException e) {
            log.warn("Quick join failed: {}", e.getMessage());
            return QuickJoinResponse.fail(e.getErrorCode().getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Quick join error", e);
            return QuickJoinResponse.fail(HttpErrorCodeEnum.UNKNOWN_ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * 检查房间是否存在
     */
    @PostMapping("/api/c2g/room-exists")
    public RoomExistsResponse roomExists(@RequestBody RoomExistsRequest request) {
        log.debug("Check room exists: roomId={}", request.roomId);

        try {
            Room room = cacheRoom.getRoomById(request.roomId);
            boolean exists = room != null;
            return RoomExistsResponse.success(exists);
        } catch (Exception e) {
            log.error("Room exists error", e);
            return RoomExistsResponse.fail(e.getMessage());
        }
    }

    /**
     * 获取房间详情
     */
    @PostMapping("/api/c2g/room-detail")
    public RoomDetailResponse roomDetail(@RequestBody RoomDetailRequest request) {
        log.info("Room detail: roomId={}", request.roomId);

        try {
            Room room = cacheRoom.getRoomById(request.roomId);
            AssertUtil.notNull(room, HttpErrorCodeEnum.ROOM_NOT_EXIST);

            return RoomDetailResponse.success(room);

        } catch (BusinessException e) {
            return RoomDetailResponse.fail(e.getErrorCode().getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Room detail error", e);
            return RoomDetailResponse.fail(HttpErrorCodeEnum.UNKNOWN_ERROR.getCode(), e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    private Object getUser(String userId) {
        // TODO: 从缓存或数据库获取用户信息
        return userId;
    }

    private String generateRoomId() {
        return "ROOM_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }

    // ==================== Request/Response 内部类 ====================

    // ---- Dispatch ----

    public static class DispatchRequest {
        public String userId;
        public String roomId;
        public String code;
        public String subCode;
        public int sequence;
        public String params;

        public JSONObject getParams() {
            try {
                return JSONObject.parseObject(params);
            } catch (Exception e) {
                return new JSONObject();
            }
        }
    }

    public static class DispatchResponse {
        public int code;
        public String message;
        public String data;

        public static DispatchResponse success() {
            DispatchResponse r = new DispatchResponse();
            r.code = HttpErrorCodeEnum.OK.getCode();
            r.message = HttpErrorCodeEnum.OK.getMessage();
            return r;
        }

        public static DispatchResponse fail(int code, String message) {
            DispatchResponse r = new DispatchResponse();
            r.code = code;
            r.message = message;
            return r;
        }
    }

    // ---- Room Create ----

    public static class CreateRoomRequest {
        public String userId;
        public String roomId;
        public String gameId;
        public Integer configId;
        public Integer maxPlayer;
    }

    public static class CreateRoomResponse {
        public int code;
        public String message;
        public String roomId;

        public static CreateRoomResponse success(String roomId) {
            CreateRoomResponse r = new CreateRoomResponse();
            r.code = HttpErrorCodeEnum.OK.getCode();
            r.message = HttpErrorCodeEnum.OK.getMessage();
            r.roomId = roomId;
            return r;
        }

        public static CreateRoomResponse fail(int code, String message) {
            CreateRoomResponse r = new CreateRoomResponse();
            r.code = code;
            r.message = message;
            return r;
        }
    }

    // ---- Room Join ----

    public static class JoinRoomRequest {
        public String userId;
        public String roomId;
    }

    public static class JoinRoomResponse {
        public int code;
        public String message;
        public String roomId;

        public static JoinRoomResponse success(String roomId) {
            JoinRoomResponse r = new JoinRoomResponse();
            r.code = HttpErrorCodeEnum.OK.getCode();
            r.message = HttpErrorCodeEnum.OK.getMessage();
            r.roomId = roomId;
            return r;
        }

        public static JoinRoomResponse fail(int code, String message) {
            JoinRoomResponse r = new JoinRoomResponse();
            r.code = code;
            r.message = message;
            return r;
        }
    }

    // ---- Quick Join ----

    public static class QuickJoinRequest {
        public String userId;
        public String gameId;
        public Integer maxPlayer;
    }

    public static class QuickJoinResponse {
        public int code;
        public String message;
        public String roomId;

        public static QuickJoinResponse success(String roomId) {
            QuickJoinResponse r = new QuickJoinResponse();
            r.code = HttpErrorCodeEnum.OK.getCode();
            r.message = HttpErrorCodeEnum.OK.getMessage();
            r.roomId = roomId;
            return r;
        }

        public static QuickJoinResponse fail(int code, String message) {
            QuickJoinResponse r = new QuickJoinResponse();
            r.code = code;
            r.message = message;
            return r;
        }
    }

    // ---- Room Exists ----

    public static class RoomExistsRequest {
        public String roomId;
    }

    public static class RoomExistsResponse {
        public int code;
        public String message;
        public boolean exists;

        public static RoomExistsResponse success(boolean exists) {
            RoomExistsResponse r = new RoomExistsResponse();
            r.code = HttpErrorCodeEnum.OK.getCode();
            r.message = HttpErrorCodeEnum.OK.getMessage();
            r.exists = exists;
            return r;
        }

        public static RoomExistsResponse fail(String message) {
            RoomExistsResponse r = new RoomExistsResponse();
            r.code = HttpErrorCodeEnum.UNKNOWN_ERROR.getCode();
            r.message = message;
            return r;
        }
    }

    // ---- Room Detail ----

    public static class RoomDetailRequest {
        public String roomId;
    }

    public static class RoomDetailResponse {
        public int code;
        public String message;
        public Room room;

        public static RoomDetailResponse success(Room room) {
            RoomDetailResponse r = new RoomDetailResponse();
            r.code = HttpErrorCodeEnum.OK.getCode();
            r.message = HttpErrorCodeEnum.OK.getMessage();
            r.room = room;
            return r;
        }

        public static RoomDetailResponse fail(int code, String message) {
            RoomDetailResponse r = new RoomDetailResponse();
            r.code = code;
            r.message = message;
            return r;
        }
    }
}
