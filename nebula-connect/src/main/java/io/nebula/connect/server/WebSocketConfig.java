package io.nebula.connect.server;


import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class WebSocketConfig extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        // 可获取客户端IP等信息
    }

    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    @Override
    public boolean checkOrigin(String originHeaderValue) {
        return true;
    }
}
