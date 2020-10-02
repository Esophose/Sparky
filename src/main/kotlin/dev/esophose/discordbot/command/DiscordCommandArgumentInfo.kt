package dev.esophose.discordbot.command

import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType
import java.util.Optional

class DiscordCommandArgumentInfo(private val parameter: Parameter, val position: Int) {

    val type: Class<*>
        get() = if (this.isOptional) this.optionalType!! else this.parameter.type

    val name: String
        get() = this.parameter.name!!

    val isOptional: Boolean
        get() = this.parameter.type == Optional::class.java

    val optionalType: Class<*>?
        get() = (this.parameter.parameterizedType as ParameterizedType).actualTypeArguments[0] as? Class<*>

    val isEnum: Boolean
        get() = this.parameter.type.isEnum

}
