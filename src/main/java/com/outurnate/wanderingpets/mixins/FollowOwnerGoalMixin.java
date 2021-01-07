package com.outurnate.wanderingpets.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.outurnate.wanderingpets.WanderingPets.Config;
import com.outurnate.wanderingpets.interfaces.IFollowsAccessor;

import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableEntity;

import org.spongepowered.asm.mixin.injection.At;

@Mixin(FollowOwnerGoal.class)
public abstract class FollowOwnerGoalMixin extends Goal
{
    @Shadow TameableEntity tameable;
    @Shadow float maxDist;
    @Shadow float minDist;
    
    @Inject(at = @At("HEAD"), method = "tick")
    public void delayTick(CallbackInfo ci)
    {
    	boolean shouldFollow =
    			// Mask the entity's follow property with the config option
    			(!Config.enableToggleFollow.get() || ((IFollowsAccessor)this.tameable).isAllowedToFollow())
    			// Check if the entity type must follow
    			&& Config.shouldEntityFollow(this.tameable);
        if (!shouldFollow)
        {
        	this.maxDist = Float.MAX_VALUE;
        	this.minDist = Float.MAX_VALUE;
        }
        else
        {
        	this.maxDist = 10.0F;
        	this.minDist = 5.0F;
        }
    }
}
