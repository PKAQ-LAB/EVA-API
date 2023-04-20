//package io.nerv.config;
//
//import org.springdoc.core.models.GroupedOpenApi;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//
///**
// * swagger配置类
// * @author PKAQ
// * @Profile()
// */
//@Configuration
//@Profile({"!prod"})
//public class JobAPIConfiguration {
//
//    @Bean
//    public GroupedOpenApi sysApiGroup() {
//        return GroupedOpenApi.builder().group("任务管理").pathsToMatch(new String[]{"/job/**"}).build();
//    }
//}
