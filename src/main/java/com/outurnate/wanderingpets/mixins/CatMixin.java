package com.outurnate.wanderingpets.mixins;

import com.outurnate.wanderingpets.Config;
import com.outurnate.wanderingpets.WanderingPets;
import com.outurnate.wanderingpets.interfaces.ICatWanderBehaviorAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.CatLieOnBedGoal;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.feline.Cat;
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
        if (Config.GENERAL.betterCatBehavior.get() && this.isTame() && this.getOwner() != null) {
            wpets$notSittedTicks++;
            wpets$notSleptTicks++;
            if (Config.GENERAL.debugMode.get() && this.tickCount % 50 == 0) {
                Set<WrappedGoal> allGoals = this.goalSelector.getAvailableGoals();
                allGoals.stream().filter(goal -> goal.getGoal() instanceof CatLieOnBedGoal).findFirst().ifPresent(lieOnBed ->
                        Config.log("LieOnBed | Running: {} | notSlept: {}", lieOnBed.isRunning(), wpets$notSleptTicks));
                allGoals.stream().filter(goal -> goal.getGoal() instanceof CatSitOnBlockGoal).findFirst().ifPresent(sitOnBlock ->
                        Config.log("SitOnBlock | Running: {} | notSitted: {}", sitOnBlock.isRunning(), wpets$notSittedTicks));
                allGoals.stream().filter(WrappedGoal::isRunning).findFirst().ifPresent(runningGoal ->
                        Config.log("catTick#%s | RunningGoal: %s", this.tickCount, runningGoal.getGoal().getClass().getSimpleName()));
            }
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
