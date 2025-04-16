package com.outurnate.wanderingpets.interfaces;

import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MoveToBlockGoal.class)
public interface IMoveToBlockGoalAccessor {

    @Accessor("searchRange")
    @Mutable
    void setSearchRange(int searchRange);
}