package com.outurnate.wanderingpets.mixins;

import com.outurnate.wanderingpets.interfaces.ICatWanderBehaviorAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
public abstract class MoveToBlockGoalMixin implements MoveToBlockGoalAccessor {

    @Final
    @Shadow
    protected PathfinderMob mob;

    @Shadow
    protected int nextStartTick;

    @Shadow
    protected BlockPos blockPos;

    @Shadow
    private boolean reachedTarget;

    @Unique
    private int wpets$sittingTicks = 0;

    @Unique
    private int wpets$sleepingTicks = 0;

    @Inject(at = @At("TAIL"), method = "canContinueToUse", cancellable = true)
    public void canContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        if (mob instanceof Cat && ((Cat) mob).isTame() && ((Cat) mob).getOwner() != null && reachedTarget) {
            ICatWanderBehaviorAccessor catAccessor = (ICatWanderBehaviorAccessor) mob;
            boolean canContinue = true;
            if ((Object) this instanceof CatSitOnBlockGoal) {
                wpets$sittingTicks++;
                canContinue = wpets$sittingTicks <= 400;
                if (wpets$sittingTicks % 50 == 0) {
                    ((Cat) mob).getOwner().sendSystemMessage(Component.translatable(String.format("CanContinueSit | %s -- %s", wpets$sittingTicks, 400)));
                }
                catAccessor.setNotSittedTicks(0);
            } else if ((Object) this instanceof CatLieOnBedGoal) {
                wpets$sleepingTicks++;
                canContinue = wpets$sleepingTicks <= 800;
                if (wpets$sleepingTicks % 50 == 0) {
                    ((Cat) mob).getOwner().sendSystemMessage(Component.translatable(String.format("CanContinueSleep | %s -- %s", wpets$sleepingTicks, 800)));
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
        if (mob instanceof Cat && ((Cat) mob).isTame() && ((Cat) mob).getOwner() != null) {
            ICatWanderBehaviorAccessor catAccessor = (ICatWanderBehaviorAccessor) mob;
            boolean canUse = true;
            if ((Object) this instanceof CatSitOnBlockGoal) {
                canUse = catAccessor.getNotSittedTicks() > 1600;
            } else if ((Object) this instanceof CatLieOnBedGoal) {
                canUse = catAccessor.getNotSleptTicks() > 3200;
            }
            if (!canUse) {
                wpets$sittingTicks = 0;
                wpets$sleepingTicks = 0;
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

//    @Inject(at = @At("TAIL"), method = "nextStartTick", cancellable = true)
//    protected void nextStartTick(PathfinderMob creature, CallbackInfoReturnable<Integer> cir) {
//        if (mob instanceof Cat && ((Cat) mob).isTame() && ((Cat) mob).getOwner() != null) {
//            if (!((Object) this instanceof CatSitOnBlockGoal)) {
//                return;
//            }
//            int delay = Mth.positiveCeilDiv((1600 + creature.getRandom().nextInt(1600)), 2);
//            cir.setReturnValue(delay);
//            cir.cancel();
//        }
//    }

    @Inject(at = @At("HEAD"), method = "acceptedDistance", cancellable = true)
    public void acceptedDistance(CallbackInfoReturnable<Double> cir) {
        if (mob instanceof Cat cat && cat.isTame() && cat.getOwner() != null && (Object) this instanceof CatLieOnBedGoal) {
            cir.setReturnValue(1.45);
        }
    }

    @ModifyVariable(method = "findNearestBlock", at = @At("STORE"), ordinal = 0, index = 1)
    private int overrideSearchRange(int original) {
        if ((Object) this instanceof CatLieOnBedGoal || (Object) this instanceof CatSitOnBlockGoal) {
            return 24;
        }
        return original;
    }

    @Override
    public int getNextStartTick() {
        return this.nextStartTick;
    }
}