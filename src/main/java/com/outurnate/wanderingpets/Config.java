package com.outurnate.wanderingpets;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Wolf;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    public static final ModConfigSpec CONFIG_SPEC = BUILDER.build();

    public static boolean isWanderBehaviorEnabled(TamableAnimal entity) {
        return (entity instanceof Cat && GENERAL.enableBehaviorCats.get()) ||
                (entity instanceof Wolf && GENERAL.enableBehaviorWolves.get()) ||
                (entity instanceof Parrot && GENERAL.enableBehaviorParrots.get());
    }

    public static void log(String format, Object... args) {
        if (GENERAL.debugMode.get()) {
            WanderingPets.LOGGER.info(format, args);
        }
    }

    public static class General {

        public final ModConfigSpec.BooleanValue enableBehaviorCats;
        public final ModConfigSpec.BooleanValue enableBehaviorWolves;
        public final ModConfigSpec.BooleanValue enableBehaviorParrots;
        public final ModConfigSpec.BooleanValue betterCatBehavior;
        public final ModConfigSpec.BooleanValue debugMode;

        public General(ModConfigSpec.Builder builder) {
            builder.push("wanderingpets");
            enableBehaviorCats = builder
                    .comment("Enable toggle wander behavior for cats")
                    .define("enableBehaviorCats", true);
            builder.comment("");
            enableBehaviorWolves = builder
                    .comment("Enable toggle wander behavior for wolves")
                    .define("enableBehaviorWolves", true);
            builder.comment("");
            enableBehaviorParrots = builder
                    .comment("Enable toggle wander behavior for parrots")
                    .define("enableBehaviorParrots", true);
            builder.comment("");
            betterCatBehavior = builder
                    .comment("Enhance cats' wander behavior, making them alternate between sitting, sleeping and wandering from time to time")
                    .define("betterCatBehavior", true);
            builder.comment("");
            debugMode = builder
                    .comment("Should log stuff")
                    .define("debugMode", false);

            builder.pop();
        }
    }
}
