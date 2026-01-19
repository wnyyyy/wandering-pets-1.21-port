package com.wnyyy.wanderingpets.mixin.cat;

import com.wnyyy.wanderingpets.config.ModConfig;
import com.wnyyy.wanderingpets.duck.ICatWanderAccessor;
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

import static com.wnyyy.wanderingpets.Constants.LOG;

@Mixin(MoveToBlockGoal.class)
public abstract class MoveToBlockGoalMixin {

    @Final
    @Shadow
    protected PathfinderMob mob;

    @Shadow
    private boolean reachedTarget;

    @Unique
    private int wanderingpets$sittingTicks = 0;

    @Unique
    private int wanderingpets$sleepingTicks = 0;

    @SuppressWarnings("ConstantConditions")
    @Inject(at = @At("TAIL"), method = "canContinueToUse", cancellable = true)
    public void canContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.isBetterCatBehaviorEnabled() && mob instanceof Cat && ((Cat) mob).isTame() && ((Cat) mob).getOwner() != null && reachedTarget) {
            ICatWanderAccessor catAccessor = (ICatWanderAccessor) mob;
            boolean canContinue = true;
            MoveToBlockGoal self = (MoveToBlockGoal) (Object) this;
            if (self instanceof CatSitOnBlockGoal) {
                wanderingpets$sittingTicks++;
                canContinue = wanderingpets$sittingTicks <= ModConfig.CatsRelaxingProfile.sitDur();
                if (ModConfig.isDebugMode() && wanderingpets$sittingTicks % 50 == 0) {
                    LOG.info("canContinueToUse - Sit | {}/{}", wanderingpets$sittingTicks, ModConfig.CatsRelaxingProfile.sitDur());
                }
                catAccessor.wanderingpets$setNotSittedTicks(0);
            } else if (self instanceof CatLieOnBedGoal) {
                wanderingpets$sleepingTicks++;
                canContinue = wanderingpets$sleepingTicks <= ModConfig.CatsRelaxingProfile.sleepDur();
                if (ModConfig.isDebugMode() && wanderingpets$sleepingTicks % 50 == 0) {
                    LOG.info("canContinueToUse - Sleep | {}/{}", wanderingpets$sleepingTicks, ModConfig.CatsRelaxingProfile.sleepDur());
                }
                catAccessor.wanderingpets$setNotSleptTicks(0);
            }
            if (!canContinue) {
                wanderingpets$sittingTicks = 0;
                wanderingpets$sleepingTicks = 0;
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(at = @At("TAIL"), method = "canUse", cancellable = true)
    public void canUse(CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.isBetterCatBehaviorEnabled() && mob instanceof Cat && ((Cat) mob).isTame() && ((Cat) mob).getOwner() != null) {
            boolean bCanUse = wanderingpets$isCanUse();
            if (!bCanUse) {
                wanderingpets$sittingTicks = 0;
                wanderingpets$sleepingTicks = 0;
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

    @Unique
    private boolean wanderingpets$isCanUse() {
        ICatWanderAccessor catAccessor = (ICatWanderAccessor) mob;
        boolean bCanUse = true;
        MoveToBlockGoal self = (MoveToBlockGoal) (Object) this;

        if (self instanceof CatSitOnBlockGoal) {
            bCanUse = catAccessor.wanderingpets$getNotSittedTicks() > ModConfig.CatsRelaxingProfile.sitCd();
        } else if (self instanceof CatLieOnBedGoal) {
            bCanUse = catAccessor.wanderingpets$getNotSleptTicks() > ModConfig.CatsRelaxingProfile.sleepCd();
        }
        return bCanUse;
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(at = @At("HEAD"), method = "acceptedDistance", cancellable = true)
    public void acceptedDistance(CallbackInfoReturnable<Double> cir) {
        MoveToBlockGoal self = (MoveToBlockGoal) (Object) this;
        if (ModConfig.isBetterCatBehaviorEnabled() && mob instanceof Cat cat && cat.isTame() && cat.getOwner() != null &&  (self instanceof CatLieOnBedGoal || self instanceof  CatSitOnBlockGoal)) {
            cir.setReturnValue(1.45);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @ModifyVariable(method = "findNearestBlock", at = @At("STORE"), ordinal = 0)
    private int overrideSearchRange(int original) {
        MoveToBlockGoal self = (MoveToBlockGoal) (Object) this;
        if (ModConfig.isBetterCatBehaviorEnabled() && (self instanceof CatLieOnBedGoal || self instanceof CatSitOnBlockGoal)) {
            return original*2;
        }
        return original;
    }
}