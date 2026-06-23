package io.nebula.connect.util;

import jakarta.websocket.Session;

import java.io.IOException;

public class SessionUtil {
    public static void closeSession(Session session) {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException ignore) {
                // ignore
            }
        }
    }
}
