package com.wizardVadim.piglin_companion_mod.client.models;

import com.wizardVadim.piglin_companion_mod.entity.PiglinCompanion;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

public class PiglinCompanionModel extends PiglinModel<PiglinCompanion> {
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
            // Можно добавить поддержку арбалета и т.д.
        }

        float attackTime = entity.getAttackAnim(0.0F);
        if (attackTime > 0.0F) {
            float f = Mth.sin(Mth.sqrt(attackTime) * (float)Math.PI);
            this.rightArm.xRot -= f * 1.2F;
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
        }
    }
}
