package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @description 全局异常处理器
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

   @ResponseBody
   @ExceptionHandler(XueChengPlusException.class)
   @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
   public RestErrorResponse customException(XueChengPlusException e) {
      log.error("【系统异常】{}",e.getErrMessage(),e);
      return new RestErrorResponse(e.getErrMessage());

   }

   @ResponseBody
   @ExceptionHandler(Exception.class)
   @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
   public RestErrorResponse exception(Exception e) {

      log.error("【系统异常】{}",e.getMessage(),e);

      return new RestErrorResponse(CommonError.UNKNOWN_ERROR.getErrMessage());

   }

   @ResponseBody // 将方法的返回值作为HTTP响应体返回
   @ExceptionHandler(MethodArgumentNotValidException.class) // 异常处理器，处理MethodArgumentNotValidException异常
   @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 设置HTTP响应状态码为500内部服务器错误
   public RestErrorResponse MethodArgumentNotValidException(MethodArgumentNotValidException e) {

      // 从异常对象中获取BindingResult对象
      BindingResult bindingResult = e.getBindingResult();

      // 创建一个ArrayList用于存储错误信息
      List<String> errors = new ArrayList<>();

      // 遍历BindingResult中的所有字段错误
      bindingResult.getFieldErrors().stream().forEach(item -> {
         // 将每个字段的错误信息添加到errors列表中
         errors.add(item.getDefaultMessage());
      });

      // 将所有错误信息用";"连接成一个字符串
      String errMessage = StringUtils.join(errors, ";");

      // 记录错误日志，包括异常信息和连接后的错误信息
      log.error("【系统异常】{}", errMessage);

      // 返回一个包含错误信息的RestErrorResponse对象
      return new RestErrorResponse(errMessage);

   }

}