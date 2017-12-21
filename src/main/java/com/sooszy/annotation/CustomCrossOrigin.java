package com.sooszy.annotation;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.*;


@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS},allowedHeaders = {"content-type","authority","x-requested-with"})
public @interface CustomCrossOrigin {
}
