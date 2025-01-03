package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.MediaApplication;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试minio的sdk
 *
 * @author ManolinCoder
 * @date 2024-10-21
 */


@SpringBootTest(classes = MediaApplication.class)
public class MinioTest {


    // MinIO服务器地址
    String url = "http://192.168.101.65:9000";
    // MinIO访问密钥
    String accessKey = "minioadmin";
    // MinIO秘密密钥
    String secretKey = "minioadmin";

    // 待上传的文件路径
    String filePath = "D:\\图片\\63775516760820.png";
    // MinIO存储桶名称
    String bucketName = "test-bucket";
    // 存储桶中的对象名称
    String objectName = "63775516760820.png";


    MinioClient minioClient = MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();


    /**
     * 上传文件
     *
     * @param
     * @return void
     * @author ManolinCoder
     * @date 2024-10-21
     */
    @Test
    public void testUpload() throws Exception {

        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;  // 通用 mimeType 字节流


        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }

        try {

            // Make 'asiatrip' bucket if not exist.
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket("test-bucket").build());
            if (!found) {
                // Make a new bucket called 'asiatrip'.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("test-bucket").build());
            } else {
                System.out.println("文件桶已存在！！！");
            }

            // 上传文件
            uploadFile(minioClient, filePath, bucketName, objectName, mimeType);

            //上传文件完整性校验
            boolean flag = checkFileIntegrity(minioClient, filePath, bucketName, objectName);


            System.out.println(flag ? "上传文件成功了！！！" : "上传文件失败了！！！");


        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
        }


    }


    /**
     * 删除文件
     *
     * @param
     * @return void
     * @author ManolinCoder
     * @date 2024-10-21
     */
//    @Test
//    public void testDelete() throws Exception {
//
//        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket("test-bucket").object("1.MP4").build();
//
//        minioClient.removeObject(removeObjectArgs);
//
//
//    }


    /**
     * 查询文件，下载到本地
     *
     * @param
     * @return void
     * @author ManolinCoder
     * @date 2024-10-21
     */
    @Test
    public void testGetObject() throws Exception {

        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("test-bucket").object("63775516760820.png").build();

        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);

        String localFileName = "D:\\63775516760820.png";

        // 指定输出流
        FileOutputStream outputStream = new FileOutputStream(new File(localFileName));
        IOUtils.copy(inputStream, outputStream);

        // md5完整性校验
        boolean flag = checkFileIntegrity(minioClient, localFileName, bucketName, objectName);


        if (flag) {
            System.out.println("下载成功了！！！");
        } else {
            System.out.println("下载失败了！！！");
        }


    }


    /**
     * 上传文件
     *
     * @param minioClient
     * @param filePath
     * @param bucketName
     * @param objectName
     * @param contentType
     * @return void
     * @author CoderManolin
     * @date 2024-10-25
     */
    public void uploadFile(MinioClient minioClient, String filePath, String bucketName, String objectName, String contentType) throws Exception {

        // 计算上传前本地文件MD5
        String uploadLocalFileMD5 = calculateMD5(filePath);
        System.out.println("上传前本地文件MD5: uploadLocalFileMD5=" + uploadLocalFileMD5);
        Map<String, String> md5Map = new HashMap<>();
        md5Map.put("md5", uploadLocalFileMD5);


        //上传文件到 MinIO
        File file = new File(filePath);
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(new FileInputStream(file), file.length(), -1)
                        .userMetadata(md5Map)
                        .contentType(contentType)
                        .build()
        );
        System.out.println("File uploaded successfully: " + objectName);

    }


    /**
     * 计算md5
     *
     * @param filePath
     * @return File
     * @author ManolinCoder
     * @date 2024-10-23
     */
    public String calculateMD5(String filePath) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        return DigestUtils.md5Hex(fileInputStream);
    }


    /**
     * 对比本地文件和minio文件的MD5值是否一致，校验文件完整性
     *
     * @param minioClient
     * @param filePath
     * @param bucketName
     * @param objectName
     * @return boolean
     * @author CoderManolin
     * @date 2024-10-25
     */

    public boolean checkFileIntegrity(MinioClient minioClient, String filePath, String bucketName, String objectName) throws Exception {


        // 计算本地文件的MD5
        String localFileMD5 = calculateMD5(filePath);
        System.out.println("Local file MD5: " + localFileMD5);

        // 获取MinIO中对象的MD5
        StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());

        String minioFileMD5 = stat.userMetadata().get("md5");
        System.out.println("MinIO file MD5: " + minioFileMD5);

        // 比较MD5值
        return localFileMD5.equals(minioFileMD5);


    }

    @Test
    void testEtag() throws Exception {
        StatObjectArgs statObjectArgs = StatObjectArgs.builder().bucket("video").object("/d/4/d41d8cd98f00b204e9800998ecf8427e/d41d8cd98f00b204e9800998ecf8427e.mp4").build();
        StatObjectResponse statObjectResponse = minioClient.statObject(statObjectArgs);
        String etag = statObjectResponse.etag();
        System.out.println(etag);
        GetObjectResponse getObjectResponse = minioClient.getObject(GetObjectArgs.builder().bucket("video").object("/d/4/d41d8cd98f00b204e9800998ecf8427e/d41d8cd98f00b204e9800998ecf8427e.mp4").build());
        FileOutputStream outputStream = new FileOutputStream("D:\\d41d8cd98f00b204e9800998ecf8427e.mp4");
        IOUtils.copy(getObjectResponse, outputStream);
        String s = calculateMD5("D:\\d41d8cd98f00b204e9800998ecf8427e.mp4");
        System.out.println(s);
    }

}