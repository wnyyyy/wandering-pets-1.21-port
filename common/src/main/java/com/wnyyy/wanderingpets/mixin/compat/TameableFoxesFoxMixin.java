package com.wnyyy.wanderingpets.mixin.compat;

import com.wnyyy.wanderingpets.duck.compat.TameableFoxesAccessor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.fox.Fox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;
import java.util.UUID;

@Mixin(Fox.class)
public abstract class TameableFoxesFoxMixin implements TameableFoxesAccessor {
    @Shadow
    @Final
    private static EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_0;

    @Unique
    public UUID wanderingpets$getFoxOwner() {
        return ((Fox)(Object)this).getEntityData().get(DATA_TRUSTED_ID_0).orElse(null);
    }
}