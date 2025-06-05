package com.wizardVadim.piglin_companion_mod.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;

public class SmartFollowOwnerGoal extends FollowOwnerGoal {
    private final TamableAnimal mob;

    public SmartFollowOwnerGoal(TamableAnimal mob, double speed, float minDist, float maxDist, boolean teleport) {
        super(mob, speed, minDist, maxDist, teleport);
        this.mob = mob;
    }

    private static final Integer ATTACK_DISTANCE = 15;

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();

        if (target != null && target.isAlive()) {
            // Если цель слишком далеко — игнорируем
            double distanceToTarget = mob.distanceToSqr(target);
            if (distanceToTarget < ATTACK_DISTANCE * ATTACK_DISTANCE) {
                return false;
            }
        }

        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();

        if (target != null && target.isAlive()) {
            double distanceToTarget = mob.distanceToSqr(target);
            if (distanceToTarget < ATTACK_DISTANCE * ATTACK_DISTANCE) {
                return false;
            }
        }

        return super.canContinueToUse();
    }
}