package com.outurnate.wanderingpets.mixins;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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
public class MoveToBlockGoalMixin {

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
        if (mob instanceof Cat && ((Cat) mob).isTame() && ((Cat) mob).getOwner() != null && reachedTarget) {
            boolean canContinue = true;
            if ((Object) this instanceof CatSitOnBlockGoal) {
                wpets$sleepingTicks = 0;
                wpets$sittingTicks++;
                canContinue = wpets$sittingTicks <= 400;
            } else if ((Object) this instanceof CatLieOnBedGoal) {
                wpets$sittingTicks = 0;
                wpets$sleepingTicks++;
                canContinue = wpets$sleepingTicks <= 2000;
            }
            ((Cat) mob).getOwner().sendSystemMessage(Component.translatable(String.format("CanContinueSit#%s", wpets$sittingTicks)));
            if (!canContinue) {
                wpets$sittingTicks = 0;
                wpets$sleepingTicks = 0;
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

    //todo: sleep cooldown on canUse

    @Inject(at = @At("TAIL"), method = "nextStartTick", cancellable = true)
    protected void nextStartTick(PathfinderMob creature, CallbackInfoReturnable<Integer> cir) {
        if (mob instanceof Cat && ((Cat) mob).isTame() && ((Cat) mob).getOwner() != null) {
            int delay = Mth.positiveCeilDiv((1400 + creature.getRandom().nextInt(1400)), 2);
            ((Cat) mob).getOwner().sendSystemMessage(Component.translatable(String.format("nextStartTick#%s", delay)));
            cir.setReturnValue(delay);
            cir.cancel();
        }
    }

    @ModifyVariable(
            method = "findNearestBlock",
            at = @At("STORE"),
            ordinal = 0,
            index = 1
    )
    private int overrideSearchRange(int original) {
        if ((Object) this instanceof CatLieOnBedGoal || (Object) this instanceof CatSitOnBlockGoal) {
            return 32;
        }
        return original;
    }
}