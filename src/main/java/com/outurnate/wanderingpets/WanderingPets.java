package com.outurnate.wanderingpets;

import com.outurnate.wanderingpets.data.ModAttachments;
import com.outurnate.wanderingpets.interfaces.IFollowsAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
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
        ModAttachments.register(modContainer.getEventBus());
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
                Mob entity = (Mob) type.create(level, EntitySpawnReason.COMMAND);
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
                Config.log("Detected modded possibly compatible mobs: {}", detectedTamable);
                Config.CONFIG_SPEC.save();
            }
        }
    }

    private boolean hasWeakCompatibility(Mob entity) {
        return entity.goalSelector.getAvailableGoals().stream()
                .anyMatch(g -> isFollowOwnerLikeGoal(g.getGoal())) &&
                entity.goalSelector.getAvailableGoals().stream()
                        .anyMatch(g -> g.getGoal() instanceof RandomStrollGoal);
    }

    private boolean isFollowOwnerLikeGoal(Object goal) {
        try {
            Class<?> clazz = goal.getClass();

            boolean hasOwnerField = false;
            boolean hasNavigatorField = false;

            for (var field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(goal);

                if (field.getName().toLowerCase().contains("owner")) {
                    hasOwnerField = true;
                }

                if (value instanceof GroundPathNavigation ||
                        value instanceof FlyingPathNavigation) {
                    hasNavigatorField = true;
                }

                if (hasOwnerField && hasNavigatorField) break;
            }

            if (!hasOwnerField || !hasNavigatorField) return false;

            if (goal instanceof Goal g) {
                var flags = g.getFlags();
                return flags.contains(Goal.Flag.MOVE)
                        && flags.contains(Goal.Flag.LOOK);
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    public void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (Config.GENERAL.moddedEntities.get().isEmpty()) {
                regenerateModdedEntitiesList(level);
            }
            Config.rebuildEnabledEntityTypes();
            for (EntityType<?> type : Config.ENABLED_ENTITY_TYPES) {
                try {
                    Mob entity = (Mob) type.create(level, EntitySpawnReason.COMMAND);
                    if (entity == null || !hasWeakCompatibility(entity)) throw new Exception();
                } catch (Exception e) {
                    Config.log("Attempted to load entity type {}, but it doesn't seems to be compatible. Ignoring it...", type);
                }
            }
        }
    }

    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        if (event.getTarget() instanceof Mob mob &&
                Config.isWanderBehaviorEnabled(mob) &&
                event.getEntity() instanceof Player player &&
                player.isShiftKeyDown()) {

            Entity owner = null;
            if (mob instanceof TamableAnimal tamable) {
                owner = tamable.getOwner();
            } else {
                Optional<WrappedGoal> followLikeGoal = mob.goalSelector.getAvailableGoals().stream().filter(g -> isFollowOwnerLikeGoal(g.getGoal())).findFirst();
                if (followLikeGoal.isEmpty()) {
                    return;
                }
                try {
                    Goal goal = followLikeGoal.get().getGoal();
                    Class<?> clazz = goal.getClass();
                    for (var field : clazz.getDeclaredFields()) {
                        if (field.getName().toLowerCase().contains("owner")) {
                            field.setAccessible(true);
                            Object value = field.get(goal);
                            owner = (Entity) value;
                        }
                    }
                } catch (Exception e) {
                    Config.log("Failed to patch {} behavior.", mob.getName());
                    return;
                }
            }
            if (owner == null || !owner.getUUID().equals(player.getUUID())) {
                return;
            }
            IFollowsAccessor followsAccessor = (IFollowsAccessor) mob;
            boolean shouldFollow = !followsAccessor.isAllowedToFollow();
            followsAccessor.setAllowedToFollow(shouldFollow);

            player.displayClientMessage(Component.translatable(
                    shouldFollow ? "wanderingpets.follow" : "wanderingpets.unfollow", mob.getName()
            ), false);

            event.setCanceled(true);
        }
    }
}
