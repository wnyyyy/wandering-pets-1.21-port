package com.outurnate.wanderingpets.mixins;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Cat.class)
public abstract class CatMixin extends TamableAnimal
{
    @Shadow public abstract boolean isLying();

    private CatMixin cat;

    protected CatMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.cat = this;
    }
}
