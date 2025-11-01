package com.outurnate.wanderingpets;

import com.outurnate.wanderingpets.interfaces.IFollowsAccessor;
import com.outurnate.wanderingpets.interfaces.IMobAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class WanderingPets implements ModInitializer {

    public static final String MODID = "wanderingpets";
    public static final Logger LOGGER = LoggerFactory.getLogger("WanderingPets");
    private long lastUse  = 0;

    @Override
    public void onInitialize() {
        Config.loadConfig();

        ServerWorldEvents.LOAD.register((server, level) -> {
            this.onLevelLoad(level);
        });

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level.isClientSide() || hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown() || !(entity instanceof Mob) || player.isSpectator() || hitResult == null) {
                return InteractionResult.PASS;
            }
            long currentTime = level.getGameTime();

            if (currentTime - lastUse < 20) {
                return InteractionResult.PASS;
            }

            InteractionResult result = this.onEntityInteract(player, (Mob) entity);

            if (result.consumesAction()) {
                lastUse = currentTime;
            }
            return result;
        });
    }

    private void onLevelLoad(ServerLevel level) {
        if (Config.getModdedEntities().isEmpty()) {
            regenerateModdedEntitiesList(level);
        }
        Config.rebuildEnabledEntityTypes();
        for (EntityType<?> type : Config.ENABLED_ENTITY_TYPES) {
            try {
                Mob entity = (Mob) type.create(level, EntitySpawnReason.COMMAND);
                if (entity == null || !hasWeakCompatibility(entity)) throw new Exception();
            } catch (Exception e) {
                LOGGER.warn("Attempted to load entity type {}, but it doesn't seem to be compatible. Ignoring it...", type);
            }
        }
    }

    public InteractionResult onEntityInteract(Player player, Mob mob) {
        if (Config.isWanderBehaviorEnabled(mob)) {
            Entity owner = null;
            if (mob instanceof TamableAnimal tamable) {
                owner = tamable.getOwner();
            } else {
                Optional<WrappedGoal> followLikeGoal = ((IMobAccessor) mob).getGoalSelector().getAvailableGoals().stream()
                        .filter(g -> isFollowOwnerLikeGoal(g.getGoal()))
                        .findFirst();

                if (followLikeGoal.isEmpty()) {
                    return InteractionResult.PASS;
                }
                try {
                    Goal goal = followLikeGoal.get().getGoal();
                    Class<?> clazz = goal.getClass();
                    for (var field : clazz.getDeclaredFields()) {
                        if (field.getName().equalsIgnoreCase("owner")) {
                            field.setAccessible(true);
                            Object value = field.get(goal);
                            if (value instanceof Entity) {
                                owner = (Entity) value;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to patch {} behavior via reflection.", mob.getName());
                    return InteractionResult.PASS;
                }
            }

            if (owner == null || !owner.getUUID().equals(player.getUUID())) {
                return InteractionResult.PASS;
            }

            IFollowsAccessor followsAccessor = (IFollowsAccessor) mob;
            boolean shouldFollow = !followsAccessor.isAllowedToFollow();
            followsAccessor.setAllowedToFollow(shouldFollow);

            player.displayClientMessage(Component.translatable(
                    shouldFollow ? "wanderingpets.follow" : "wanderingpets.unfollow", mob.getName()
            ), false);

            if (mob instanceof TamableAnimal tamable) {
                tamable.setInSittingPose(false);
            }

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
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

                boolean hasFollowGoal = ((IMobAccessor) entity).getGoalSelector().getAvailableGoals().stream()
                        .anyMatch(g -> g.getGoal() instanceof FollowOwnerGoal);
                boolean hasStrollGoal = ((IMobAccessor) entity).getGoalSelector().getAvailableGoals().stream()
                        .anyMatch(g -> g.getGoal() instanceof RandomStrollGoal);

                if (hasFollowGoal && hasStrollGoal) {
                    LOGGER.info("Found possibly compatible entity: {}", type);
                    ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                    detectedTamable.add(id.toString());
                }
            } catch (Exception ignored) {
            }
        }

        if (!detectedTamable.isEmpty()) {
            List<String> currentList = Config.getModdedEntities();
            if (currentList.isEmpty()) {
                Config.setModdedEntities(List.copyOf(detectedTamable));
                LOGGER.info("Detected modded possibly compatible mobs: {}", detectedTamable);
                Config.saveConfig();
            }
        }
    }

    private boolean hasWeakCompatibility(Mob entity) {
        return ((IMobAccessor) entity).getGoalSelector().getAvailableGoals().stream()
                .anyMatch(g -> isFollowOwnerLikeGoal(g.getGoal())) &&
                ((IMobAccessor) entity).getGoalSelector().getAvailableGoals().stream()
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

                if (field.getName().equalsIgnoreCase("owner")) {
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
}
