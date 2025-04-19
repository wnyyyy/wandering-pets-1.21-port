package com.outurnate.wanderingpets.mixins;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.CatLieOnBedGoal;
import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CatLieOnBedGoal.class)
public abstract class CatLieOnBedGoalMixin {

    @Final
    @Shadow
    private Cat cat;

    @Inject(at = @At("TAIL"), method = "nextStartTick", cancellable = true)
    public void nextStartTick(CallbackInfoReturnable<Integer> cir) {
        int delay = Mth.positiveCeilDiv((2000 + cat.getRandom().nextInt(2000)), 2);
        cir.setReturnValue(delay);
        cir.cancel();
    }
}
