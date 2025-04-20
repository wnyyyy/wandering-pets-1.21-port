package com.outurnate.wanderingpets;

import com.outurnate.wanderingpets.interfaces.IFollowsAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Mod(WanderingPets.MODID)
public class WanderingPets {

    public static final String MODID = "wanderingpets";
    public static final Logger LOGGER = LoggerFactory.getLogger("WanderingPets");

    public WanderingPets(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC);

        NeoForge.EVENT_BUS.addListener(this::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(this::onLevelLoad);
    }

    private void regenerateModdedEntitiesList(ServerLevel level) {
        Set<String> detectedTamable = new TreeSet<>();
        List<String> vanillaTamable = List.of(
                "entity.minecraft.cat",
                "entity.minecraft.wolf",
                "entity.minecraft.parrot"
        );

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (vanillaTamable.contains(type.getDescriptionId())) continue;
            try {
                Mob entity = (Mob) type.create(level);
                if (entity == null) continue;

                boolean hasFollowGoal = entity.goalSelector.getAvailableGoals().stream()
                        .anyMatch(g -> g.getGoal() instanceof FollowOwnerGoal);
                boolean hasStrollGoal = entity.goalSelector.getAvailableGoals().stream()
                        .anyMatch(g -> g.getGoal() instanceof RandomStrollGoal);

                if (hasFollowGoal && hasStrollGoal) {
                    Config.log("Found possibly compatible entity: {}", type);
                    ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                    String idStr = id.toString();

                    detectedTamable.add(idStr);
                }
            } catch (Exception ignored) {
            }
        }

        if (!detectedTamable.isEmpty()) {
            List<?> currentList = Config.GENERAL.moddedEntities.get();

            if (currentList.isEmpty()) {
                Config.GENERAL.moddedEntities.set(List.copyOf(detectedTamable));
                Config.log("Detected modded possibly tamable mobs: {}", detectedTamable);
                Config.CONFIG_SPEC.save();
            }
        }
    }

    public void onLevelLoad(LevelEvent.Load event) {
        if (Config.GENERAL.moddedEntities.get().isEmpty() && event.getLevel() instanceof ServerLevel level) {
            regenerateModdedEntitiesList(level);
        }
        Config.rebuildEnabledEntityTypes();
    }

    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getSide() == LogicalSide.SERVER &&
                event.getTarget() instanceof TamableAnimal mob &&
                Config.isWanderBehaviorEnabled(mob) &&
                event.getEntity() instanceof Player player &&
                player.isShiftKeyDown() &&
                player.getUUID().equals(mob.getOwnerUUID())) {

            IFollowsAccessor followsAccessor = (IFollowsAccessor) mob;
            boolean shouldFollow = !followsAccessor.isAllowedToFollow();
            followsAccessor.setAllowedToFollow(shouldFollow);

            player.sendSystemMessage(Component.translatable(
                    shouldFollow ? "wanderingpets.follow" : "wanderingpets.unfollow", mob.getName()
            ));

            event.setCanceled(true);
        }
    }
}
