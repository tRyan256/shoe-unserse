package com.su.controller.admin;


import com.su.constant.MessageConstant;
import com.su.result.Result;
import com.su.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("/admin/common")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    /**
     * 文件上传
     * @param file
     * @return
     */
    @RequestMapping("/upload")
    public Result<String> upload(MultipartFile file)  {
        log.info("文件上传：{}",file);
        try {
            String originalFilename = file.getOriginalFilename();
            String url = null;
            if (originalFilename != null) {
                url = aliOssUtil.upload(file.getInputStream(),
                        originalFilename);
            }
            if (url == null || url.isBlank()) {
                return Result.error(MessageConstant.UPLOAD_FAILED);
            }
            return Result.success(url);
        } catch (Exception e) {
            log.error("文件上传失败：{}", e);
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
