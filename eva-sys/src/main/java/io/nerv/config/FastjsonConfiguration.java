package io.nerv.config;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.serializer.ContextValueFilter;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import io.nerv.core.annotation.Code;
import io.nerv.web.sys.dict.cache.DictHelperProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


/**
 * 使用fastjson代替jackson,
 * @author PKAQ
 */
@Configuration
public class FastjsonConfiguration {

    @Autowired
    DictHelperProvider dictHelper;

    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        //定义一个转换消息的对象
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();

        // 防止返回字符串多出双引号
        List<MediaType> mediaTypes = new ArrayList<>(2);
        mediaTypes.add(MediaType.valueOf(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8"));
        mediaTypes.add(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"));

        fastConverter.setSupportedMediaTypes(mediaTypes);

        //添加fastjson的配置信息 比如 ：是否要格式化返回的json数据
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setCharset(Charset.forName("UTF-8"));
        // 格式化json,
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat);
        fastJsonConfig.setDateFormat(DatePattern.NORM_DATETIME_PATTERN);
        fastJsonConfig.setSerializeFilters((PropertyFilter) (object, name, value) ->
                !(value instanceof List) || ((List) value).size() >= 1);

        //字典值转换
        fastJsonConfig.setSerializeFilters((ContextValueFilter) (context, object, name, value) -> {
            if(value == null || context == null){
                return value;
            }
            //根据注解判断该属性值是否需要转换
            Code codeFilter = context.getAnnation(Code.class);
            if (codeFilter != null) {
                String dictValue = dictHelper.get(StrUtil.isBlank(codeFilter.value()) ? name : codeFilter.value(), value.toString());
                value = StrUtil.isNotBlank(dictValue) ? dictValue : value;
            }
            return value;
        });
        //在转换器中添加配置信息
        fastConverter.setFastJsonConfig(fastJsonConfig);

        return new HttpMessageConverters(fastConverter);
    }
}
