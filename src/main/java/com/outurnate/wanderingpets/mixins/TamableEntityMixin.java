package com.outurnate.wanderingpets.mixins;

import com.outurnate.wanderingpets.interfaces.IFollowsAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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

import java.util.Optional;

@Mixin(TamableAnimal.class)
public abstract class TamableEntityMixin extends Animal implements IFollowsAccessor {

    private static final EntityDataAccessor<Boolean> FOLLOWS = SynchedEntityData.defineId(TamableEntityMixin.class, EntityDataSerializers.BOOLEAN);

    protected TamableEntityMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "defineSynchedData")
    protected void defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(FOLLOWS, true);
    }

    @Inject(at = @At("TAIL"), method = "addAdditionalSaveData")
    protected void onAddAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
        output.putBoolean("DoesFollow", this.entityData.get(FOLLOWS));
    }

    @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
    protected void onReadAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        boolean follows = input.getBooleanOr("DoesFollow", true);
        this.entityData.set(FOLLOWS, follows);
    }

    @Override
    public boolean isAllowedToFollow() {
        return this.entityData.get(FOLLOWS);
    }

    @Override
    public void setAllowedToFollow(boolean value) {
        this.entityData.set(FOLLOWS, value);
    }
}
