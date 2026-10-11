package com.itheima.pojo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置项，对应 {@code application.yml} 中的 {@code aliyun.oss.*}。
 *
 * <p>所有字段均由配置文件注入，Java 代码中不出现任何硬编码的地址与凭证。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOSSProperties {

    /** OSS 访问域名，例如 https://oss-cn-beijing.aliyuncs.com */
    private String endpoint;

    /** Bucket 名称 */
    private String bucketName;

    /** Bucket 所在地域，例如 cn-beijing */
    private String region;

    /**
     * 访问密钥 ID。留空时回退读取环境变量 {@code OSS_ACCESS_KEY_ID}，
     * 生产环境推荐使用环境变量或 RAM 角色，避免密钥落入配置文件。
     */
    private String accessKeyId;

    /** 访问密钥 Secret，留空时回退读取环境变量 {@code OSS_ACCESS_KEY_SECRET} */
    private String accessKeySecret;

}
