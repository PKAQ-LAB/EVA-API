package io.nerv.core.docs;

import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;

import java.util.ArrayList;
import java.util.List;

public class SwaggerHelper {
    public static List getHeadPars(){
        List<Parameter> pars = new ArrayList<>();

        var device = new ParameterBuilder();
        var version = new ParameterBuilder();

        device.name("device").description("设备类型")
                .modelRef(new ModelRef("string")).parameterType("header")
                //header中的ticket参数非必填，传空也可以
                .required(false).build();
        pars.add(device.build());

        version.name("version").description("应用版本")
                .modelRef(new ModelRef("string")).parameterType("header")
                //header中的ticket参数非必填，传空也可以
                .required(false).build();
        pars.add(version.build());

        return pars;
    }
}
