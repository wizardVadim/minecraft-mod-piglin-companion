package com.wizardVadim.piglin_companion_mod.goals;

import com.wizardVadim.piglin_companion_mod.entity.PiglinCompanion;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

public class CustomAttackGoal extends Goal {
    private final PiglinCompanion mob;

    private final Goal rangedGoal;
    private final Goal meleeGoal;

    private Goal currentGoal = null;

    public CustomAttackGoal(PiglinCompanion mob, double speed, boolean longMemory) {
        this.mob = mob;
        // 1 - частота стрельбы 20 = 1 секунда, 2 - дистанция
        this.rangedGoal = new CustomRangedBowAttackGoal<>(mob, speed, 20, 20.0F, 7.0F);
        this.meleeGoal = new CustomMeleeAttackGoal(mob, speed, longMemory);
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        updateGoal();
        if (currentGoal != null) {
            currentGoal.start();
        }
    }

    @Override
    public void stop() {
        if (currentGoal != null) {
            currentGoal.stop();
            currentGoal = null;
        }
    }

    @Override
    public void tick() {
        if (shouldSwitchGoal()) {
            if (currentGoal != null) {
                currentGoal.stop();
            }
            updateGoal();
            if (currentGoal != null) {
                currentGoal.start();
            }
        }

        if (currentGoal != null) {
            currentGoal.tick();
        }
    }

    private void updateGoal() {
        ItemStack mainHand = mob.getMainHandItem();
        boolean hasBow = mainHand.getItem() instanceof BowItem;

        if (hasBow) {
            currentGoal = rangedGoal;
        } else {
            currentGoal = meleeGoal;
        }
    }

    private boolean shouldSwitchGoal() {
        ItemStack mainHand = mob.getMainHandItem();
        boolean hasBow = mainHand.getItem() instanceof BowItem;

        Goal desiredGoal = hasBow ? rangedGoal : meleeGoal;

        return desiredGoal != currentGoal;
    }
}