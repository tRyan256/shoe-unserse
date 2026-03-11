package com.su.service.impl;

import com.su.exception.ImageUploadException;
import com.su.exception.ImageValidationException;
import com.su.service.ExperienceImageService;
import com.su.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 体验心得图片服务实现类
 */
@Service
@Slf4j
public class ExperienceImageServiceImpl implements ExperienceImageService {

    @Autowired
    private AliOssUtil aliOssUtil;

    // 支持的图片格式
    private static final List<String> ALLOWED_FORMATS = Arrays.asList("jpg", "jpeg", "png", "webp");
    
    // 单张图片最大大小：10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    // 单个心得最大图片数量
    private static final int MAX_IMAGE_COUNT = 9;

    /**
     * 上传单张图片
     *
     * @param file 图片文件
     * @return 图片存储路径
     * @throws ImageUploadException 图片上传失败
     */
    @Override
    public String uploadImage(MultipartFile file) throws ImageUploadException {
        // 验证图片
        validateImage(file);
        
        try {
            // 上传到OSS
            String originalFilename = file.getOriginalFilename();
            String filePath = aliOssUtil.upload(file.getInputStream(), originalFilename);
            log.info("图片上传成功: {}", filePath);
            return filePath;
        } catch (IOException e) {
            log.error("读取图片文件失败", e);
            throw new ImageUploadException("图片上传失败，请稍后重试");
        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new ImageUploadException("图片上传失败，请稍后重试");
        }
    }

    /**
     * 批量上传图片
     *
     * @param files 图片文件列表
     * @return 图片存储路径列表
     * @throws ImageUploadException 图片上传失败
     */
    @Override
    public List<String> uploadImages(List<MultipartFile> files) throws ImageUploadException {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 验证图片数量
        if (files.size() > MAX_IMAGE_COUNT) {
            throw new ImageValidationException("图片数量不能超过" + MAX_IMAGE_COUNT + "张");
        }
        
        List<String> imagePaths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String path = uploadImage(file);
                imagePaths.add(path);
            }
        }
        
        return imagePaths;
    }

    /**
     * 验证图片
     *
     * @param file 图片文件
     * @throws ImageValidationException 图片验证失败
     */
    @Override
    public void validateImage(MultipartFile file) throws ImageValidationException {
        if (file == null || file.isEmpty()) {
            throw new ImageValidationException("图片文件不能为空");
        }
        
        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ImageValidationException("图片大小超过限制");
        }
        
        // 验证文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new ImageValidationException("图片文件名不能为空");
        }
        
        String fileExtension = getFileExtension(originalFilename);
        if (fileExtension == null || !ALLOWED_FORMATS.contains(fileExtension.toLowerCase())) {
            throw new ImageValidationException("不支持的图片格式");
        }
        
        // 验证Content-Type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageValidationException("不支持的图片格式");
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（小写，不含点）
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        
        return null;
    }
}
