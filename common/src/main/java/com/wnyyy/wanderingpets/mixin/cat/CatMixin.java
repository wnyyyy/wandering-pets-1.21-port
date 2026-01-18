package com.wnyyy.wanderingpets.mixin.cat;

import com.wnyyy.wanderingpets.config.ModConfig;
import com.wnyyy.wanderingpets.duck.ICatWanderAccessor;
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

import java.util.Random;
import java.util.Set;

import static com.wnyyy.wanderingpets.Constants.LOG;

@Mixin(Cat.class)
public abstract class CatMixin extends TamableAnimal implements ICatWanderAccessor {

    @Unique
    private final Random wpets$rand = new Random();
    @Unique
    private int wpets$notSittedTicks = wpets$rand.nextInt(ModConfig.CatsRelaxingProfile.sitCd());
    @Unique
    private int wpets$notSleptTicks = wpets$rand.nextInt(ModConfig.CatsRelaxingProfile.sleepCd());

    protected CatMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo ci) {
        if (ModConfig.isBetterCatBehaviorEnabled() && this.isTame() && this.getOwner() != null) {
            wpets$notSittedTicks++;
            wpets$notSleptTicks++;
            if (ModConfig.isDebugMode() && this.tickCount % 50 == 0) {
                Set<WrappedGoal> allGoals = this.goalSelector.getAvailableGoals();
                allGoals.stream().filter(goal -> goal.getGoal() instanceof CatLieOnBedGoal).findFirst().ifPresent(lieOnBed ->
                        LOG.info("LieOnBed | Running: {} | notSlept: {}", lieOnBed.isRunning(), wpets$notSleptTicks));
                allGoals.stream().filter(goal -> goal.getGoal() instanceof CatSitOnBlockGoal).findFirst().ifPresent(sitOnBlock ->
                        LOG.info("SitOnBlock | Running: {} | notSitted: {}", sitOnBlock.isRunning(), wpets$notSittedTicks));
                allGoals.stream().filter(WrappedGoal::isRunning).findFirst().ifPresent(runningGoal ->
                        LOG.info("catTick# {} | RunningGoal: {}", this.tickCount, runningGoal.getGoal().getClass().getSimpleName()));
            }
        }
    }

    @Override
    public int wanderingpets$getNotSittedTicks() {
        return wpets$notSittedTicks;
    }

    @Override
    public void wanderingpets$setNotSittedTicks(int notSittedTicks) {
        this.wpets$notSittedTicks = notSittedTicks;
    }

    @Override
    public int wanderingpets$getNotSleptTicks() {
        return wpets$notSleptTicks;
    }

    @Override
    public void wanderingpets$setNotSleptTicks(int notSleptTicks) {
        this.wpets$notSleptTicks = notSleptTicks;
    }
}