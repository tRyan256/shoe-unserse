package com.su.test;

import com.su.context.BaseContext;
import com.su.interceptor.JwtTokenAdminInterceptor;
import com.su.interceptor.JwtTokenUserInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class InterceptorContextCleanupTest {
    @AfterEach
    void cleanup() {
        BaseContext.removeCurrentId();
    }

    @Test
    void userInterceptor_afterCompletion_clearsBaseContext() throws Exception {
        BaseContext.setCurrentId(1L);
        JwtTokenUserInterceptor interceptor = new JwtTokenUserInterceptor();
        interceptor.afterCompletion(null, null, null, null);
        assertNull(BaseContext.getCurrentId());
    }

    @Test
    void adminInterceptor_afterCompletion_clearsBaseContext() throws Exception {
        BaseContext.setCurrentId(2L);
        JwtTokenAdminInterceptor interceptor = new JwtTokenAdminInterceptor();
        interceptor.afterCompletion(null, null, null, null);
        assertNull(BaseContext.getCurrentId());
    }
}

