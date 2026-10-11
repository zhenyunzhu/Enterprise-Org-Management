package com.itheima.interceptor;

import com.itheima.pojo.Result;
import com.itheima.util.CurrentHolder;
import com.itheima.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

/**
 * 登录校验拦截器。
 *
 * <p>拦截所有业务接口，从请求头 {@code token} 中解析 JWT：校验通过则把员工 ID
 * 写入 {@link CurrentHolder}，供 AOP 操作日志使用；校验失败统一返回
 * HTTP 401 + {@code Result.error("NOT_LOGIN")}。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    /** 前端携带令牌的请求头名称 */
    public static final String TOKEN_HEADER = "token";

    /** JWT 负载中存放员工 ID 的声明名 */
    private static final String CLAIM_ID = "id";

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 CORS 预检请求，否则跨域场景下所有请求都会失败
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        // 仅拦截 Controller 方法；静态资源、错误页等直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String jwt = request.getHeader(TOKEN_HEADER);
        if (!StringUtils.hasText(jwt)) {
            log.info("请求 {} 未携带 token, 拦截", request.getRequestURI());
            return writeNotLogin(response);
        }

        try {
            Claims claims = JwtUtils.parseJWT(jwt);
            Object id = claims.get(CLAIM_ID);
            if (id == null) {
                log.warn("token 负载缺少 {} 声明, 拦截", CLAIM_ID);
                return writeNotLogin(response);
            }
            CurrentHolder.setCurrentId(Integer.valueOf(id.toString()));
            return true;
        } catch (Exception e) {
            log.info("token 校验失败({}), 拦截请求 {}", e.getMessage(), request.getRequestURI());
            return writeNotLogin(response);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 请求结束必须清理 ThreadLocal，避免线程池复用导致操作人串号
        CurrentHolder.remove();
    }

    /**
     * 输出未登录响应体，并中断后续处理。
     */
    private boolean writeNotLogin(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(Result.NOT_LOGIN)));
        return false;
    }

}
