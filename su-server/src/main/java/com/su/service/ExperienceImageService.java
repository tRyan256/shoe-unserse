package com.su.service;

import com.su.exception.ImageUploadException;
import com.su.exception.ImageValidationException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 体验心得图片服务接口
 */
public interface ExperienceImageService {

    /**
     * 上传单张图片
     *
     * @param file 图片文件
     * @return 图片存储路径
     * @throws ImageUploadException 图片上传失败
     */
    String uploadImage(MultipartFile file) throws ImageUploadException;

    /**
     * 批量上传图片
     *
     * @param files 图片文件列表
     * @return 图片存储路径列表
     * @throws ImageUploadException 图片上传失败
     */
    List<String> uploadImages(List<MultipartFile> files) throws ImageUploadException;

    /**
     * 验证图片
     *
     * @param file 图片文件
     * @throws ImageValidationException 图片验证失败
     */
    void validateImage(MultipartFile file) throws ImageValidationException;
}
