package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author mugen
 * @description
 */
@Service
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class MediaFileServiceImpl extends ServiceImpl<MediaFilesMapper, MediaFiles> implements MediaFileService {

    @Resource
    MediaFilesMapper mediaFilesMapper;
    @Resource
    MinioClient minioClient;
    @Resource
    MediaProcessMapper mediaProcessMapper;
    @Value("${minio.bucket.files}")
    private String bucket_media;
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MediaFiles::getCompanyId, companyId)
                .like(StringUtils.isNotEmpty(queryMediaParamsDto.getFilename()), MediaFiles::getFilename, queryMediaParamsDto.getFilename())
                .eq(StringUtils.isNotEmpty(queryMediaParamsDto.getFileType()), MediaFiles::getFileType, queryMediaParamsDto.getFileType());
        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());

    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, String localFilePath, UploadFileParamsDto uploadFileParamsDto) {
        File file = new File(localFilePath);
        if (!file.exists()) {
            XueChengPlusException.cast("文件不存在");
        }
        //获取文件扩展名
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        //获取文件类型
        String mimeType = getMimeType(extension);
        //生成文件名
        String fileMd5 = calculateMD5(file);
        String objectName = getDefaultFolderPath() + fileMd5 + extension;
        //将文件上传到minio
        boolean flag = uploadFile(localFilePath, bucket_media, objectName, mimeType);
        if (!flag) {
            XueChengPlusException.cast("文件上传失败");
        }

        //将文件信息保存到数据库
        MediaFileService mediaFileService = (MediaFileService) AopContext.currentProxy();
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_media, objectName);
        if (mediaFiles == null) {
            XueChengPlusException.cast("文件信息保存失败");
        }
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);

        return uploadFileResultDto;
    }

    public boolean uploadFile(String filePath, String bucketName, String objectName, String contentType) {


        // 计算上传前本地文件MD5
//        String uploadLocalFileMD5 = calculateMD5(filePath);
//        System.out.println("上传前本地文件MD5: uploadLocalFileMD5=" + uploadLocalFileMD5);
//        Map<String, String> md5Map = new HashMap<>();
//        md5Map.put("md5", uploadLocalFileMD5);


        //上传文件到 MinIO
        File file = new File(filePath);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(Files.newInputStream(file.toPath()), file.length(), -1)
//                            .userMetadata(md5Map)
                            .contentType(contentType)
                            .build()
            );
            log.debug("上传文件到MinIO成功,bucket:{},objectName:{}", bucketName, objectName);
            return true;
        } catch (Exception e) {
            log.error("上传文件到MinIO失败,bucket:{},objectName:{},错误信息：{}", bucketName, objectName, e.getMessage());
        }
        return false;
    }

//    @Override
//    public MediaFiles getMediaFileById(String id) {
//        return mediaFilesMapper.selectById(id);
//    }

    public String calculateMD5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fileInputStream);
        } catch (Exception e) {
            return null;
        }
    }

    public String getMimeType(String extension) {

        if (extension == null) {
            extension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;

    }

    /**
     * 获取文件默认存储目录路径，格式为年/月/日。
     *
     * @return 返回文件默认存储目录路径的字符串表示，例如："2023/10/01/"
     */
    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date()).replace("-", "/") + "/";
    }

    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        //从数据库查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            //保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败,{}", mediaFiles.toString());
                XueChengPlusException.cast("保存文件信息失败");
            }
            log.debug("保存文件信息到数据库成功,{}", mediaFiles.toString());

            //记录待处理任务
            addWaitingTask(mediaFiles);

            return mediaFiles;
        }

        return mediaFiles;

    }

    /**
     * 添加待处理任务
     * @param mediaFiles 媒资文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles){
        //文件名称
        String filename = mediaFiles.getFilename();
        //文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //文件mimeType
        String mimeType = getMimeType(extension);
        //如果是avi视频添加到视频待处理表
        if(mimeType.equals("video/x-msvideo")){
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            mediaProcess.setStatus("1");//未处理
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);//失败次数默认为0
            mediaProcessMapper.insert(mediaProcess);
        }
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {

        // 先查数据库
        // 检查数据库中是否存在该文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {

            // 从数据库中获取存储桶名称和对象名称
            String bucket = mediaFiles.getBucket();
            String objectName = mediaFiles.getFilePath();

            // 构建获取对象参数
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build();

            try {
                // 从MinIO客户端获取对象输入流
                // 如果文件存在，则应该能够获取到输入流
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null) {
                    // 如果输入流不为空，表示文件存在，返回成功响应
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                // 如果在获取文件过程中发生异常，则记录错误日志
                log.error("查询文件失败,fileMd5:{},错误信息：{}", fileMd5, e.getMessage());
            }

        }

        // 如果文件不存在于数据库中或无法从MinIO中获取，则返回失败响应
        return RestResponse.success(false);
    }


    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 获取分块文件所在的文件夹路径
        String chunkFolderPath = getChunkFileFolderPath(fileMd5);

        // 构建获取分块文件的参数
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)  // 设置存储桶名称
                .object(chunkFolderPath + chunkIndex)  // 设置分块文件的名称（包括路径）
                .build();

        try {
            // 从MinIO客户端尝试获取分块文件的输入流
            // 如果文件存在，MinIO客户端将返回一个非空的输入流
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);

            // 判断输入流是否为空
            if (inputStream != null) {
                // 如果输入流不为空，说明分块文件存在
                // 返回表示成功的响应
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            // 如果在获取分块文件的过程中发生异常
            // 则记录错误日志，包括fileMd5、chunkIndex和异常信息
            log.error("查询分块失败,fileMd5:{},chunkIndex:{},错误信息：{}", fileMd5, chunkIndex, e.getMessage());
        }

        // 如果文件不存在或发生异常，则返回表示失败的响应
        return RestResponse.success(false);
    }


    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath){

        // 获取文件的MIME类型
        String mimeType = getMimeType(null);

        // 构建分块文件的存储路径
        String chuckFilePath = getChunkFileFolderPath(fileMd5) + chunk;

        // 上传分块文件到指定路径，并返回是否上传成功
        boolean flag = uploadFile(localChunkFilePath, bucket_video, chuckFilePath, mimeType);

        // 判断上传是否成功
        if (!flag) {
            // 如果上传失败，则返回失败响应
            return RestResponse.validfail(false, "上传分块失败");
        }

        // 如果上传成功，则返回成功响应
        return RestResponse.success(true);

    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //===找到分块文件调用minio进行文件合并===
        //获取分块文件路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //源文件名称
        String filename = uploadFileParamsDto.getFilename();
        //扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //组成将分块文件路径组成 List<ComposeSource>
        List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucket_video)
                        .object(chunkFileFolderPath.concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());
        //合并后的文件名称，此处用的是文件的MD5值作为文件名的一部分，再加上扩展名来命名最终的文件。
        String objectName = getFilePathByMd5(fileMd5, extension);
        //合并文件
        try {
            //构建合并文件的参数对象 ComposeObjectArgs
            ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                    .bucket(bucket_video)
                    .object(objectName)
                    .sources(sourceObjectList)
                    .build();
            minioClient.composeObject(composeObjectArgs);
            log.debug("合并文件成功:{}",objectName);
        } catch (Exception e) {
            log.error("合并文件失败,bucket:{},objectName:{},错误信息：{}", bucket_video, objectName, e.getMessage());
            return RestResponse.validfail(false, "合并文件失败");
        }

        //===校验合并后的和源文件是否一致，视频上传才成功===
        File minioFile = downloadFileFromMinIO(bucket_video,objectName);
        if(minioFile == null){
            log.debug("下载合并后文件失败,objectName:{}",objectName);
            return RestResponse.validfail(false, "下载合并后文件失败。");
        }
        try (FileInputStream newFileInputStream = new FileInputStream(minioFile)) {
            //minio上文件的md5值
            String md5Hex = DigestUtils.md5Hex(newFileInputStream);
            //比较md5值，不一致则说明文件不完整
            if(!fileMd5.equals(md5Hex)){
                log.error("文件合并校验失败,fileMd5:{},md5Hex:{}",fileMd5,md5Hex);
                //删除分块文件
                clearChunkFiles(chunkFileFolderPath, chunkTotal);
                //删除合并后的文件
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket_video).object(objectName).build());
                return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
            }
            //文件大小
            uploadFileParamsDto.setFileSize(minioFile.length());
        }catch (Exception e){
            log.debug("校验文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
            return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
        }finally {
            if(minioFile != null){
                minioFile.delete();
                log.debug("删除临时文件成功,objectName:{}",objectName);
            }
        }

        //===写入数据库===
        MediaFileService mediaFileService = (MediaFileService) AopContext.currentProxy();
//        try {
//            long size = minioClient.statObject(StatObjectArgs.builder().bucket(bucket_video).object(objectName).build()).size();
//            uploadFileParamsDto.setFileSize(size);
//        } catch (Exception e) {
//            log.error("获取文件大小失败,fileMd5:{},异常信息：{}", fileMd5, e.getMessage());
//            return RestResponse.validfail(false, "保存文件信息失败");
//        }
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
        if (mediaFiles == null) {
            log.error("插入数据库失败");
            //删除合并后的文件
            deleteMediaFromMinio(bucket_video, objectName);
            return RestResponse.validfail(false, "保存文件信息失败");
        }

        //===清理分块===
        clearChunkFiles(chunkFileFolderPath, chunkTotal);

        return RestResponse.success(true);
    }

    @Override
    public RestResponse<Boolean> deleteMediaFiles(String id) {
        // 查询数据库
        MediaFiles mediaFile = mediaFilesMapper.selectById(id);
        if (mediaFile == null) {
            return RestResponse.validfail(false,"删除失败");
        }

        // 删除MinIO中的媒体文件
        String bucket = mediaFile.getBucket();
        String filePath = mediaFile.getFilePath();
        deleteMediaFromMinio(bucket, filePath);

        // 删除数据库中的媒体文件记录
        int count = mediaFilesMapper.deleteById(id);
        if (count < 0) {
            return RestResponse.validfail(false,"删除失败");
        }

        return RestResponse.success(true);

    }

    private void deleteMediaFromMinio( String bucket, String objectName) {
        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build();
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            log.error("删除MinIO中的媒体文件时发生异常,bucket:{},objectName:{},message:{}", bucket, objectName, e.getMessage());
        }
    }

    /**
     * 根据文件的MD5值生成分块文件的存储路径。
     *
     * @param fileMd5 文件的MD5值
     * @return 分块文件的存储路径
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 得到合并后的文件的地址
     *
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return 返回合并后的文件路径字符串
     */
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 清除分块文件
     *
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal          分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) {
        try {
            // 创建删除对象的列表
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            // 构建删除对象的参数
            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucket_video).objects(deleteObjects).build();

            // 执行删除操作
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);

            // 遍历删除结果
            results.forEach(r -> {
                DeleteError deleteError = null;
                try {
                    // 获取删除错误
                    deleteError = r.get();
                } catch (Exception e) {
                    // 记录错误日志
                    log.error("清除分块文件失败,objectname:{}", deleteError.objectName(), e);
                }
            });
        } catch (Exception e) {
            // 记录异常日志
            log.error("清除分块文件失败,chunkFileFolderPath:{}", chunkFileFolderPath, e);
        }
    }


}
