package com.outurnate.wanderingpets.mixins;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(RandomStrollGoal.class)
public abstract class RandomStrollGoalMixin extends Goal
{
    @Final
    @Shadow
    protected PathfinderMob mob;

    @Unique
    private int wandering_pets_1_21_port$tickCount = 0;

    @Inject(at = @At("HEAD"), method = "canUse")
    public void canUse(CallbackInfoReturnable<Boolean> cir) {
        if (mob instanceof Cat && ((Cat) mob).isTame() ) {
            wandering_pets_1_21_port$tickCount++;
            Objects.requireNonNull(((Cat) mob).getOwner()).sendSystemMessage(Component.translatable(String.format("canUseRandomStroll#%s", wandering_pets_1_21_port$tickCount)));
        }
    }
}
