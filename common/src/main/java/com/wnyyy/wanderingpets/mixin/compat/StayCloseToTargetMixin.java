package com.wnyyy.wanderingpets.mixin.compat;

import com.wnyyy.wanderingpets.config.ModConfig;
import com.wnyyy.wanderingpets.duck.IWanderingTamableAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.StayCloseToTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Predicate;

@Mixin(StayCloseToTarget.class)
public class StayCloseToTargetMixin {

    @ModifyVariable(
            method = "create",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private static Predicate<LivingEntity> wanderingpets$StayCloseToTargetMixin(Predicate<LivingEntity> originalPredicate) {
        return (entity) -> {
            if (!originalPredicate.test(entity) && !ModConfig.isEnableModdedEntities()) {
                return false;
            }
            return !(entity instanceof IWanderingTamableAccessor pet) || !pet.wanderingpets$shouldWander();
        };
    }
}