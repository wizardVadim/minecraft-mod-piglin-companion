package com.wizardVadim.piglin_companion_mod.goals;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.phys.Vec3;

public class CustomRangedBowAttackGoal<T extends Mob & RangedAttackMob> extends RangedBowAttackGoal<T> {
    private final float minAttackDistance;
    private final T mob;
    private final double speedModifier;

    public CustomRangedBowAttackGoal(T mob, double speedModifier, int attackInterval, float maxAttackDistance, float minAttackDistance) {
        super(mob, speedModifier, attackInterval, maxAttackDistance);
        this.mob = mob;
        this.minAttackDistance = minAttackDistance;
        this.speedModifier = speedModifier;
    }

    @Override
    public boolean canUse() {
        if (mob.getTarget() == null) return false;
        double dist = mob.distanceToSqr(mob.getTarget());
        return super.canUse() && dist >= minAttackDistance * minAttackDistance;
    }

    @Override
    public boolean canContinueToUse() {
        if (mob.getTarget() == null) return false;
        double dist = mob.distanceToSqr(mob.getTarget());
        return super.canContinueToUse() && dist >= minAttackDistance * minAttackDistance;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.mob.getTarget() != null) {
            double distanceSq = this.mob.distanceToSqr(this.mob.getTarget());
            if (distanceSq < minAttackDistance * minAttackDistance) {
                Vec3 awayFromTarget = this.mob.position().subtract(this.mob.getTarget().position()).normalize().scale(0.15);
                this.mob.getMoveControl().setWantedPosition(
                        this.mob.getX() + awayFromTarget.x,
                        this.mob.getY(),
                        this.mob.getZ() + awayFromTarget.z,
                        this.speedModifier
                );

                this.mob.getLookControl().setLookAt(
                        this.mob.getTarget(),
                        30.0F,
                        30.0F
                );
            }


        }
    }
}