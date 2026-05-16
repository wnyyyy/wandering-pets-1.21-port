package com.wnyyy.wanderingpets;

import com.wnyyy.wanderingpets.config.ModConfig;
import com.wnyyy.wanderingpets.config.ModConfig.ConfigData;
import com.wnyyy.wanderingpets.config.ModConfig.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NeoForgeModConfig {
    public static final ModConfigSpec SPEC;
    private static final Map<String, ModConfigSpec.ConfigValue<?>> CONFIG_VALUE_MAP = new HashMap<>();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ConfigData defaults = new ConfigData();

        builder.push("General Settings");

        for (Field field : ConfigData.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigValue.class)) {
                field.setAccessible(true);
                ConfigValue info = field.getAnnotation(ConfigValue.class);
                String name = field.getName();

                try {
                    Object defaultValue = field.get(defaults);

                    builder.comment(String.format("%s (Default: %s)", info.description(), defaultValue));

                    if (defaultValue instanceof Boolean b) {
                        CONFIG_VALUE_MAP.put(name, builder.define(name, b));
                    } else if (defaultValue instanceof Integer i) {
                        CONFIG_VALUE_MAP.put(name, builder.defineInRange(name, i, info.min(), info.max()));
                    } else if (defaultValue instanceof List<?> list) {
                        CONFIG_VALUE_MAP.put(name, builder.defineList(name, list, () -> "", element -> true));
                    }
                } catch (IllegalAccessException ignored) { }
            }
        }
        builder.pop();
        SPEC = builder.build();
    }

    public static ConfigData loadConfig() {
        ConfigData data = new ConfigData();
        for (Field field : ConfigData.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                ModConfigSpec.ConfigValue<?> value = CONFIG_VALUE_MAP.get(field.getName());
                if (value != null) {
                    field.set(data, value.get());
                }
            } catch (IllegalAccessException ignored) {}
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    public static void saveConfig(ConfigData data) {
        for (Field field : ConfigData.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(ModConfig.ConfigValue.class)) {
                try {
                    field.setAccessible(true);
                    ModConfigSpec.ConfigValue<Object> configValue = (ModConfigSpec.ConfigValue<Object>) CONFIG_VALUE_MAP.get(field.getName());

                    if (configValue != null) {
                        Object runtimeValue = field.get(data);
                        configValue.set(runtimeValue);
                    }
                } catch (IllegalAccessException ignored) {}
            }
        }
        SPEC.save();
    }
}