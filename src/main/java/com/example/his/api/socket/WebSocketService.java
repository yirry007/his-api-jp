package com.example.his.api.socket;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.his.api.config.sa_token.StpCustomerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint(value = "/socket")
@Component
public class WebSocketService {
    public static ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
    }

    @OnClose
    public void onClose(Session session) {
        Map map = session.getUserProperties();
        if (map.containsKey("userId")) {
            String userId = MapUtil.getStr(map, "userId");
            sessionMap.remove(userId);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        JSONObject json = JSONUtil.parseObj(message);
        // command
        String opt = json.getStr("opt");
        // identity
        String identity = json.getStr("identity");
        // token
        String token = json.getStr("token");
        String userId = null;
        if ("customer".equals(identity)) {
            userId = "customer_" + StpCustomerUtil.getLoginIdByToken(token).toString();
        } else {
            userId = "user_" + StpUtil.getLoginIdByToken(token).toString();
        }

        Map map = session.getUserProperties();
        map.put("userId", userId);

        if (sessionMap.containsKey(userId)) {
            sessionMap.replace(userId, session);
        } else {
            sessionMap.put(userId, session);
        }

        if ("ping".equals(opt)) {
            return;
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("エラー", error);
    }

    public static void sendInfo(String message, String userId) {
        if (StrUtil.isNotBlank(userId) && sessionMap.containsKey(userId)) {
            Session session = sessionMap.get(userId);
            sendMessage(message, session);
        }
    }

    private static void sendMessage(String message, Session session) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("実行エラー", e);
        }
    }
}
