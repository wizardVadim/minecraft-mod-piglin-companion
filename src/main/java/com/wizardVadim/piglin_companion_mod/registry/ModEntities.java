package com.wizardVadim.piglin_companion_mod.registry;

import com.wizardVadim.piglin_companion_mod.entity.PiglinCompanion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.wizardVadim.piglin_companion_mod.PiglinCompanionMod.MODID;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<PiglinCompanion>> PIGLIN_COMPANION =
            ENTITY_TYPES.register("piglin_companion",
                    () -> EntityType.Builder.of(PiglinCompanion::new, MobCategory.CREATURE)  // Категорию можно изменить
                            .sized(0.6F, 1.95F)
                            .build(new ResourceLocation(MODID, "piglin_companion").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}