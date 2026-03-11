package com.su.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {


    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 鏂囦欢涓婁紶
     *
     * @param inputStream 鏂囦欢杈撳叆娴?
     * @param objectName 鍘熷鏂囦欢鍚?
     * @return 鏂囦欢璁块棶璺緞
     */
    public String upload(InputStream inputStream, String objectName) {

        // 鍒涘缓OSSClient瀹炰緥銆?
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 鐢熸垚鏂扮殑鏂囦欢鍚嶅拰璺緞
        String dir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String suffix = "";
        if (objectName != null) {
            String name = objectName.trim();
            int dot = name.lastIndexOf('.');
            if (dot >= 0 && dot < name.length() - 1) {
                suffix = name.substring(dot);
            }
        }
        String newFileName = UUID.randomUUID() + suffix;
        String fullObjectName = dir + "/" + newFileName;

        try {
            // 鍒涘缓PutObject璇锋眰銆?
            ossClient.putObject(bucketName, fullObjectName, inputStream);
        } catch (OSSException oe) {
            log.error("OSS上传失败，bucketName={}, objectName={}, errorCode={}, requestId={}",
                    bucketName, fullObjectName, oe.getErrorCode(), oe.getRequestId(), oe);
            throw oe;
        } catch (ClientException ce) {
            log.error("OSS客户端异常，bucketName={}, objectName={}", bucketName, fullObjectName, ce);
            throw ce;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //鏂囦欢璁块棶璺緞瑙勫垯 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        String[] parts = endpoint.split("//");
        String host = parts.length > 1 ? parts[1] : endpoint;
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(host)
                .append("/")
                .append(fullObjectName);

        log.info("鏂囦欢涓婁紶鍒?{}", stringBuilder);

        return stringBuilder.toString();
    }
}
