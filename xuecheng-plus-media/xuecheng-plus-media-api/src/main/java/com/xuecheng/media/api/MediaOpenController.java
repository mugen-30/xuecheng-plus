package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/open")
public class MediaOpenController {

    @Resource
    private MediaFileService mediaFileService;

    @ApiOperation("预览视频")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> preview(@PathVariable String mediaId) {
        MediaFiles mediaFiles = mediaFileService.getById(mediaId);
        if (mediaFiles == null){
            return RestResponse.validfail("该视频不存在");
        }
        String url = mediaFiles.getUrl();
        if (StringUtils.isBlank(url)){
            return RestResponse.validfail("该视频正在处理中");
        }

        return RestResponse.success(mediaFiles.getUrl());
    }

}
