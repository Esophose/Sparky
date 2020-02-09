package dev.esophose.discordbot.command;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;

public class DiscordCommandArgumentInfo {

    private final Parameter parameter;

    public DiscordCommandArgumentInfo(Parameter parameter) {
        this.parameter = parameter;
    }

    public Class<?> getType() {
        return this.isOptional() ? this.getOptionalType() : this.parameter.getType();
    }

    public String getName() {
        return this.parameter.getName();
    }

    public boolean isOptional() {
        return this.parameter.getType() == Optional.class;
    }

    public Class<?> getOptionalType() {
        return (Class<?>) ((ParameterizedType) this.parameter.getParameterizedType()).getActualTypeArguments()[0];
    }

    public boolean isEnum() {
        return this.parameter.getType().isEnum();
    }

}
