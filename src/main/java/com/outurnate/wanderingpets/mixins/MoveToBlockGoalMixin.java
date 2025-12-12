package com.outurnate.wanderingpets.mixins;

import com.outurnate.wanderingpets.Config;
import com.outurnate.wanderingpets.interfaces.ICatWanderBehaviorAccessor;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.CatLieOnBedGoal;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.feline.Cat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MoveToBlockGoal.class)
public abstract class MoveToBlockGoalMixin {

    @Final
    @Shadow
    protected PathfinderMob mob;

    @Shadow
    private boolean reachedTarget;

    @Unique
    private int wpets$sittingTicks = 0;

    @Unique
    private int wpets$sleepingTicks = 0;

    @Inject(at = @At("TAIL"), method = "canContinueToUse", cancellable = true)
    public void canContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        if (Config.GENERAL.betterCatBehavior.get() && mob instanceof Cat && ((Cat) mob).isTame() && ((Cat) mob).getOwner() != null && reachedTarget) {
            ICatWanderBehaviorAccessor catAccessor = (ICatWanderBehaviorAccessor) mob;
            boolean canContinue = true;
            if ((Object) this instanceof CatSitOnBlockGoal) {
                wpets$sittingTicks++;
                canContinue = wpets$sittingTicks <= Config.CatsRelaxingProfile.sitDur();
                if (wpets$sittingTicks % 50 == 0) {
                    Config.log("canContinueToUse - Sit | {}/{}", wpets$sittingTicks, Config.CatsRelaxingProfile.sitDur());
                }
                catAccessor.setNotSittedTicks(0);
            } else if ((Object) this instanceof CatLieOnBedGoal) {
                wpets$sleepingTicks++;
                canContinue = wpets$sleepingTicks <= Config.CatsRelaxingProfile.sleepDur();
                if (wpets$sleepingTicks % 50 == 0) {
                    Config.log("canContinueToUse - Sleep | {}/{}", wpets$sleepingTicks, Config.CatsRelaxingProfile.sleepDur());
                }
                catAccessor.setNotSleptTicks(0);
            }
            if (!canContinue) {
                wpets$sittingTicks = 0;
                wpets$sleepingTicks = 0;
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "canUse", cancellable = true)
    public void canUse(CallbackInfoReturnable<Boolean> cir) {
        if (Config.GENERAL.betterCatBehavior.get() && mob instanceof Cat && ((Cat) mob).isTame() && ((Cat) mob).getOwner() != null) {
            ICatWanderBehaviorAccessor catAccessor = (ICatWanderBehaviorAccessor) mob;
            boolean canUse = true;
            if ((Object) this instanceof CatSitOnBlockGoal) {
                canUse = catAccessor.getNotSittedTicks() > Config.CatsRelaxingProfile.sitCd();
            } else if ((Object) this instanceof CatLieOnBedGoal) {
                canUse = catAccessor.getNotSleptTicks() > Config.CatsRelaxingProfile.sleepCd();
            }
            if (!canUse) {
                wpets$sittingTicks = 0;
                wpets$sleepingTicks = 0;
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "acceptedDistance", cancellable = true)
    public void acceptedDistance(CallbackInfoReturnable<Double> cir) {
        if (Config.GENERAL.betterCatBehavior.get() && mob instanceof Cat cat && cat.isTame() && cat.getOwner() != null && (Object) this instanceof CatLieOnBedGoal) {
            cir.setReturnValue(1.45);
        }
    }

    @ModifyVariable(method = "findNearestBlock", at = @At("STORE"), ordinal = 0, index = 1)
    private int overrideSearchRange(int original) {
        if (Config.GENERAL.betterCatBehavior.get() && ((Object) this instanceof CatLieOnBedGoal || (Object) this instanceof CatSitOnBlockGoal)) {
            return 24;
        }
        return original;
    }
}