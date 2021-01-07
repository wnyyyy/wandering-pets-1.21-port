package com.outurnate.wanderingpets;

import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.outurnate.wanderingpets.interfaces.IFollowsAccessor;

import net.minecraft.entity.Entity;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;

@Mod(WanderingPets.MODID)
public class WanderingPets
{
    public static final String MODID = "wanderingpets";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
    
    public WanderingPets()
    {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(Type.COMMON, Config.CONFIG_SPEC);
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
    			
    			((IFollowsAccessor)tamable).setAllowedToFollow(true);

				tamable.getOwner().sendMessage(new TranslationTextComponent("wanderingpets.petDeath"), null);
				
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
    
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event)
    {
    	if (Config.enableToggleFollow.get())
    	{
	    	if (event.getSide() == LogicalSide.SERVER && event.getTarget() instanceof TameableEntity)
			{
				TameableEntity pet = (TameableEntity)event.getTarget();
				if (event.getPlayer().getUniqueID().equals(pet.getOwnerId()) && event.getPlayer().isSneaking())
				{
					IFollowsAccessor followsAccessor = (IFollowsAccessor)pet; 
					followsAccessor.setAllowedToFollow(!followsAccessor.isAllowedToFollow());
					
					event.getPlayer().sendMessage(new TranslationTextComponent(
							followsAccessor.isAllowedToFollow() ?
									"wanderingpets.follow" :
									"wanderingpets.unfollow"), null);
					
					event.setCanceled(true);
				}
			}
    	}
    }
    
    public static class Config
    {
		public static final BooleanValue disableFollowingCats;
		public static final BooleanValue disableFollowingWolves;
		public static final BooleanValue disableFollowingParrots;
		public static final BooleanValue enablePetRespawn;
		public static final BooleanValue enableToggleFollow;
	
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
			enableToggleFollow = builder
					.comment("Changes shift+right click behaviour to toggle following")
					.define("enableToggleFollow", true);
			CONFIG_SPEC = builder.build();
		}
		
		public static boolean shouldEntityFollow(Entity entity)
		{
			return 	(disableFollowingCats.get()    && entity instanceof CatEntity) ||
					(disableFollowingWolves.get()  && entity instanceof WolfEntity) ||
        			(disableFollowingParrots.get() && entity instanceof ParrotEntity);
		}
    }
}
