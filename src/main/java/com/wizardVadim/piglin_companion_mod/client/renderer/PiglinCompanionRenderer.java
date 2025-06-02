package com.wizardVadim.piglin_companion_mod.client.renderer;

import com.wizardVadim.piglin_companion_mod.entity.PiglinCompanion;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class PiglinCompanionRenderer extends MobRenderer<PiglinCompanion, HumanoidModel<PiglinCompanion>> {

    public PiglinCompanionRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PIGLIN)), 0.5f);

        // броня
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
        ));

        // ОРУЖИЕ/ЩИТ в руках
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(PiglinCompanion entity) {
        int variant = entity.getTextureVariant();
        return new ResourceLocation("piglin_companion_mod", "textures/entity/piglin_companion_" + variant + ".png");
    }
}