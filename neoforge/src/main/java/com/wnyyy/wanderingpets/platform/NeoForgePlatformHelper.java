package com.wnyyy.wanderingpets.platform;

import com.wnyyy.wanderingpets.platform.services.IPlatformHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.function.Consumer;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public void registerEntityInteractCallback(EntityInteractCallback callback) {
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.EntityInteract event) -> {
            InteractionResult result = callback.interact(
                    event.getEntity(),
                    event.getLevel(),
                    event.getHand(),
                    event.getTarget(),
                    null
            );

            if (result.consumesAction()) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
        });
    }

    @Override
    public void registerLevelLoadCallback(Consumer<ServerLevel> callback) {
        NeoForge.EVENT_BUS.addListener((LevelEvent.Load event) -> {
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                callback.accept(serverLevel);
            }
        });
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}