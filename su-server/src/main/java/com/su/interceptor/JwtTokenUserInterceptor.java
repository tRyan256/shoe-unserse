package com.su.interceptor;


import com.su.constant.JwtClaimsConstant;
import com.su.context.BaseContext;
import com.su.properties.JwtProperties;
import com.su.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    private static final List<String> PUBLIC_GET_PATHS = Arrays.asList(
        "/user/experience/posts",
        "/user/experience/posts/all"
    );

    private boolean isPublicGetPath(String uri) {
        if (PUBLIC_GET_PATHS.contains(uri)) {
            return true;
        }
        if (uri.matches("/user/experience/posts/\\d+$")) return true;
        if (uri.matches("/user/experience/posts/\\d+/likes")) return true;
        if (uri.matches("/user/experience/posts/\\d+/comments")) return true;
        if (uri.equals("/user/experience/comments")) return true;
        return false;
    }

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            log.info("当前拦截到的是静态资源: {}", request.getRequestURI());
            return true;
        }

        String uri = request.getRequestURI();
        String method = request.getMethod();
        boolean isPublicPath = "GET".equals(method) && isPublicGetPath(uri);

        String token = request.getHeader(jwtProperties.getUserTokenName());
        log.info("从请求头中获取令牌: {}, uri: {}, isPublicPath: {}", token, uri, isPublicPath);
        
        if (token == null || token.isEmpty()) {
            if (isPublicPath) {
                log.info("公开接口无token访问: {}", uri);
                return true;
            }
            log.error("JWT令牌为空");
            response.setStatus(401);
            return false;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户id：{}", userId);
            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception ex) {
            if (isPublicPath) {
                log.warn("公开接口token无效，继续访问: {}", uri);
                return true;
            }
            log.error("JWT校验失败: ", ex);
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}
