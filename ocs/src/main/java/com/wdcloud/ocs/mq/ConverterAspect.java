package com.wdcloud.ocs.mq;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.wdcloud.model.dao.FileInfoDao;
import com.wdcloud.mq.model.ConvertMQO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredFieldsWarningInspection"})
@Aspect
@Component
public class ConverterAspect {
    @Autowired
    private FileInfoDao fileInfoDao;

    @Pointcut("@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener)")
    public void mq() {
    }

    @Around("mq()")
    public Object aroundAdvice(ProceedingJoinPoint pjp) {
        System.out.println("===========around before advice");
        ConvertMQO mqo = (ConvertMQO) pjp.getArgs()[0];
        Object retVal = exec(mqo, pjp);
        System.out.println("===========around after advice");
        return retVal;
    }

    private Object exec(ConvertMQO mqo, ProceedingJoinPoint pjp) {
        Object result = null;
        boolean isError = false;
        String error = "";
        int TRY_COUNT = 3;
        for (int i = 0; i < TRY_COUNT; i++) {
            try {
                result = pjp.proceed();
                isError = false;
                break;
            } catch (Throwable e) {
                isError = true;
                error = Throwables.getStackTraceAsString(e);
            }
        }
        if (isError) {
            final HashMap<String, Object> map = Maps.newHashMap();
            map.put("fileId", mqo.getFileId());
            map.put("errorMsg", error.substring(0, error.length() > 4095 ? 4095 : error.length()));
            fileInfoDao.saveErrorMsg(map);
        }

        return result;
    }
}
