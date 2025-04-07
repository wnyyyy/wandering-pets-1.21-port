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

        public final ModConfigSpec.BooleanValue disableFollowingCats;
        public final ModConfigSpec.BooleanValue disableFollowingWolves;
        public final ModConfigSpec.BooleanValue disableFollowingParrots;
        public final ModConfigSpec.BooleanValue enablePetRespawn;
        public final ModConfigSpec.BooleanValue enableToggleFollow;

        public General(ModConfigSpec.Builder builder) {
            builder.push("wanderingpets");
            disableFollowingCats = builder
                    .comment("Disable following for cats")
                    .define("disableCatFollow", true);

            disableFollowingWolves = builder
                    .comment("Disable following for wolves")
                    .define("disableWolfFollow", false);

            disableFollowingParrots = builder
                    .comment("Disable following for parrots")
                    .define("disableParrotFollow", true);

            enablePetRespawn = builder
                    .comment("Allow pets to respawn (with debuff)")
                    .define("enablePetRespawn", false);

            enableToggleFollow = builder
                    .comment("Toggle follow behavior with shift+right click")
                    .define("enableToggleFollow", true);

            builder.pop();
        }
    }

    public static boolean shouldEntityFollow(TamableAnimal entity) {
        return  (entity instanceof Cat    && GENERAL.disableFollowingCats.get()) ||
                (entity instanceof Wolf   && GENERAL.disableFollowingWolves.get()) ||
                (entity instanceof Parrot && GENERAL.disableFollowingParrots.get());
    }
}
