package com.outurnate.wanderingpets;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import com.outurnate.wanderingpets.interfaces.IFollowsAccessor;
@Mod(WanderingPets.MODID)
public class WanderingPets
{
    public static final String MODID = "wanderingpets";

	public WanderingPets(ModContainer modContainer) {
		modContainer.registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC);

		NeoForge.EVENT_BUS.addListener(this::onEntityInteract);
	}

	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {

		if (event.getSide() == LogicalSide.SERVER &&
			event.getTarget() instanceof TamableAnimal pet &&
			Config.isWanderBehaviorEnabled(pet) &&
			event.getEntity() instanceof Player player &&
			player.isShiftKeyDown() &&
			player.getUUID().equals(pet.getOwnerUUID())) {

			IFollowsAccessor followsAccessor = (IFollowsAccessor) pet;
			boolean shouldFollow = !followsAccessor.isAllowedToFollow();
			followsAccessor.setAllowedToFollow(shouldFollow);

			player.sendSystemMessage(Component.translatable(
					shouldFollow ? "wanderingpets.follow" : "wanderingpets.unfollow", pet.getName()
			));

			event.setCanceled(true);
		}
	}
}
