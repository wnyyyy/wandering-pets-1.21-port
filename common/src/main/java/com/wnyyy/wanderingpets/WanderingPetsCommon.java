package com.wnyyy.wanderingpets;

import com.wnyyy.wanderingpets.config.ModConfig;
import com.wnyyy.wanderingpets.duck.IWanderingTamableAccessor;
import com.wnyyy.wanderingpets.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class WanderingPetsCommon {
    private static long lastUse = 0;

    public static void init() {
        Services.PLATFORM.registerEntityInteractCallback(WanderingPetsCommon::onEntityInteract);
        Services.PLATFORM.registerLevelLoadCallback(WanderingPetsCommon::onLevelLoad);
    }

    public static void onLevelLoad(ServerLevel level) {
        lastUse = 0;
        if (ModConfig.isEnableModdedEntities()) {
            ModConfig.rebuildEnabledEntities(level);
        }
    }

    public static InteractionResult onEntityInteract(Player player, Level level, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown() || !(entity instanceof TamableAnimal mob) || !ModConfig.isWanderBehaviorEnabled(mob) || player.isSpectator()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            long currentTime = level.getGameTime();
            if (currentTime - lastUse < 5) {
                return InteractionResult.FAIL;
            }

            InteractionResult result = handleTameableInteraction(player, mob);

            if (result.consumesAction()) {
                lastUse = currentTime;
                return result;
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static Optional<LivingEntity> getOwner(TamableAnimal entity) {
        return Optional.ofNullable(entity.getOwner());
    }

    private static InteractionResult handleTameableInteraction(Player player, TamableAnimal mob) {
        Optional<LivingEntity> maybeOwner = getOwner(mob);
        if (maybeOwner.isEmpty()) {
            return InteractionResult.PASS;
        }
        LivingEntity owner = maybeOwner.get();

        if (!owner.getUUID().equals(player.getUUID())) {
            return InteractionResult.PASS;
        }

        IWanderingTamableAccessor shouldFollowAccessor = (IWanderingTamableAccessor) mob;
        boolean shouldWander = !shouldFollowAccessor.wanderingpets$shouldWander();
        shouldFollowAccessor.wanderingpets$setShouldWander(shouldWander);

        player.displayClientMessage(Component.translatable(
                shouldWander ? "wanderingpets.wander" : "wanderingpets.follow", mob.getName()
        ), false);

        return InteractionResult.SUCCESS;
    }
}