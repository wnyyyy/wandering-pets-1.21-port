package com.outurnate.wanderingpets.mixins;

import com.outurnate.wanderingpets.interfaces.IMobAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mob.class)
public abstract class MobAccessorMixin implements IMobAccessor {

    @Accessor("goalSelector")
    @Override
    public abstract GoalSelector getGoalSelector();
}
