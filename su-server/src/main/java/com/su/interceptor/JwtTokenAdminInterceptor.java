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
import org.springframework.web.method.HandlerMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

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
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            log.info("当前拦截到的是静态资源: {}", request.getRequestURI());
            //当前拦截到的不是动态方法，直接放行
            return true;
        }
        
        // 获取请求路径
        String requestURI = request.getRequestURI();
        log.info("当前请求路径: {}", requestURI);

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());
        log.info("从请求头中获取令牌: {}", token);
        
        // 判断token是否为空
        if (token == null || token.isEmpty()) {
            log.error("JWT令牌为空");
            // 响应401状态码
            response.setStatus(401);
            return false;
        }

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            log.info("当前员工id：{}", empId);
            BaseContext.setCurrentId(empId);
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            log.error("JWT校验失败: ", ex);
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}
