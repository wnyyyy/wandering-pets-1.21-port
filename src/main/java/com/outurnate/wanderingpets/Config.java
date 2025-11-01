package com.outurnate.wanderingpets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("wanderingpets.json");

    private static ConfigData configData = new ConfigData();

    public static Set<EntityType<?>> ENABLED_ENTITY_TYPES = Collections.emptySet();

    private static class ConfigData {
        boolean enableBehaviorCats = true;
        boolean enableBehaviorWolves = true;
        boolean enableBehaviorParrots = true;
        boolean betterCatBehavior = true;
        int catsRelaxingCooldown = 800;
        boolean debugMode = false;
        boolean enableModdedEntities = true;
        List<String> moddedEntities = new ArrayList<>();
    }

    public static void loadConfig() {
        boolean needsSave = false;
        try (FileReader reader = new FileReader(CONFIG_FILE.toFile())) {
            configData = GSON.fromJson(reader, ConfigData.class);
            if (configData == null) {
                configData = new ConfigData();
                needsSave = true;
            }
        } catch (IOException e) {
            configData = new ConfigData();
            needsSave = true;
        }

        if (needsSave) {
            saveConfig();
        }
        rebuildEnabledEntityTypes();
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE.toFile())) {
            GSON.toJson(configData, writer);
        } catch (IOException e) {
            WanderingPets.LOGGER.error("Could not save config file!", e);
        }
    }

    public static void rebuildEnabledEntityTypes() {
        List<String> allEnabled = new ArrayList<>();

        if (configData.enableBehaviorCats) allEnabled.add("minecraft:cat");
        if (configData.enableBehaviorWolves) allEnabled.add("minecraft:wolf");
        if (configData.enableBehaviorParrots) allEnabled.add("minecraft:parrot");

        if (configData.enableModdedEntities) {
            allEnabled.addAll(configData.moddedEntities);
        }

        ENABLED_ENTITY_TYPES = allEnabled.stream()
                .map(id -> {
                    try {
                        return ResourceLocation.tryParse(id);
                    } catch (Exception e) {
                        log("Invalid entity ID format in config: {}", id);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(id -> {
                    Optional<Holder.Reference<EntityType<?>>> type = BuiltInRegistries.ENTITY_TYPE.get(id);
                    if (type.isEmpty()) {
                        log("Entity id {} not found, ignoring it...", id);
                        return null;
                    }
                    return type.get().value();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static boolean isWanderBehaviorEnabled(Mob mob) {
        return ENABLED_ENTITY_TYPES.contains(mob.getType());
    }

    public static void log(String format, Object... args) {
        if (configData.debugMode) {
            WanderingPets.LOGGER.info(format, args);
        }
    }

    public static List<String> getModdedEntities() {
        return configData.moddedEntities;
    }

    public static void setModdedEntities(List<String> entities) {
        configData.moddedEntities = new ArrayList<>(entities);
    }

    public static boolean isBetterCatBehaviorEnabled() {
        return configData.betterCatBehavior;
    }

    public static boolean isDebugMode() {
        return configData.debugMode;
    }

    public static class CatsRelaxingProfile {
        private static final int base = configData.catsRelaxingCooldown;

        public static int sitCd() {
            return base;
        }

        public static int sitDur() {
            return base / 4;
        }

        public static int sleepCd() {
            return base * 4;
        }

        public static int sleepDur() {
            return base;
        }
    }
}

