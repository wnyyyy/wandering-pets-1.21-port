package com.outurnate.wanderingpets.mixins;

import com.outurnate.wanderingpets.interfaces.MoveToBlockGoalAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.CatLieOnBedGoal;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(Cat.class)
public abstract class CatMixin extends TamableAnimal {

    protected CatMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo ci) {
        if (this.isTame() && this.getOwner() != null && this.tickCount % 50 == 0) {
            Set<WrappedGoal> allGoals = this.goalSelector.getAvailableGoals();
            WrappedGoal lieOnBed = allGoals.stream().filter(goal -> goal.getGoal() instanceof CatLieOnBedGoal).findFirst().orElse(null);
            if (lieOnBed != null) {
                MoveToBlockGoalAccessor moveToBlockGoalAccessor = (MoveToBlockGoalAccessor) lieOnBed.getGoal();
                int nextStart = moveToBlockGoalAccessor.getNextStartTick();
                this.getOwner().sendSystemMessage(Component.translatable(String.format("LieOnBed | Running: %s | nextStartTick: %s", lieOnBed.isRunning(), nextStart)));
            }
            WrappedGoal sitOnBlock = allGoals.stream().filter(goal -> goal.getGoal() instanceof CatSitOnBlockGoal).findFirst().orElse(null);
            if (sitOnBlock != null) {
                MoveToBlockGoalAccessor moveToBlockGoalAccessor = (MoveToBlockGoalAccessor) sitOnBlock.getGoal();
                int nextStart = moveToBlockGoalAccessor.getNextStartTick();
                this.getOwner().sendSystemMessage(Component.translatable(String.format("SitOnBlock | Running: %s | nextStartTick: %s", sitOnBlock.isRunning(), nextStart)));
            }
            allGoals.stream().filter(WrappedGoal::isRunning).findFirst().ifPresent(runningGoal -> this.getOwner().sendSystemMessage(Component.translatable(String.format("catTick#%s | RunningGoal: %s", this.tickCount, runningGoal.getGoal().getClass().getSimpleName()))));
        }
    }
}
