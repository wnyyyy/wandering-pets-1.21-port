package com.wnyyy.wanderingpets.platform.services;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface IPlatformHelper {
    void registerEntityInteractCallback(EntityInteractCallback callback);
    void registerLevelLoadCallback(Consumer<ServerLevel> callback);
    boolean isModLoaded(String modId);

    @FunctionalInterface
    interface EntityInteractCallback {
        InteractionResult interact(Player player, Level level, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult);
    }
}

