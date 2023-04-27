//package io.nerv.config;
//
//import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
//import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
//import io.nerv.properties.EvaConfig;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * mybatis plus 配置
// * 仅在启用后配置
// * @author PKAQ
// */
//@Configuration
//@ConditionalOnProperty(prefix = "eva.datapermission", name = "enable", havingValue = "true")
//public class MybatisPlusPermissionConfig {
//    @Autowired
//    private EvaConfig evaConfig;
//
//    @Autowired
//    private MybatisPlusInterceptor mybatisPlusInterceptor;
//    /**
//    * 数据权限插件
//    * @return
//    */
//   @Bean
//   public void permissionInterceptor() {
//
//       DataPermissionInterceptor dataPermissionInterceptor = new DataPermissionInterceptor();
//
//       MybatisPlusDataPermissionHandler myDataPermissionHandler = new MybatisPlusDataPermissionHandler();
//                                        myDataPermissionHandler.setEvaConfig(evaConfig);
//
//       dataPermissionInterceptor.setDataPermissionHandler(myDataPermissionHandler);
//
//       mybatisPlusInterceptor.addInnerInterceptor(dataPermissionInterceptor);
//   }
//}