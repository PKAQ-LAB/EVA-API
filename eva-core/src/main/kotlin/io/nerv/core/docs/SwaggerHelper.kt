package io.nerv.core.docs

import springfox.documentation.builders.ParameterBuilder
import springfox.documentation.schema.ModelRef
import springfox.documentation.service.Parameter
import java.util.*

object SwaggerHelper {
    //header中的ticket参数非必填，传空也可以
    @JvmStatic
    val headPars: List<Parameter>
        get() {
            val pars: MutableList<Parameter> = ArrayList()
            val device = ParameterBuilder()
            val version = ParameterBuilder()
            device.name("device").description("设备类型")
                    .modelRef(ModelRef("string")).parameterType("header") //header中的ticket参数非必填，传空也可以
                    .required(false).build()
            pars.add(device.build())
            version.name("version").description("应用版本")
                    .modelRef(ModelRef("string")).parameterType("header") //header中的ticket参数非必填，传空也可以
                    .required(false).build()
            pars.add(version.build())
            return pars
        }
}