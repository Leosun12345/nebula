package io.nebula.connect.util;

import com.alibaba.fastjson.JSONObject;
import io.nebula.common.exception.HttpErrorCodeEnum;
import io.nebula.connect.client.WebClient;
import io.nebula.connect.manager.CacheOnline;

import java.util.concurrent.Callable;

public class IoSender {

    public static void sendMessage(String code, String subCode, int sequence,
                                   JSONObject response, String... userIds) {
        for (String userId : userIds) {
            WebClient client = CacheOnline.WebClientManager.getInstance().getWebClientByUid(userId);
            if (client != null && client.isAlive()) {
                client.nativeSend(code, subCode, sequence, response);
            }
        }
    }

    public static void sendMessage(String code, String subCode, int sequence,
                                   JSONObject response, WebClient... clients) {
        for (WebClient client : clients) {
            if (client != null && client.isAlive()) {
                client.nativeSend(code, subCode, sequence, response);
            }
        }
    }

    public static void sendMessage(Callable<?> callable, String code, String subCode,
                                   int sequence, JSONObject response, String... userIds) {
        for (String userId : userIds) {
            WebClient client = CacheOnline.WebClientManager.getInstance().getWebClientByUid(userId);
            if (client != null && client.isAlive()) {
                client.nativeSend(callable, code, subCode, sequence, response);
            }
        }
    }

    public static void broadAll(String code, String subCode, int sequence, JSONObject response) {
        for (WebClient client : CacheOnline.WebClientManager.getInstance().getClients().values()) {
            if (client.isAlive()) {
                client.nativeSend(code, subCode, sequence, response);
            }
        }
    }

    public static void sendError(WebClient client, HttpErrorCodeEnum errorCode) {
        JSONObject error = new JSONObject();
        error.put("errorCode", errorCode.getCode());
        error.put("msg", errorCode.getMessage());
        client.nativeSend("BUSINESS", "NOTIFY_ERROR", 0, error);
    }
}
