package com.wnyyy.wanderingpets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wnyyy.wanderingpets.config.ModConfig.ConfigData;
import com.wnyyy.wanderingpets.config.ModConfig.ConfigValue;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wnyyy.wanderingpets.Constants.MOD_ID;

public class FabricModConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static ConfigData loadConfig() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json5");
        Path oldConfigPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
        ConfigData configData;

        if (Files.exists(oldConfigPath)) {
            try {
                Files.deleteIfExists(oldConfigPath);
            } catch (IOException ignored) { }
        }

        if (Files.exists(configPath)) {
            try {
                String content = Files.readString(configPath);
                JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();

                configData = GSON.fromJson(jsonObject, ConfigData.class);

                if (configData == null || isOutdated(jsonObject)) {
                    configData = (configData == null) ? new ConfigData() : configData;
                    validateConfig(configData);
                } else {
                    validateConfig(configData);
                }
            } catch (Exception e) {
                configData = new ConfigData();
            }
        } else {
            configData = new ConfigData();
        }
        saveConfig(configData);
        return configData;
    }

    private static boolean isOutdated(JsonObject json) {
        Set<String> jsonKeys = json.keySet();
        Set<String> expectedFields = Arrays.stream(ConfigData.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        if (!jsonKeys.containsAll(expectedFields)) return true;

        return !expectedFields.containsAll(jsonKeys);
    }

    private static void validateConfig(ConfigData data) {
        for (Field field : ConfigData.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigValue.class)) {
                ConfigValue annotation = field.getAnnotation(ConfigValue.class);
                try {
                    field.setAccessible(true);
                    if (field.getType() == int.class) {
                        int val = field.getInt(data);
                        int clamped = Math.max(annotation.min(), Math.min(annotation.max(), val));
                        if (val != clamped) {
                            field.setInt(data, clamped);
                        }
                    }
                } catch (IllegalAccessException ignored) {}
            }
        }
    }

    private static void saveConfig(ConfigData data) {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json5");
        try {
            Files.createDirectories(configPath.getParent());
            String rawJson = GSON.toJson(data);
            String commentedJson = injectComments(rawJson);
            Files.writeString(configPath, commentedJson);
        } catch (IOException ignored) { }
    }

    private static String injectComments(String json) {
        List<String> lines = new ArrayList<>(List.of(json.split("\n")));
        ConfigData defaults = new ConfigData();
        int fieldsProcessed = 0;

        for (Field field : ConfigData.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigValue.class)) {
                ConfigValue info = field.getAnnotation(ConfigValue.class);
                String fieldName = field.getName();

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (line.contains("\"" + fieldName + "\":")) {
                        try {
                            field.setAccessible(true);
                            Object defVal = field.get(defaults);

                            String indent = line.substring(0, line.indexOf("\""));
                            String range = (field.getType() == int.class)
                                    ? String.format(" [Range: %d ~ %d]", info.min(), info.max())
                                    : "";

                            String comment = String.format("%s// %s (Default: %s)%s", indent, info.description(), defVal, range);

                            if (fieldsProcessed > 0) {
                                lines.add(i, "");
                                i++;
                            }

                            lines.add(i, comment);
                            i++;
                            fieldsProcessed++;
                        } catch (IllegalAccessException ignored) {}
                        break;
                    }
                }
            }
        }
        return String.join("\n", lines);
    }
}
