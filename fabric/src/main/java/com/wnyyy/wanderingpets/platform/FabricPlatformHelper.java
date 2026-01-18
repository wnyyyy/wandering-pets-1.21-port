package com.wnyyy.wanderingpets.platform;

import com.wnyyy.wanderingpets.platform.services.IPlatformHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerLevel;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public void registerEntityInteractCallback(EntityInteractCallback callback) {
        UseEntityCallback.EVENT.register(callback::interact);
    }

    @Override
    public void registerLevelLoadCallback(java.util.function.Consumer<ServerLevel> callback) {
        ServerWorldEvents.LOAD.register((server, level) -> callback.accept(level));
    }
}