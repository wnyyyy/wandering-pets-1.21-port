package com.outurnate.wanderingpets.mixins;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterAvoidingRandomStrollGoal.class)
public abstract class WaterAvoidingRandomStrollGoalMixin extends RandomStrollGoal {
    @Shadow
    @Final
    protected float probability;

    public WaterAvoidingRandomStrollGoalMixin(PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier);
    }

    @Inject(at = @At("TAIL"), method = "getPosition", cancellable = true)
    protected void getPosition(CallbackInfoReturnable<Vec3> cir) {
//        int multiplier = 3;
//        if (!mob.isInWaterOrBubble()) {
//            cir.setReturnValue(mob.getRandom().nextFloat() >= this.probability ?
//                    LandRandomPos.getPos(mob, 10*multiplier, 7) :
//                    wpets$getPosition(mob, multiplier));
//            cir.cancel();
//        }
    }

    @Unique
    private Vec3 wpets$getPosition(PathfinderMob mob, int mult) {
        return LandRandomPos.getPos(mob, 10 * mult, 7);
    }
}
