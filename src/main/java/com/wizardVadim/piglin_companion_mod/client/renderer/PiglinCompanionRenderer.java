package com.wizardVadim.piglin_companion_mod.client.renderer;

import com.wizardVadim.piglin_companion_mod.entity.PiglinCompanion;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.HumanoidModel;

public class PiglinCompanionRenderer extends MobRenderer<PiglinCompanion, HumanoidModel<PiglinCompanion>> {

    public PiglinCompanionRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PIGLIN)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(PiglinCompanion entity) {
        int variant = entity.getTextureVariant();
        return new ResourceLocation("piglin_companion_mod", "textures/entity/piglin_companion_" + variant + ".png");
    }
}
