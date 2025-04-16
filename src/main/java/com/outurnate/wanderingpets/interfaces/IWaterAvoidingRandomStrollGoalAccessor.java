package com.outurnate.wanderingpets.interfaces;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WaterAvoidingRandomStrollGoal.class)
public interface IWaterAvoidingRandomStrollGoalAccessor  {

    @Accessor("mob")
    PathfinderMob getMob();
}
