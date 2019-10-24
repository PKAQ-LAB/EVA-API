package io.nerv.generator;

import java.util.Map;

public interface Generator {
    // 生成文件
    void generate(String namespace, Map<String, Object> params);
}
