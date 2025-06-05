package com.wizardVadim.piglin_companion_mod.client.models;

import com.wizardVadim.piglin_companion_mod.entity.PiglinCompanion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

public class PiglinCompanionModel extends PiglinModel<PiglinCompanion> {
    private static final float PI = (float) Math.PI;
    private static final float ATTACK_ARM_SWING_MULTIPLIER = 2.0F;
    private static final float HEAD_SWING_MULTIPLIER = 0.75F;
    private static final float ARM_Z_ROT_MULTIPLIER = -0.4F;
    private static final float BODY_ROT_MULTIPLIER = 2.0F;
    private static final float HEAD_OFFSET = 0.7F;

    public PiglinCompanionModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(PiglinCompanion entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        this.rightArmPose = ArmPose.EMPTY;
        this.leftArmPose = ArmPose.EMPTY;

        if (entity.isUsingItem()) {
            ItemStack stack = entity.getUseItem();
            if (stack.getItem() instanceof BowItem) {
                this.rightArmPose = ArmPose.BOW_AND_ARROW;
            }
        }

        float attackTime = entity.getAttackAnim(Minecraft.getInstance().getFrameTime());
        if (attackTime > 0.0F) {
            float sin = Mth.sin(attackTime * PI);
            float swing = sin * ATTACK_ARM_SWING_MULTIPLIER;
            float headInfluence = sin * -(this.head.xRot - HEAD_OFFSET) * HEAD_SWING_MULTIPLIER;

            this.rightArm.xRot = -swing + headInfluence;
            this.rightArm.yRot = this.body.yRot * BODY_ROT_MULTIPLIER;
            this.rightArm.zRot = sin * ARM_Z_ROT_MULTIPLIER;
        }
    }
}