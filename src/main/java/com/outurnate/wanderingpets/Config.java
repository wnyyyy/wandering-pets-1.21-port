package com.outurnate.wanderingpets;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    public static final ModConfigSpec CONFIG_SPEC = BUILDER.build();
    private static List<EntityType<?>> ENABLED_ENTITY_TYPES = List.of();

    public static boolean isWanderBehaviorEnabled(Entity entity) {
        return ENABLED_ENTITY_TYPES.contains(entity.getType());
    }

    public static void rebuildEnabledEntityTypes() {
        List<EntityType<?>> result = new java.util.ArrayList<>();

        if (GENERAL.enableBehaviorCats.get()) result.add(EntityType.CAT);
        if (GENERAL.enableBehaviorWolves.get()) result.add(EntityType.WOLF);
        if (GENERAL.enableBehaviorParrots.get()) result.add(EntityType.PARROT);

        if (GENERAL.enableModdedEntities.get()) {
            for (Object o : GENERAL.moddedEntities.get()) {
                if (o instanceof String s) {
                    try {
                        ResourceLocation id = ResourceLocation.parse(s);
                        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
                        result.add(type);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        ENABLED_ENTITY_TYPES = List.copyOf(result);
    }

    public static void log(String format, Object... args) {
        if (GENERAL.debugMode.get()) {
            WanderingPets.LOGGER.info(format, args);
        }
    }

    public static class CatsRelaxingProfile {
        private static final int base = GENERAL.catsRelaxingCooldown.get();

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

    public static class General {

        public final ModConfigSpec.BooleanValue enableBehaviorCats;
        public final ModConfigSpec.BooleanValue enableBehaviorWolves;
        public final ModConfigSpec.BooleanValue enableBehaviorParrots;
        public final ModConfigSpec.BooleanValue betterCatBehavior;
        public final ModConfigSpec.ConfigValue<Integer> catsRelaxingCooldown;
        public final ModConfigSpec.BooleanValue debugMode;
        public final ModConfigSpec.BooleanValue enableModdedEntities;
        public final ModConfigSpec.ConfigValue<List<?>> moddedEntities;


        public General(ModConfigSpec.Builder builder) {
            builder.push("wanderingpets");
            enableBehaviorCats = builder
                    .comment(" Enable toggle wander behavior for cats")
                    .define("enableBehaviorCats", true);
            enableBehaviorWolves = builder
                    .comment(" Enable toggle wander behavior for wolves")
                    .define("enableBehaviorWolves", true);
            enableBehaviorParrots = builder
                    .comment(" Enable toggle wander behavior for parrots")
                    .define("enableBehaviorParrots", true);
            betterCatBehavior = builder
                    .comment(" Enhance cats' wander behavior, making them alternate between sitting, sleeping and wandering from time to time")
                    .define("betterCatBehavior", true);
            catsRelaxingCooldown = builder
                    .comment(" Lower values makes cats sleep and sit more, higher values makes them wander more. Only applies if betterCatBehavior is enabled.")
                    .defineInRange("catsRelaxingCooldown", 1600, 200, 8000);
            enableModdedEntities = builder
                    .comment(" Enable mod behavior for possibly compatible modded entities")
                    .define("enableModdedEntities", true);
            moddedEntities = builder
                    .comment(" List of modded mobs that should use mod behavior. If left empty, will search for possibly compatible entities. Format: modid:entity_id")
                    .defineListAllowEmpty(
                            "moddedEntities",
                            List::of,
                            List::of,
                            o -> {
                                if (!(o instanceof String s)) return false;
                                try {
                                    ResourceLocation.parse(s);
                                    return true;
                                } catch (Exception e) {
                                    return false;
                                }
                            }
                    );

            debugMode = builder
                    .comment(" Should log stuff")
                    .define("debugMode", false);

            builder.pop();
        }
    }
}
