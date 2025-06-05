package com.wizardVadim.piglin_companion_mod.event;

import com.wizardVadim.piglin_companion_mod.PiglinCompanionMod;
import com.wizardVadim.piglin_companion_mod.entity.PiglinCompanion;
import com.wizardVadim.piglin_companion_mod.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

@Mod.EventBusSubscriber(modid = PiglinCompanionMod.MODID)
public class PiglinTamingHandler {

    private static final int TAMING_SUCCESS_CHANCE = 3; // 1 из 3
    private static final int PARTICLE_COUNT = 10;
    private static final double PARTICLE_OFFSET_X = 0.2;
    private static final double PARTICLE_OFFSET_Y = 0.5;
    private static final double PARTICLE_OFFSET_Z = 0.2;
    private static final double PARTICLE_SPEED = 0.01;

    private static final float SOUND_VOLUME = 1.0f;
    private static final float SOUND_PITCH = 1.0f;

    private static final int TEXTURE_VARIANT_COUNT = 3;
    private static final int TEXTURE_LEVEL_DEFAULT = 1;

    private static final Item TAMING_ITEM = Items.GOLD_BLOCK;
    private static final String REJECTION_MESSAGE = "Пиглин отверг ваше золото";

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return; // работаем только на сервере

        Entity target = event.getTarget();
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        ItemStack heldItem = event.getItemStack();

        if (target instanceof Piglin piglin) {
            if (heldItem.getItem() == TAMING_ITEM) {
                if (player.getRandom().nextInt(TAMING_SUCCESS_CHANCE) == 0) {
                    Level level = player.level();

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.HEART,
                                target.getX(), target.getY() + 1, target.getZ(),
                                PARTICLE_COUNT,
                                PARTICLE_OFFSET_X, PARTICLE_OFFSET_Y, PARTICLE_OFFSET_Z,
                                PARTICLE_SPEED
                        );
                    }

                    level.playSound(null, target.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);

                    if (ModEntities.PIGLIN_COMPANION.isPresent()) {
                        PiglinCompanion companion = ModEntities.PIGLIN_COMPANION.get().create(level);
                        if (companion != null) {
                            int randomVariant = player.getRandom().nextInt(TEXTURE_VARIANT_COUNT);
                            companion.setTextureVariant(randomVariant);
                            companion.setTextureLevel(TEXTURE_LEVEL_DEFAULT);

                            System.out.println("Create new piglin " + companion.getTextureVariant());
                            companion.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
                            companion.tame(player);
                            companion.setOwnerUUID(player.getUUID());
                            companion.setOrderedToSit(false);

                            for (var slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                                companion.setItemSlot(slot, piglin.getItemBySlot(slot).copy());
                            }

                            level.addFreshEntity(companion);
                            target.remove(Entity.RemovalReason.DISCARDED);

                            if (!player.getAbilities().instabuild) {
                                heldItem.shrink(1);
                            }

                            event.setCancellationResult(InteractionResult.SUCCESS);
                            event.setCanceled(true);
                        }
                    } else {
                        System.err.println("PiglinCompanion EntityType не зарегистрирован!");
                    }
                } else {
                    Level level = player.level();
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.SMOKE,
                                target.getX(), target.getY() + 1, target.getZ(),
                                PARTICLE_COUNT,
                                PARTICLE_OFFSET_X, PARTICLE_OFFSET_Y, PARTICLE_OFFSET_Z,
                                PARTICLE_SPEED
                        );
                    }

                    level.playSound(null, target.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);

                    player.displayClientMessage(Component.literal(REJECTION_MESSAGE), true);
                    event.setCancellationResult(InteractionResult.FAIL);
                    event.setCanceled(true);
                }
            }
        }
    }
}