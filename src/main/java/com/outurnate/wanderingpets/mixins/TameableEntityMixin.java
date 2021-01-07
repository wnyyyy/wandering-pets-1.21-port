package com.outurnate.wanderingpets.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.outurnate.wanderingpets.interfaces.IFollowsAccessor;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TameableEntity.class)
public abstract class TameableEntityMixin extends AnimalEntity implements IFollowsAccessor
{
	private static DataParameter<Boolean> FOLLOWS;
	
	protected TameableEntityMixin(EntityType<? extends AnimalEntity> type, World worldIn)
	{
		super(type, worldIn);
	}
	
    @Inject(at = @At("TAIL"), method = "<clinit>")
    static private void injectStatic(CallbackInfo callbackInfo)
    {
    	FOLLOWS = EntityDataManager.createKey(TameableEntity.class, DataSerializers.BOOLEAN);
    }
	
	@Inject(at = @At("TAIL"), method = "registerData")
	private void injectRegisterData(CallbackInfo info)
	{
		this.dataManager.register(FOLLOWS, true);
	}

	@Inject(at = @At("TAIL"), method = "writeAdditional")
	public void injectWriteAdditional(CompoundNBT compound, CallbackInfo callbackInfo)
	{
		compound.putBoolean("DoesFollow", this.isAllowedToFollow());
	}

	@Inject(at = @At("TAIL"), method = "readAdditional")
	public void injectReadAdditional(CompoundNBT compound, CallbackInfo callbackInfo)
	{
		this.setAllowedToFollow(compound.getBoolean("DoesFollow"));
	}
	
    @Override
    public boolean isAllowedToFollow()
    {
        return this.dataManager.get(FOLLOWS);
    }

    @Override
    public void setAllowedToFollow(boolean value)
    {
        this.dataManager.set(FOLLOWS, value);
    }
}
