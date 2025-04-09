package com.outurnate.wanderingpets;

import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.TamableAnimal;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    public static final ModConfigSpec CONFIG_SPEC = BUILDER.build();

    public static class General {

        public final ModConfigSpec.BooleanValue enableBehaviorCats;
        public final ModConfigSpec.BooleanValue enableBehaviorWolves;
        public final ModConfigSpec.BooleanValue enableBehaviorParrots;
        public final ModConfigSpec.BooleanValue enablePetRespawn;

        public General(ModConfigSpec.Builder builder) {
            builder.push("wanderingpets");
            enableBehaviorCats = builder
                    .comment("Enable toggle wander behavior for cats")
                    .define("enableBehaviorCats", true);

            enableBehaviorWolves = builder
                    .comment("Enable toggle wander behavior for wolves")
                    .define("enableBehaviorWolves", true);

            enableBehaviorParrots = builder
                    .comment("Enable toggle wander behavior for parrots")
                    .define("enableBehaviorParrots", true);

            enablePetRespawn = builder
                    .comment("Allow pets to respawn (with debuff to player)")
                    .define("enablePetRespawn", false);

            builder.pop();
        }
    }

    public static boolean shouldEntityFollow(TamableAnimal entity) {
        return  (entity instanceof Cat    && GENERAL.enableBehaviorCats.get()) ||
                (entity instanceof Wolf   && GENERAL.enableBehaviorWolves.get()) ||
                (entity instanceof Parrot && GENERAL.enableBehaviorParrots.get());
    }
}
