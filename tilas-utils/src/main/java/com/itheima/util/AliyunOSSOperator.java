package com.itheima.util;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.itheima.pojo.AliyunOSSProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云 OSS 文件上传工具。
 *
 * <p>Endpoint、Bucket、Region 从配置读取；访问凭证优先取配置，未配置时回退到
 * 环境变量 {@code OSS_ACCESS_KEY_ID} / {@code OSS_ACCESS_KEY_SECRET}。</p>
 */
@Component
public class AliyunOSSOperator {

    /** 按「年/月」分目录存放，便于后期归档与生命周期管理 */
    private static final DateTimeFormatter DIR_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");

    private final AliyunOSSProperties aliyunOSSProperties;

    public AliyunOSSOperator(AliyunOSSProperties aliyunOSSProperties) {
        this.aliyunOSSProperties = aliyunOSSProperties;
    }

    /**
     * 上传文件到 OSS
     *
     * @param content          文件字节内容
     * @param originalFilename 原始文件名，用于提取扩展名
     * @return 文件的可公开访问 URL
     * @throws Exception 上传过程中的网络或鉴权异常
     */
    public String upload(byte[] content, String originalFilename) throws Exception {
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();

        String dir = LocalDate.now().format(DIR_FORMATTER);
        String suffix = extractSuffix(originalFilename);
        String objectName = dir + "/" + UUID.randomUUID() + suffix;

        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(resolveCredentialsProvider())
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
        } finally {
            ossClient.shutdown();
        }

        return endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + objectName;
    }

    /**
     * 解析访问凭证：优先使用配置中的 AK/SK，未配置则回退环境变量。
     */
    private CredentialsProvider resolveCredentialsProvider() throws Exception {
        String accessKeyId = aliyunOSSProperties.getAccessKeyId();
        String accessKeySecret = aliyunOSSProperties.getAccessKeySecret();
        if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(accessKeySecret)) {
            return new DefaultCredentialProvider(accessKeyId, accessKeySecret);
        }
        return CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
    }

    /**
     * 提取文件扩展名（含点号），无扩展名时返回空串。
     */
    private String extractSuffix(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        return dotIndex < 0 ? "" : originalFilename.substring(dotIndex);
    }

}
