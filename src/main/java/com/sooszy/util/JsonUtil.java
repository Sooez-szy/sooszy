package com.sooszy.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.lang3.StringUtils;
import java.text.SimpleDateFormat;


public class JsonUtil{
    private static ObjectMapper objectMapper = new ObjectMapper();
    static {
        //对象的所有字段全部列入
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

        //取消默认转换timesmaps形式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false);

        //忽略空bean转json的错误
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);

        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        //忽略 在json字符串中存在，但在java对象中不存在对应属性的情况。防止错误
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

    public static <T> String obj2String(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj:objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static <T> String obj2StringPretty(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj:objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T stringToObj(String str,Class<T> clazz){
        if(StringUtils.isEmpty(str)||clazz == null){
            return null;
        }
        try {
            return clazz.equals(String.class)? (T)str:objectMapper.readValue(str,clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T stringToObj(String str,TypeReference<T> typeReference){
        if(StringUtils.isEmpty(str)||typeReference == null){
            return null;
        }
        try {
            return (T)(typeReference.getType().equals(String.class)? str:objectMapper.readValue(str,typeReference));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T stringToObj(String str,Class<?> collectionClass,Class<?>... elementClasses){
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass,elementClasses);
        try {
            return objectMapper.readValue(str,javaType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

