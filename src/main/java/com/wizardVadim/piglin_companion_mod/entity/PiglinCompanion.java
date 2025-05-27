package com.wizardVadim.piglin_companion_mod.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PiglinCompanion extends TamableAnimal {

    private static final EntityDataAccessor<Integer> DATA_TEXTURE_VARIANT =
            SynchedEntityData.defineId(PiglinCompanion.class, net.minecraft.network.syncher.EntityDataSerializers.INT);

    private int textureVariant;

    public PiglinCompanion(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        if (!level.isClientSide) {
            this.textureVariant = this.getRandom().nextInt(3); // 0..2
        }
    }

    public int getTextureVariant() {
        return this.entityData.get(DATA_TEXTURE_VARIANT);
    }

    public void setTextureVariant(int variant) {
        this.entityData.set(DATA_TEXTURE_VARIANT, variant);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.0D, 5.0F, 2.0F, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    // Обработка взаимодействия игрока с питомцем (приручение, кормление и т.д.)
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        Item item = itemstack.getItem();

        if (this.level().isClientSide) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || this.isFood(itemstack);
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            if (this.isTame()) {
                if (this.isOwnedBy(player)) {
                    // Можно добавить логику взаимодействия с приручённым питомцем
                    return InteractionResult.SUCCESS;
                }
            } else if (this.isFood(itemstack)) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.level().gameEvent(GameEvent.ENTITY_INTERACT, this.position(), GameEvent.Context.of(player));
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, (byte)7); // анимация приручения
                } else {
                    this.level().broadcastEntityEvent(this, (byte)6); // анимация отказа
                }
                return InteractionResult.SUCCESS;
            }
            return super.mobInteract(player, hand);
        }
    }

    // Метод создания потомства — можно вернуть null, если питомец не размножается
    @Nullable
    @Override
    public TamableAnimal getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null; // Если питомец не размножается
    }

    // Проверка, является ли предмет "едой" для питомца (используется для приручения и кормления)
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.getItem() == net.minecraft.world.item.Items.GOLD_INGOT;
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TextureVariant", this.getTextureVariant());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setTextureVariant(tag.getInt("TextureVariant"));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TEXTURE_VARIANT, 0);
    }
}