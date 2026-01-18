package com.wnyyy.wanderingpets.mixin;

import com.wnyyy.wanderingpets.duck.IWanderingTamableAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.wnyyy.wanderingpets.Constants.SHOULD_WANDER_KEY;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements IWanderingTamableAccessor {

    @Unique
    private boolean wanderingpets$shouldWander = false;

    @Override
    public boolean wanderingpets$shouldWander() {
        return wanderingpets$shouldWander;
    }

    @Override
    public void wanderingpets$setShouldWander(boolean wandering) {
        this.wanderingpets$shouldWander = wandering;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void wanderingpets$saveData(CompoundTag compound, CallbackInfo ci) {
        if (this instanceof OwnableEntity) {
            compound.putBoolean(SHOULD_WANDER_KEY, this.wanderingpets$shouldWander);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void wanderingpets$loadData(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains(SHOULD_WANDER_KEY)) {
            this.wanderingpets$shouldWander = compound.getBoolean(SHOULD_WANDER_KEY);
        }
    }
}