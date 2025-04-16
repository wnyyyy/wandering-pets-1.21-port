package com.outurnate.wanderingpets.mixins;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;

@Mixin(CatSitOnBlockGoal.class)
public abstract class CatSitOnBlockGoalMixin extends MoveToBlockGoal {

    @Mutable
    void setSearchRange(int value) {

    }

    public CatSitOnBlockGoalMixin(PathfinderMob mob, double speedModifier, int searchRange) {
        super(mob, speedModifier, 32);
    }
}
