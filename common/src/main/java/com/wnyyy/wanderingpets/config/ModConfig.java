package com.wnyyy.wanderingpets.config;

import com.wnyyy.wanderingpets.Constants;
import com.wnyyy.wanderingpets.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import java.util.stream.Collectors;

public class ModConfig {

    private static Set<EntityType<?>> ENABLED_ENTITY_TYPES;
    private static ConfigData configData;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigValue {
        String description() default "";
        int min() default Integer.MIN_VALUE;
        int max() default Integer.MAX_VALUE;
    }

    public static class ConfigData {
        @ConfigValue(description = "Enable toggle wander behavior for cats")
        public boolean enableBehaviorCats = true;

        @ConfigValue(description = "Enable toggle wander behavior for wolves")
        public boolean enableBehaviorWolves = true;

        @ConfigValue(description = "Enable toggle wander behavior for parrots")
        public boolean enableBehaviorParrots = true;

        @ConfigValue(description = "Enhance cats' wander behavior, making them alternate between sitting, sleeping and wandering from time to time")
        public boolean betterCatBehavior = true;

        @ConfigValue(
                description = "Lower values makes cats sleep and sit more, higher values makes them wander more. Only applies if betterCatBehavior is enabled.",
                min = 200,
                max = 2000
        )
        public int catsRelaxingCooldown = 700;

        @ConfigValue(description = "Enable mod behavior for possibly compatible modded entities")
        public boolean enableModdedEntities = true;

        @ConfigValue(description = "Whitelist of modded mob IDs allowed to use mod behavior. Copy IDs from the discovered list below into here.")
        public List<String> moddedMobWhitelist = new ArrayList<>(Arrays.asList(
                "friendsandfoes:glare",
                "alexsmobs:capybara",
                "minecraft:fox"
        ));

        @ConfigValue(description = "[READ-ONLY] Detected mobs that may or may not be compatible with mod behavior. Add these into the whitelist to turn on mod behavior. Requires loading a save to populate")
        public List<String> discoveredCompatibleMobs =  new ArrayList<>(List.of());

        @ConfigValue(description = "Should log stuff")
        public boolean debugMode = false;
    }

    public static void initConfig(ConfigData loadedData) {
        if (ENABLED_ENTITY_TYPES == null) {
            ENABLED_ENTITY_TYPES = new HashSet<>();
            configData = loadedData;

            if (configData.enableBehaviorCats) ENABLED_ENTITY_TYPES.add(EntityType.CAT);
            if (configData.enableBehaviorWolves) ENABLED_ENTITY_TYPES.add(EntityType.WOLF);
            if (configData.enableBehaviorParrots) ENABLED_ENTITY_TYPES.add(EntityType.PARROT);
        }
    }

    public static boolean isWanderBehaviorEnabled(LivingEntity entity) {
        return ModConfig.ENABLED_ENTITY_TYPES.contains(entity.getType());
    }

    public static boolean isBetterCatBehaviorEnabled() {
        return configData.betterCatBehavior;
    }

    public static boolean isDebugMode() {
        return configData.debugMode;
    }

    public static boolean isEnableModdedEntities() {
        return configData.enableModdedEntities;
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
            return base * 3;
        }

        public static int sleepDur() {
            return base;
        }
    }

    public static void rebuildEnabledEntities(ServerLevel level) {
        ENABLED_ENTITY_TYPES.clear();
        if (configData.enableBehaviorCats) ENABLED_ENTITY_TYPES.add(EntityType.CAT);
        if (configData.enableBehaviorWolves) ENABLED_ENTITY_TYPES.add(EntityType.WOLF);
        if (configData.enableBehaviorParrots) ENABLED_ENTITY_TYPES.add(EntityType.PARROT);

        if (configData.enableModdedEntities) {
            Set<EntityType<? extends Mob>> discoveredMobs = discoverModdedMobs(level);

            configData.discoveredCompatibleMobs = discoveredMobs.stream()
                    .map(type -> BuiltInRegistries.ENTITY_TYPE.getKey(type).toString())
                    .sorted()
                    .collect(Collectors.toList());

            Services.PLATFORM.saveConfig(configData);

            Set<String> whitelist = new HashSet<>(configData.moddedMobWhitelist);
            for (EntityType<? extends Mob> type : discoveredMobs) {
                String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
                if (whitelist.contains(id)) {
                    ENABLED_ENTITY_TYPES.add(type);
                }
            }
        }
    }

    private static Set<EntityType<? extends Mob>> discoverModdedMobs(ServerLevel level) {
        Set<EntityType<? extends Mob>> mobs = new HashSet<>();
        Set<EntityType<? extends Mob>> entities = new HashSet<>();
        Set<String> blacklistedMods = Arrays.stream(Constants.BLACKLISTED_MODS.split(":")).collect(Collectors.toSet());
        Set<String> additionalVanilla = Arrays.stream(Constants.ADDITIONAL_VANILLA_MOBS.split(":")).collect(Collectors.toSet());

        for (Identifier location : BuiltInRegistries.ENTITY_TYPE.keySet()) {

            if (location.getNamespace().equals("minecraft")) {
                if (additionalVanilla.isEmpty()) {
                    continue;
                }
                if (additionalVanilla.contains(location.getPath())) {
                    //noinspection unchecked
                    BuiltInRegistries.ENTITY_TYPE.get(location).ifPresent(type -> mobs.add((EntityType<? extends Mob>) type.value()));
                }
                continue;
            }

            if (blacklistedMods.contains(location.getNamespace())) {
                continue;
            }

            //noinspection unchecked
            BuiltInRegistries.ENTITY_TYPE.get(location).ifPresent(type -> entities.add((EntityType<? extends Mob>) type.value()));
        }

        for (EntityType<? extends Mob> entityType : entities) {
            try {
                Entity example = entityType.create(level, EntitySpawnReason.COMMAND);
                if (example instanceof TamableAnimal) {
                    mobs.add(entityType);
                }
                if (example != null) {
                    example.discard();
                }
            } catch (Exception ignored) {
            }
        }

        return mobs;
    }
}

