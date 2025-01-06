package com.xuecheng.media;

import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.service.MediaFileService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class MediaFileServiceTest {

    @Resource
    private MediaFileService mediaFileService;

    @Test
    void testUpload() throws Exception {

        Long companyId = 1232141425L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        mediaFileService.uploadFile(companyId, "D:\\63775516760820.png", uploadFileParamsDto, null);

    }



}
