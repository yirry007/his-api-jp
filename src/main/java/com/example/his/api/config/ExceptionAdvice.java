package com.example.his.api.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.felord.payment.PayException;
import cn.hutool.json.JSONObject;
import com.example.his.api.exception.HisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {
    /*
     * Catch Exception, return 500 status
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String exceptionHandler(Exception e) {
        JSONObject json = new JSONObject();
        if (e instanceof HttpMessageNotReadableException) {
            HttpMessageNotReadableException exception = (HttpMessageNotReadableException) e;
            log.error("error", exception);
            json.set("error", "リクエストでデータが送信されていない、もしくはデータに誤りがあります。");
        }
        else if (e instanceof MissingServletRequestPartException) {
            MissingServletRequestPartException exception = (MissingServletRequestPartException) e;
            log.error("error", exception);
            json.set("error", "リクエストで送信されたデータに誤りがあります。");
        }
        else if (e instanceof HttpRequestMethodNotSupportedException) {
            HttpRequestMethodNotSupportedException exception = (HttpRequestMethodNotSupportedException) e;
            log.error("error", exception);
            json.set("error", "HTTPリクエスト方法のタイプが間違っています。");
        }
        // Parameter data transform exception
        else if (e instanceof BindException) {
            BindException exception = (BindException) e;
            String defaultMessage = exception.getFieldError().getDefaultMessage();
            log.error(defaultMessage, exception);
            json.set("error", defaultMessage);
        }
        // Method argument not valid Exception
        else if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) e;
            json.set("error", exception.getBindingResult().getFieldError().getDefaultMessage());
        }
        // Project Exception
        else if (e instanceof HisException) {
            log.error("処理異常", e);
            HisException exception = (HisException) e;
            json.set("error", exception.getMsg());
        }
        // Wechat pay Exception
        else if (e instanceof PayException) {
            PayException exception = (PayException) e;
            log.error("WeChat支払いの異常", exception);
            json.set("error", "WeChat支払いの異常");
        }
        // Other Exception
        else {
            log.error("処理異常", e);
            json.set("error", "処理異常");
        }
        return json.toString();
    }

    /*
     * Catch Exception, return 401 status
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NotLoginException.class)
    public String unLoginHandler(Exception e) {
        JSONObject json = new JSONObject();
        json.set("error", e.getMessage());
        return json.toString();
    }

}
