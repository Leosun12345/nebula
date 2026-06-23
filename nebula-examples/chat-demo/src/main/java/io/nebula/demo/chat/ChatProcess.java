package io.nebula.demo.chat;

import com.alibaba.fastjson.JSONObject;
import io.nebula.common.exception.HttpErrorCodeEnum;
import io.nebula.connect.client.WebClient;
import io.nebula.connect.model.MessageIn;
import io.nebula.connect.process.QueueProcess;
import io.nebula.connect.process.annotation.IREQ;
import io.nebula.connect.util.IoSender;

@IREQ(codes = {"CHAT"})
public class ChatProcess extends QueueProcess<JSONObject> {

    @Override
    public void init(Object... objs) {
        this.message = (MessageIn) objs[0];
        this.client = (WebClient) objs[1];
    }

    @Override
    public void process(WebClient client, JSONObject request, String code, String subCode, int sequence) {
        String msg = request.getString("msg");
        String userId = client.getUserId();

        // 广播消息给所有在线用户
        IoSender.sendMessage("CHAT", "CHAT", sequence,
            JSONObject.parseObject("{\"userId\":\"" + userId + "\",\"msg\":\"" + msg + "\"}"),
            client.getUserId());
    }
}
