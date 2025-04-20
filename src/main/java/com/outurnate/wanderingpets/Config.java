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

    public static class CatsRelaxingProfile {
        private static final int base = GENERAL.catsRelaxingCooldown.get();

        public static int sitCd() {
            return base;
        }

        public static int sitDur() {
            return base/4;
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
            catsRelaxingCooldown = builder
                    .comment("(Default: 1600 | Range: 200~8000) Lower values makes cats sleep and sit more, higher values makes them wander more. Only applies if betterCatBehavior is enabled.")
                    .defineInRange("catsRelaxingCooldown", 1600, 200, 8000);
            builder.comment("");
            debugMode = builder
                    .comment("Should log stuff")
                    .define("debugMode", false);

            builder.pop();
        }
    }
}
