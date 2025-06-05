package com.wizardVadim.piglin_companion_mod.goals;

import com.wizardVadim.piglin_companion_mod.entity.PiglinCompanion;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.InteractionHand;

public class CustomMeleeAttackGoal extends MeleeAttackGoal {

    public CustomMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followTargetEvenIfNotSeen) {
        super(mob, speedModifier, followTargetEvenIfNotSeen);
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
        if (distToEnemySqr <= this.getAttackReachSqr(enemy) && ((PiglinCompanion) this.mob).getAttackAnim(0.0F) == 0.0F) {
            this.mob.swing(InteractionHand.MAIN_HAND);
            ((PiglinCompanion) this.mob).startAttackAnimation();
            this.mob.doHurtTarget(enemy);
        }
    }


}
