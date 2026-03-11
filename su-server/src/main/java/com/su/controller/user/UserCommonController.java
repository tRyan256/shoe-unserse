package com.su.controller.user;

import com.su.result.Result;
import com.su.service.ExperienceImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user/common")
@Slf4j
public class UserCommonController {

    @Autowired
    private ExperienceImageService experienceImageService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        log.info("用户上传图片，fileName={}, size={}", file.getOriginalFilename(), file.getSize());
        try {
            String url = experienceImageService.uploadImage(file);
            return Result.success(url);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error(e.getMessage());
        }
    }
}
