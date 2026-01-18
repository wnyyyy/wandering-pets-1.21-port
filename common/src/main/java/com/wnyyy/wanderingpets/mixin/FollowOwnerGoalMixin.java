package com.wnyyy.wanderingpets.mixin;

import com.wnyyy.wanderingpets.config.ModConfig;
import com.wnyyy.wanderingpets.duck.IWanderingTamableAccessor;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FollowOwnerGoal.class)
public class FollowOwnerGoalMixin {
    @Shadow @Final private TamableAnimal tamable;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void canUse(CallbackInfoReturnable<Boolean> cir) {
        if (this.tamable instanceof IWanderingTamableAccessor pet && pet.wanderingpets$shouldWander() && ModConfig.isWanderBehaviorEnabled(this.tamable)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "canContinueToUse", cancellable = true)
    public void canContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        if (this.tamable instanceof IWanderingTamableAccessor pet && pet.wanderingpets$shouldWander() && ModConfig.isWanderBehaviorEnabled(this.tamable)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}