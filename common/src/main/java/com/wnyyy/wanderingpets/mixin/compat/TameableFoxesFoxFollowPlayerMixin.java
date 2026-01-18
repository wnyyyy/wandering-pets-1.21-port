package com.wnyyy.wanderingpets.mixin.compat;

import com.wnyyy.wanderingpets.config.ModConfig;
import com.wnyyy.wanderingpets.duck.IWanderingTamableAccessor;
import lancet_.tameable_foxes.goals.FoxFollowPlayerGoal;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FoxFollowPlayerGoal.class, remap = false)
public abstract class TameableFoxesFoxFollowPlayerMixin implements IWanderingTamableAccessor {

    @Shadow(aliases = "fop")
    private Fox fop;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void wanderingpets$cancelCanUse(CallbackInfoReturnable<Boolean> cir) {
        if (this.fop instanceof IWanderingTamableAccessor pet && ModConfig.isWanderBehaviorEnabled(fop)) {
            if (pet.wanderingpets$shouldWander()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    private void wanderingpets$cancelCanContinue(CallbackInfoReturnable<Boolean> cir) {
        if (this.fop instanceof IWanderingTamableAccessor pet && ModConfig.isWanderBehaviorEnabled(fop)) {
            if (pet.wanderingpets$shouldWander()) {
                cir.setReturnValue(false);
            }
        }
    }
}