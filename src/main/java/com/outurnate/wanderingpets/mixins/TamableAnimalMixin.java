package com.outurnate.wanderingpets.mixins;

import com.outurnate.wanderingpets.interfaces.IFollowsAccessor;
import com.outurnate.wanderingpets.data.ModAttachments;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin extends Animal implements IFollowsAccessor {

    protected TamableAnimalMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isAllowedToFollow() {
        return this.getData(ModAttachments.SHOULD_FOLLOW);
    }

    @Override
    public void setAllowedToFollow(boolean value) {
        this.setData(ModAttachments.SHOULD_FOLLOW, value);
    }
}
