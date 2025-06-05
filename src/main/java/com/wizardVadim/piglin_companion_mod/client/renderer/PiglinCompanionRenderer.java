package com.wizardVadim.piglin_companion_mod.client.renderer;

import com.wizardVadim.piglin_companion_mod.client.models.PiglinCompanionModel;
import com.wizardVadim.piglin_companion_mod.entity.PiglinCompanion;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class PiglinCompanionRenderer extends MobRenderer<PiglinCompanion, PiglinCompanionModel> {
    public PiglinCompanionRenderer(EntityRendererProvider.Context context) {
        super(context, new PiglinCompanionModel(context.bakeLayer(ModelLayers.PIGLIN)), 1.0f);

        // броня на HumanoidModel (как было)
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
        ));

        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(PiglinCompanion entity) {
        int variant = entity.getTextureVariant();
        int textureLevel = entity.getTextureLevel();
        return new ResourceLocation("piglin_companion_mod", "textures/entity/level_" + textureLevel + "/piglin_companion_" + variant + ".png");
    }
}