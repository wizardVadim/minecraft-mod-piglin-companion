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

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return; // работаем только на сервере

        Entity target = event.getTarget();
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        ItemStack heldItem = event.getItemStack();

        if (target instanceof Piglin piglin) {
            if (heldItem.getItem() == Items.GOLD_BLOCK) {
                // Пробуем приручить с шансом 1/3
                if (player.getRandom().nextInt(3) == 0) {
                    Level level = player.level();

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.HEART,
                                target.getX(), target.getY() + 1, target.getZ(),
                                10,
                                0.2, 0.5, 0.2,
                                0.01
                        );
                    }

                    level.playSound(null, target.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0f, 1.0f);

                    // Создаём нового прирученного пиглина (PiglinCompanion)
                    if (ModEntities.PIGLIN_COMPANION.isPresent()) {
                        PiglinCompanion companion = ModEntities.PIGLIN_COMPANION.get().create(level);
                        if (companion != null) {
                            int randomVariant = player.getRandom().nextInt(3); // 0, 1 или 2
                            companion.setTextureVariant(randomVariant);

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
                                net.minecraft.core.particles.ParticleTypes.SMOKE,
                                target.getX(), target.getY() + 1, target.getZ(),
                                10,
                                0.2, 0.5, 0.2,
                                0.01
                        );
                    }

                    level.playSound(null, target.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0f, 1.0f);

                    // Можно добавить визуальную или звуковую обратную связь, что приручение не прошло
                    player.displayClientMessage(Component.literal("Пиглин отверг ваше золото"), true);
                    event.setCancellationResult(InteractionResult.FAIL);
                    event.setCanceled(true);
                }
            }
        }
    }
}