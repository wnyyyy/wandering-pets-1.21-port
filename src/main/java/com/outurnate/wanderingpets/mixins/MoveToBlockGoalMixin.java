package com.outurnate.wanderingpets.mixins;

import com.outurnate.wanderingpets.Config;
import com.outurnate.wanderingpets.interfaces.ICatWanderBehaviorAccessor;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.CatLieOnBedGoal;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Cat;
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

    @SuppressWarnings("ConstantConditions")
    @Inject(at = @At("TAIL"), method = "canContinueToUse", cancellable = true)
    public void canContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        if (Config.isBetterCatBehaviorEnabled() && mob instanceof Cat && ((Cat) mob).isTame() && ((Cat) mob).getOwner() != null && reachedTarget) {
            ICatWanderBehaviorAccessor catAccessor = (ICatWanderBehaviorAccessor) mob;
            boolean canContinue = true;
            MoveToBlockGoal self = (MoveToBlockGoal) (Object) this;
            if (self instanceof CatSitOnBlockGoal) {
                wpets$sittingTicks++;
                canContinue = wpets$sittingTicks <= Config.CatsRelaxingProfile.sitDur();
                if (wpets$sittingTicks % 50 == 0) {
                    Config.log("canContinueToUse - Sit | {}/{}", wpets$sittingTicks, Config.CatsRelaxingProfile.sitDur());
                }
                catAccessor.setNotSittedTicks(0);
            } else if (self instanceof CatLieOnBedGoal) {
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

    @SuppressWarnings("ConstantConditions")
    @Inject(at = @At("TAIL"), method = "canUse", cancellable = true)
    public void canUse(CallbackInfoReturnable<Boolean> cir) {
        if (Config.isBetterCatBehaviorEnabled() && mob instanceof Cat && ((Cat) mob).isTame() && ((Cat) mob).getOwner() != null) {
            ICatWanderBehaviorAccessor catAccessor = (ICatWanderBehaviorAccessor) mob;
            boolean bCanUse = true;
            MoveToBlockGoal self = (MoveToBlockGoal) (Object) this;

            if (self instanceof CatSitOnBlockGoal) {
                bCanUse = catAccessor.getNotSittedTicks() > Config.CatsRelaxingProfile.sitCd();
            } else if (self instanceof CatLieOnBedGoal) {
                bCanUse = catAccessor.getNotSleptTicks() > Config.CatsRelaxingProfile.sleepCd();
            }
            if (!bCanUse) {
                wpets$sittingTicks = 0;
                wpets$sleepingTicks = 0;
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(at = @At("HEAD"), method = "acceptedDistance", cancellable = true)
    public void acceptedDistance(CallbackInfoReturnable<Double> cir) {
        MoveToBlockGoal self = (MoveToBlockGoal) (Object) this;
        if (Config.isBetterCatBehaviorEnabled() && mob instanceof Cat cat && cat.isTame() && cat.getOwner() != null &&  self instanceof CatLieOnBedGoal) {
            cir.setReturnValue(1.45);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @ModifyVariable(method = "findNearestBlock", at = @At("STORE"), ordinal = 0, index = 1)
    private int overrideSearchRange(int original) {
        MoveToBlockGoal self = (MoveToBlockGoal) (Object) this;
        if (Config.isBetterCatBehaviorEnabled() && (self instanceof CatLieOnBedGoal || self instanceof CatSitOnBlockGoal)) {
            return 24;
        }
        return original;
    }
}