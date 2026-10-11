package com.itheima.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * JWT 令牌工具类。
 *
 * <p>签名密钥与有效期通过 {@code application.yml} 的 {@code tlias.jwt.*} 注入，
 * 不硬编码在代码中。由于本类对外暴露静态方法，这里借助 Spring 的 setter 注入
 * 把配置写入静态字段，兼顾「工具类静态调用」与「配置外置」。</p>
 */
@Component
public class JwtUtils {

    /** 签名密钥（Base64 编码），对应 tlias.jwt.sign-key */
    private static String signKey;

    /** 令牌有效期，单位毫秒，对应 tlias.jwt.expire */
    private static Long expire;

    @Value("${tlias.jwt.sign-key}")
    public void setSignKey(String signKey) {
        JwtUtils.signKey = signKey;
    }

    @Value("${tlias.jwt.expire}")
    public void setExpire(Long expire) {
        JwtUtils.expire = expire;
    }

    /**
     * 生成 JWT 令牌
     *
     * @param claims 负载数据，登录场景至少包含 id 与 username
     * @return 签名后的 JWT 字符串
     */
    public static String generateJwt(Map<String, Object> claims) {
        return Jwts.builder()
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS256, signKey)
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .compact();
    }

    /**
     * 解析并校验 JWT 令牌
     *
     * @param jwt 令牌字符串
     * @return 负载内容
     * @throws io.jsonwebtoken.JwtException 签名不合法或令牌已过期时抛出
     */
    public static Claims parseJWT(String jwt) {
        return Jwts.parser()
                .setSigningKey(signKey)
                .parseClaimsJws(jwt)
                .getBody();
    }

}
