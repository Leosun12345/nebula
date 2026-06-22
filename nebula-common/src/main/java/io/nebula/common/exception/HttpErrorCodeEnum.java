package io.nebula.common.exception;

public enum HttpErrorCodeEnum {
    OK(0, "Success"),
    UNKNOWN_ERROR(9999, "Unknown error"),
    PARAM_ERROR(10001, "Parameter error"),
    USER_NOT_LOGIN(10002, "User not login"),
    USER_NOT_ONLINE(10003, "User not online"),
    INSUFFICIENT_GOLD_COINS(10004, "Insufficient gold coins"),
    ROOM_NOT_EXIST(10005, "Room not exist"),
    ROOM_IS_FULL(10006, "Room is full"),
    ROOM_IS_GAMING(10007, "Room is gaming"),
    OPERATION_FREQUENTLY(10008, "Operation too frequently"),
    GAME_NOT_OPEN(10009, "Game not open"),
    NOT_IN_ROOM(10010, "User not in room"),
    ;

    private final int code;
    private final String message;

    HttpErrorCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
