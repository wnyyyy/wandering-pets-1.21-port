package com.outurnate.wanderingpets.mixins;

import com.outurnate.wanderingpets.interfaces.IMoveToBlockGoalAccessor;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.CatLieOnBedGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CatLieOnBedGoal.class)
public abstract class CatLieOnBedGoalMixin extends MoveToBlockGoal
{
    public CatLieOnBedGoalMixin(PathfinderMob mob, double speedModifier, int searchRange) {
        super(mob, speedModifier, searchRange);
    }

    @Inject(at = @At("HEAD"), method = "canUse", cancellable = true)
    public void canUse(CallbackInfoReturnable<Boolean> cir) {
        //cir.setReturnValue(false);
        //cir.cancel();
    }
}
