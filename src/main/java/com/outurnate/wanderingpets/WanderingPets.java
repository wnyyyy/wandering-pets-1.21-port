package com.outurnate.wanderingpets;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.outurnate.wanderingpets.interfaces.IFollowsAccessor;

@Mod(WanderingPets.MODID)
public class WanderingPets
{
    public static final String MODID = "wanderingpets";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public WanderingPets(ModContainer modContainer) {
		modContainer.registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC);

		NeoForge.EVENT_BUS.addListener(this::onLivingDeath);
		NeoForge.EVENT_BUS.addListener(this::onEntityInteract);
	}

	public void onLivingDeath(LivingDeathEvent event) {
		if (!Config.GENERAL.enablePetRespawn.getAsBoolean()) return;

		if (event.getEntity() instanceof TamableAnimal tamable && !tamable.level().isClientSide()) {
			if (!tamable.isTame()) return;

			event.setCanceled(true);

			((IFollowsAccessor) tamable).setAllowedToFollow(true);

			LivingEntity owner = tamable.getOwner();
			if (owner instanceof Player player) {
				player.sendSystemMessage(Component.translatable("wanderingpets.petDeath"));
			}

			tamable.setHealth(tamable.getMaxHealth());

			for (MobEffectInstance effectInstance : tamable.getActiveEffects()) {
				tamable.removeEffect(effectInstance.getEffect());
			}

			if (owner != null) {
				owner.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 18000));
				owner.addEffect(new MobEffectInstance(MobEffects.HUNGER, 18000));
				owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 18000));
			}
		}
	}

	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {

		if (!Config.GENERAL.enableToggleFollow.getAsBoolean()) return;

		if (event.getSide() == LogicalSide.SERVER &&
				event.getTarget() instanceof TamableAnimal pet &&
				event.getEntity() instanceof Player player &&
				player.isShiftKeyDown() &&
				player.getUUID().equals(pet.getOwnerUUID())) {

			IFollowsAccessor followsAccessor = (IFollowsAccessor) pet;
			boolean newState = !followsAccessor.isAllowedToFollow();
			followsAccessor.setAllowedToFollow(newState);

			player.sendSystemMessage(Component.translatable(
					newState ? "wanderingpets.follow" : "wanderingpets.unfollow"
			));

			event.setCanceled(true);
		}
	}
}
