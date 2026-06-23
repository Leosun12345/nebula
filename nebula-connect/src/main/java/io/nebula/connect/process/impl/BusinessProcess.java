package io.nebula.connect.process.impl;

import com.alibaba.fastjson.JSONObject;
import io.nebula.connect.client.WebClient;
import io.nebula.connect.process.QueueProcess;
import io.nebula.connect.process.annotation.IREQ;

@IREQ(codes = {"BUSINESS"})
public class BusinessProcess extends QueueProcess<JSONObject> {

    @Override
    public void process(WebClient client, JSONObject request, String code, String subCode, int sequence) {
        // 业务处理由具体的Handler实现
        // 这里只是路由到Logic层
        // 实际实现中，会通过RemoteLogic转发到游戏服
    }
}
