package com.xuecheng.orders.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2025-01-16
 */
public interface IXcPayRecordService extends IService<XcPayRecord> {

    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付记录id
     * @return 支付记录信息
     */
    PayRecordDto queryPayResult(String payNo);

    /**
     * @description 保存支付宝支付结果
     * @param payStatusDto  支付结果信息
     * @return void
     * @author Mr.M
     * @date 2022/10/4 16:52
     */
    void saveAliPayStatus(PayStatusDto payStatusDto) ;

}
