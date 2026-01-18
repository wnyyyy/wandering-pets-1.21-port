package com.wnyyy.wanderingpets.compat;

import com.wnyyy.wanderingpets.duck.compat.TameableFoxesAccessor;
import com.wnyyy.wanderingpets.platform.Services;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.fox.Fox;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ModCompat {
    public static String getNewTameableVanilla() {
        String newMobs = "";
        if (Services.PLATFORM.isModLoaded("tameablefoxes")) {
            newMobs += "fox:";
        }
        return newMobs;
    }

    public static Optional<UUID> tryGetOwnerUUID(LivingEntity mob) {
        if (mob instanceof OwnableEntity) {
            return Optional.of(Objects.requireNonNull(((OwnableEntity) mob).getOwner()).getUUID());
        }
        if (mob instanceof Fox fox) {
            UUID ownerId = ((TameableFoxesAccessor) fox).wanderingpets$getFoxOwner();

            if (ownerId != null) {
                return Optional.of(ownerId);
            }
        }
        return Optional.empty();
    }
}
