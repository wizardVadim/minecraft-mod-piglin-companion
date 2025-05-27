package com.wizardVadim.piglin_companion_mod.registry;

import com.wizardVadim.piglin_companion_mod.PiglinCompanionMod;
import com.wizardVadim.piglin_companion_mod.entity.PiglinCompanion;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PiglinCompanionMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.PIGLIN_COMPANION.get(), PiglinCompanion.createAttributes().build());
    }
}