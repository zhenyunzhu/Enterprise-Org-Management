package com.itheima.controller;

import com.itheima.pojo.Result;
import com.itheima.util.AliyunOSSOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UploadController {

    private final AliyunOSSOperator aliyunOSSOperator;

    /**
     * 上传文件到阿里云 OSS，返回可访问的 URL。
     *
     * <p>文件名由 OSS 工具类统一按「yyyy/MM/uuid.扩展名」规则生成，
     * 避免原始文件名冲突与非法字符问题。</p>
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            log.warn("上传失败: 文件为空");
            return Result.error("上传失败：文件为空");
        }
        log.info("上传文件, filename={}, size={}B", file.getOriginalFilename(), file.getSize());
        String url = aliyunOSSOperator.upload(file.getBytes(), file.getOriginalFilename());
        return Result.success(url);
    }

}
