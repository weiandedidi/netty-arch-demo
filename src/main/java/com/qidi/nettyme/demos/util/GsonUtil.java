package com.qidi.nettyme.demos.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 封装gson的工具类
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-07-05 19:49
 */

public class GsonUtil {

    private static final Gson gson;

    static {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATE_MILLISECOND_TIME_FORMAT);
        //日期格式调整 yyyy-MM-dd HH:mm:ss.SSS
        JsonSerializer<Date> dateSerializer = (src, typeOfSrc, context) -> context.serialize(sdf.format(src));
        JsonDeserializer<Date> dateDeserializer = (json, typeOfT, context) -> {
            try {
                return sdf.parse(json.getAsString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        JsonSerializer<LocalDateTime> localDateTimeSerializer = (src, typeOfSrc, context) -> context.serialize( src.format(formatter));
        JsonDeserializer<LocalDateTime> localDateTimeDeserializer = (json, typeOfT, context) -> {
            try {
                return  LocalDateTime.parse(json.getAsString(),formatter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        //时间修改格式
        gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, dateSerializer)
                .registerTypeAdapter(Date.class, dateDeserializer)
                .registerTypeAdapter(LocalDateTime.class, localDateTimeSerializer)
                .registerTypeAdapter(LocalDateTime.class, localDateTimeDeserializer)
                .setPrettyPrinting()
                .create();
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param object 要转换的对象
     * @return JSON 字符串
     */
    public static String toJsonString(Object object) {
        return gson.toJson(object);
    }

    /**
     * 将 JSON 字符串转换为指定类型的对象
     * Type type = new TypeToken<List<Student>>(){}.getType();
     * List<Student> list = new Gson().fromJson(jsonString,type)
     *
     * @param json JSON 字符串
     * @param type 指定的对象类型
     * @param <T>  对象类型
     * @return 指定类型的对象
     */
    public static <T> T fromJsonString(String json, Type type) {
        return gson.fromJson(json, type);
    }
//
//    public static void main(String[] args) {
//        String json = "[{\"name\":\"zhaoxa\",\"score\":100},{\"name\":\"zhaoxa2\",\"score\":76},{\"name\":\"zhaoxa3\",\"score\":99},{\"name\":\"zhaoxa4\",\"score\":48}]";
//        List<Student> students = gson.fromJson(json, new TypeToken<List<Student>>() {
//        }.getType());
//
//        System.out.println(students.get(2).getName());
//        System.out.println(students.get(2).getScore());
//
//    }
//
//    class Student {
//        String name;
//        int score;
//
//        public Student(String name, int score) {
//            this.name = name;
//            this.score = score;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public int getScore() {
//            return score;
//        }
//
//        public void setScore(int score) {
//            this.score = score;
//        }
//    }

}
