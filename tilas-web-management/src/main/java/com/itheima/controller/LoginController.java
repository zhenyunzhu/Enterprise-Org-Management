package com.itheima.controller;

import com.itheima.pojo.Emp;
import com.itheima.pojo.LoginInfo;
import com.itheima.pojo.Result;
import com.itheima.service.EmpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录认证
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final EmpService empService;

    /**
     * 员工登录，校验通过后签发 JWT 令牌。
     *
     * <p>该接口在 {@code WebConfig} 中已被拦截器放行，无需携带 token。</p>
     */
    @PostMapping("/login")
    public Result login(@RequestBody Emp emp) {
        log.info("员工登录, username={}", emp.getUsername());
        LoginInfo loginInfo = empService.login(emp);
        if (loginInfo != null) {
            return Result.success(loginInfo);
        }
        return Result.error("用户名或密码错误~");
    }

}
