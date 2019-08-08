package io.nerv.web.service;

import io.nerv.web.entity.GeneratorEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
public class GeneratorService {
    /**
     * 根据配置生成代码
     * @param generatorEntity
     */
    private void generate(GeneratorEntity generatorEntity){

    }
}
