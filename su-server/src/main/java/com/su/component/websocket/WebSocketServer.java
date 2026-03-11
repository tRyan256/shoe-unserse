package com.su.component.websocket;

import com.su.constant.JwtClaimsConstant;
import com.su.properties.JwtProperties;
import com.su.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务
 */
@Component
@ServerEndpoint(value = "/ws/{sid}", configurator = SpringEndpointConfigurator.class)
@Slf4j
public class WebSocketServer {

    //存放会话对象
    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    private static final Map<Long, Set<String>> spuSubscriberMap = new ConcurrentHashMap<>();
    private static final Map<String, Set<Long>> sidSubscriptions = new ConcurrentHashMap<>();

    private final JwtProperties jwtProperties;

    public WebSocketServer(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        Long sidUserId = parseLongSafe(sid);
        Long tokenUserId = resolveUserIdFromToken(session);

        // 详细的错误处理和提示
        if (sidUserId == null) {
            log.warn("WebSocket连接失败：sid格式无效，sid={}", sid);
            closeSession(session, "INVALID_SID", "用户ID格式无效");
            return;
        }

        if (tokenUserId == null) {
            log.warn("WebSocket连接失败：token无效或缺失，sid={}", sid);
            closeSession(session, "INVALID_TOKEN", "身份验证失败，请重新登录");
            return;
        }

        if (!sidUserId.equals(tokenUserId)) {
            log.warn("WebSocket连接失败：sid与token不匹配，sid={}, tokenUserId={}", sid, tokenUserId);
            closeSession(session, "USER_MISMATCH", "用户身份不匹配");
            return;
        }

        log.info("WebSocket连接建立，sid={}", sid);
        sessionMap.put(sid, session);
    }

    private void closeSession(Session session, String code, String reason) {
        try {
            session.close(new CloseReason(
                CloseReason.CloseCodes.VIOLATED_POLICY,
                code + ":" + reason
            ));
        } catch (Exception e) {
            log.warn("关闭WebSocket会话失败", e);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.info("收到WebSocket消息，sid={}, message={}", sid, message);
        if (message == null) {
            return;
        }
        String msg = message.trim();
        if (msg.regionMatches(true, 0, "SUB:", 0, 4)) {
            Long spuId = parseLongSafe(msg.substring(4));
            if (spuId != null) {
                spuSubscriberMap.computeIfAbsent(spuId, k -> ConcurrentHashMap.newKeySet()).add(sid);
                sidSubscriptions.computeIfAbsent(sid, k -> ConcurrentHashMap.newKeySet()).add(spuId);
            }
        } else if (msg.regionMatches(true, 0, "UNSUB:", 0, 6)) {
            Long spuId = parseLongSafe(msg.substring(6));
            if (spuId != null) {
                Set<String> sids = spuSubscriberMap.get(spuId);
                if (sids != null) {
                    sids.remove(sid);
                }
                Set<Long> ids = sidSubscriptions.get(sid);
                if (ids != null) {
                    ids.remove(spuId);
                }
            }
        }
    }

    /**
     * 连接关闭调用的方法
     *
     * @param sid
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        log.info("WebSocket连接断开，sid={}", sid);
        sessionMap.remove(sid);
        Set<Long> spuIds = sidSubscriptions.remove(sid);
        if (spuIds != null && !spuIds.isEmpty()) {
            for (Long spuId : spuIds) {
                Set<String> sids = spuSubscriberMap.get(spuId);
                if (sids != null) {
                    sids.remove(sid);
                }
            }
        }
    }

    /**
     * 连接错误时调用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.warn("WebSocket发生错误，sessionId={}", session == null ? null : session.getId(), error);
    }

    /**
     * 群发
     *
     * @param message
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                } else {
                    String sid = findSidBySession(session);
                    if (sid != null) {
                        cleanupSession(sid);
                    }
                }
            } catch (Exception e) {
                log.warn("WebSocket群发失败，sessionId={}", session == null ? null : session.getId(), e);
                String sid = findSidBySession(session);
                if (sid != null) {
                    cleanupSession(sid);
                }
            }
        }
    }

    private String findSidBySession(Session session) {
        if (session == null) {
            return null;
        }
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            if (session.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void cleanupSession(String sid) {
        if (sid == null) {
            return;
        }
        sessionMap.remove(sid);
        Set<Long> spuIds = sidSubscriptions.remove(sid);
        if (spuIds != null && !spuIds.isEmpty()) {
            for (Long spuId : spuIds) {
                Set<String> sids = spuSubscriberMap.get(spuId);
                if (sids != null) {
                    sids.remove(sid);
                }
            }
        }
    }

    public void sendToSpuSubscribers(Long spuId, String message) {
        if (spuId == null) {
            sendToAllClient(message);
            return;
        }
        Set<String> sids = spuSubscriberMap.get(spuId);
        if (sids == null || sids.isEmpty()) {
            sendToAllClient(message);
            return;
        }
        for (String sid : sids) {
            sendToClient(sid, message);
        }
    }

    /**
     * 发送给指定客户端
     * @param sid 客户端ID
     * @param message 消息内容
     */
    public void sendToClient(String sid, String message) {
        Session session = sessionMap.get(sid);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.warn("WebSocket发送失败，sid={}, sessionId={}", sid, session.getId(), e);
            }
        }
    }

    /**
     * 检查客户端是否在线
     * @param sid 客户端ID
     * @return true表示在线，false表示离线
     */
    public boolean isOnline(String sid) {
        Session session = sessionMap.get(sid);
        return session != null && session.isOpen();
    }

    private Long parseLongSafe(String s) {
        if (s == null) {
            return null;
        }
        try {
            return Long.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Long resolveUserIdFromToken(Session session) {
        if (session == null || jwtProperties == null || jwtProperties.getUserSecretKey() == null) {
            return null;
        }
        Map<String, java.util.List<String>> params = session.getRequestParameterMap();
        java.util.List<String> tokens = params == null ? null : params.get("token");
        String token = (tokens == null || tokens.isEmpty()) ? null : tokens.get(0);
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Object userId = claims.get(JwtClaimsConstant.USER_ID);
            if (userId == null) {
                return null;
            }
            return Long.valueOf(String.valueOf(userId));
        } catch (Exception e) {
            return null;
        }
    }
}
