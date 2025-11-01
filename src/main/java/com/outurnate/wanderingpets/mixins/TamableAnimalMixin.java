package com.outurnate.wanderingpets.mixins;

import com.outurnate.wanderingpets.interfaces.IFollowsAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin extends Animal implements IFollowsAccessor {

    private static final String ALLOWED_TO_FOLLOW_KEY = "WanderingPets_AllowedToFollow";

    private boolean allowedToFollow = true;

    protected TamableAnimalMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isAllowedToFollow() {
        return this.allowedToFollow;
    }

    @Override
    public void setAllowedToFollow(boolean allowed) {
        this.allowedToFollow = allowed;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void onAddAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
        output.putBoolean(ALLOWED_TO_FOLLOW_KEY, this.allowedToFollow);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void onReadAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        input.getBooleanOr(ALLOWED_TO_FOLLOW_KEY, this.allowedToFollow);
    }
}
