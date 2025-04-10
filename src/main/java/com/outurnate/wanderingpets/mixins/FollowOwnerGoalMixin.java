package com.outurnate.wanderingpets.mixins;

import com.outurnate.wanderingpets.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import com.outurnate.wanderingpets.interfaces.IFollowsAccessor;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(FollowOwnerGoal.class)
public abstract class FollowOwnerGoalMixin extends Goal
{

    @Final
    @Shadow
    private TamableAnimal tamable;

    @Shadow public abstract void stop();

    @Shadow public abstract boolean canUse();

    @Shadow @Nullable private LivingEntity owner;

    @Inject(at = @At("HEAD"), method = "canContinueToUse", cancellable = true)
    public void canContinueToUse(CallbackInfoReturnable<Boolean> cir)
    {
    	boolean shouldFollow =
    			((IFollowsAccessor)this.tamable).isAllowedToFollow() && Config.isWanderBehaviorEnabled(this.tamable);
        if (!shouldFollow) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "canUse", cancellable = true)
    public void canUse(CallbackInfoReturnable<Boolean> cir)
    {
        boolean shouldFollow =
                ((IFollowsAccessor)this.tamable).isAllowedToFollow() && Config.isWanderBehaviorEnabled(this.tamable);
        if (!shouldFollow) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
