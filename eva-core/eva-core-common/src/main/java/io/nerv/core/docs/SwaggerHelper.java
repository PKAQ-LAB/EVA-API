package io.nerv.core.docs;

import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;

import java.util.ArrayList;
import java.util.List;

public class SwaggerHelper {
    public static List getHeadPars(){
        List<RequestParameter> pars = new ArrayList<>();

        var device = new RequestParameterBuilder();
        var version = new RequestParameterBuilder();

        device.name("device")
              .description("设备类型")
              .in(ParameterType.HEADER)
              //header中的ticket参数非必填，传空也可以
              .required(false).query(param -> param.model(model -> model.scalarModel(ScalarType.STRING)))
              .build();
        pars.add(device.build());

        version.name("version")
               .description("应用版本")
               .in(ParameterType.HEADER)
               //header中的ticket参数非必填，传空也可以
               .required(false).query(param -> param.model(model -> model.scalarModel(ScalarType.STRING)))
               .build();
        pars.add(version.build());

        return pars;
    }
}
