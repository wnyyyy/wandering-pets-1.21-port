package com.wnyyy.wanderingpets.mixin.compat;

import com.wnyyy.wanderingpets.config.ModConfig;
import com.wnyyy.wanderingpets.duck.IWanderingTamableAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.faboslav.friendsandfoes.common.entity.ai.brain.task.glare.GlareTeleportToOwnerTask", remap = false)
public class FriendsAndFoesTeleportMixin {

    @Inject(
            method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void wanderingpets$checkExtraStartConditions(ServerLevel world, LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.isWanderBehaviorEnabled(entity) && entity instanceof IWanderingTamableAccessor tamable && tamable.wanderingpets$shouldWander()) {
            cir.setReturnValue(false);
        }
    }
}