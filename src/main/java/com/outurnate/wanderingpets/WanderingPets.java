package com.outurnate.wanderingpets;

import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;

@Mod(WanderingPets.MODID)
public class WanderingPets
{
    public static final String MODID = "wanderingpets";
    
    public WanderingPets()
    {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(Type.COMMON, Config.CONFIG_SPEC);
    }
    
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        if ((Config.disableFollowingCats.get()    && event.getEntity() instanceof CatEntity) ||
        	(Config.disableFollowingWolves.get()  && event.getEntity() instanceof WolfEntity) ||
        	(Config.disableFollowingParrots.get() && event.getEntity() instanceof ParrotEntity))
        {
        	TameableEntity tamable = (TameableEntity)event.getEntity();
        	
            Optional<PrioritizedGoal> follow = tamable.goalSelector.goals.stream().filter((goal) ->
            {
                return goal.getGoal() instanceof FollowOwnerGoal;
            }).findFirst();
            if (follow.isPresent())
            	tamable.goalSelector.removeGoal(follow.get().getGoal());
        }
    }
    
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
    	if (Config.enablePetRespawn.get() && event.getEntityLiving() instanceof TameableEntity && event.getEntityLiving().isServerWorld())
    	{
    		TameableEntity tamable = (TameableEntity)event.getEntity();
    		
    		if (tamable.isTamed())
    		{
    			event.setCanceled(true);
    			tamable.setHealth(tamable.getMaxHealth());
    			tamable.getActivePotionEffects().stream().map((EffectInstance effect) ->
    			{
    				return effect.getPotion();
    			}).collect(Collectors.toList()).stream().forEach((Effect effect) ->
    			{
    				tamable.removePotionEffect(effect);
    			});
    			
    			LivingEntity owner = tamable.getOwner();
    			if (owner != null)
    			{
    				owner.addPotionEffect(new EffectInstance(Effects.UNLUCK, 18000));
    				owner.addPotionEffect(new EffectInstance(Effects.HUNGER, 18000));
    				owner.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 18000));
    			}
    		}
    	}
    }
    
    private static class Config
    {
		public static final BooleanValue disableFollowingCats;
		public static final BooleanValue disableFollowingWolves;
		public static final BooleanValue disableFollowingParrots;
		public static final BooleanValue enablePetRespawn;
	
		public static final ForgeConfigSpec CONFIG_SPEC;
	
		static
		{
			ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
			disableFollowingCats = builder
					.comment("Disables following for cats")
					.define("disableCatFollow", true);
			disableFollowingWolves = builder
					.comment("Disables following for wolves")
					.define("disableWolfFollow", true);
			disableFollowingParrots = builder
					.comment("Disables following for parrots")
					.define("disableParrotFollow", true);
			enablePetRespawn = builder
					.comment("Allows pets to respawn with player debuff")
					.define("enablePetRespawn", false);
			CONFIG_SPEC = builder.build();
		}
    }
}
