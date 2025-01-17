package com.xuecheng.auth.model.dto;

import com.xuecheng.auth.model.po.XcUser;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mugen
 * @description 用户扩展信息
 */
@Data
public class XcUserExt extends XcUser {
    //用户权限
    List<String> permissions = new ArrayList<>();
}
