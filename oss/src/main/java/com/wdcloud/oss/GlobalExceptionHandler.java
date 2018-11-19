package com.wdcloud.oss;

import com.google.common.base.Throwables;
import com.wdcloud.utils.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    @ResponseBody
    private Object exec(Exception ex) {
        log.error(Throwables.getStackTraceAsString(ex));
        return ResponseDTO.notOK(null, ex.getMessage());
    }
}
