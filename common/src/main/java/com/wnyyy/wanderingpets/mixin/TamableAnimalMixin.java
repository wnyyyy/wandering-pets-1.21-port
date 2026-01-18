package com.wnyyy.wanderingpets.mixin;

import com.wnyyy.wanderingpets.duck.IWanderingTamableAccessor;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.wnyyy.wanderingpets.Constants.SHOULD_WANDER_KEY;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin implements IWanderingTamableAccessor {

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
    private void wanderingpets$saveData(ValueOutput output, CallbackInfo ci) {
        output.putBoolean(SHOULD_WANDER_KEY, this.wanderingpets$shouldWander);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void wanderingpets$loadData(ValueInput input, CallbackInfo ci) {
        this.wanderingpets$shouldWander = input.getBooleanOr(SHOULD_WANDER_KEY, false);
    }
}