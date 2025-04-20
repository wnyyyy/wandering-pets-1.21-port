package com.outurnate.wanderingpets.mixins;

import com.outurnate.wanderingpets.interfaces.ICatWanderBehaviorAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.CatLieOnBedGoal;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(Cat.class)
public abstract class CatMixin extends TamableAnimal implements ICatWanderBehaviorAccessor {

    @Unique
    private int wpets$notSittedTicks = 0;
    @Unique
    private int wpets$notSleptTicks = 0;

    protected CatMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo ci) {
        if (this.isTame() && this.getOwner() != null) {
            wpets$notSittedTicks++;
            wpets$notSleptTicks++;
        }
        if (this.isTame() && this.getOwner() != null && this.tickCount % 50 == 0) {
            Set<WrappedGoal> allGoals = this.goalSelector.getAvailableGoals();
            WrappedGoal lieOnBed = allGoals.stream().filter(goal -> goal.getGoal() instanceof CatLieOnBedGoal).findFirst().orElse(null);
            if (lieOnBed != null) {
                MoveToBlockGoalAccessor moveToBlockGoalAccessor = (MoveToBlockGoalAccessor) lieOnBed.getGoal();
                this.getOwner().sendSystemMessage(Component.translatable(String.format("LieOnBed | Running: %s | notSlept: %s", lieOnBed.isRunning(), wpets$notSleptTicks)));
            }
            WrappedGoal sitOnBlock = allGoals.stream().filter(goal -> goal.getGoal() instanceof CatSitOnBlockGoal).findFirst().orElse(null);
            if (sitOnBlock != null) {
                MoveToBlockGoalAccessor moveToBlockGoalAccessor = (MoveToBlockGoalAccessor) sitOnBlock.getGoal();
                this.getOwner().sendSystemMessage(Component.translatable(String.format("SitOnBlock | Running: %s | notSitted: %s", sitOnBlock.isRunning(), wpets$notSittedTicks)));
            }
            allGoals.stream().filter(WrappedGoal::isRunning).findFirst().ifPresent(runningGoal ->
                    this.getOwner().sendSystemMessage(Component.translatable(String.format("catTick#%s | RunningGoal: %s", this.tickCount, runningGoal.getGoal().getClass().getSimpleName()))));
        }
    }

    @Override
    public int getNotSittedTicks() {
        return wpets$notSittedTicks;
    }

    @Override
    public void setNotSittedTicks(int notSittedTicks) {
        this.wpets$notSittedTicks = notSittedTicks;
    }

    @Override
    public int getNotSleptTicks() {
        return wpets$notSleptTicks;
    }

    @Override
    public void setNotSleptTicks(int notSleptTicks) {
        this.wpets$notSleptTicks = notSleptTicks;
    }
}
