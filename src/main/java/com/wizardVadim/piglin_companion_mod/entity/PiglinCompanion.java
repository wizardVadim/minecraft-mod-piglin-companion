package com.wizardVadim.piglin_companion_mod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;

public class PiglinCompanion extends TamableAnimal implements RangedAttackMob {

    private static final EntityDataAccessor<Integer> DATA_TEXTURE_VARIANT =
            SynchedEntityData.defineId(PiglinCompanion.class, EntityDataSerializers.INT);

    public PiglinCompanion(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.setTame(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TEXTURE_VARIANT, 0);
    }

    public int getTextureVariant() {
        return this.entityData.get(DATA_TEXTURE_VARIANT);
    }

    public void setTextureVariant(int variant) {
        this.entityData.set(DATA_TEXTURE_VARIANT, variant);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new CustomAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0D, 5.0F, 2.0F, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetNoFriendliesGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        this.targetSelector.addGoal(5, new HurtByTargetNoFriendliesGoal(this).setAlertOthers(PiglinCompanion.class));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ItemStack bow = this.getMainHandItem();
        if (!(bow.getItem() instanceof BowItem)) return;

        AbstractArrow arrow = new Arrow(this.level(), this);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333D) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double distance = Mth.sqrt((float)(dx * dx + dz * dz));

        arrow.shoot(dx, dy + distance * 0.2F, dz, 1.6F, 14 - this.level().getDifficulty().getId() * 4);
        this.level().addFreshEntity(arrow);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem item) {
        return item instanceof BowItem;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();

        if (this.level().isClientSide) {
            boolean interacting = this.isOwnedBy(player) || this.isTame() || this.isFood(stack);
            return interacting ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        if (this.isTame()) {
            if (this.isOwnedBy(player)) {
                if (player.isShiftKeyDown()) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    return InteractionResult.SUCCESS;
                }

                if (item instanceof ArmorItem armor) {
                    EquipmentSlot slot = armor.getEquipmentSlot();
                    ItemStack old = this.getItemBySlot(slot);
                    if (!old.isEmpty()) {
                        this.spawnAtLocation(old);
                    }
                    this.setItemSlot(slot, stack.copy());
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }

                if (item instanceof SwordItem || item == Items.SHIELD || item instanceof BowItem) {
                    EquipmentSlot slot = item == Items.SHIELD ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                    ItemStack old = this.getItemBySlot(slot);
                    if (!old.isEmpty()) {
                        this.spawnAtLocation(old);
                    }
                    this.setItemSlot(slot, stack.copy());
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        } else if (this.isFood(stack)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (this.random.nextInt(3) == 0) {
                this.tame(player);
                this.setOrderedToSit(false);
                this.level().broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.getItem() == Items.GOLD_INGOT;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TextureVariant", this.getTextureVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setTextureVariant(tag.getInt("TextureVariant"));
    }

    @Nullable
    @Override
    public TamableAnimal getBreedOffspring(ServerLevel serverLevel, AgeableMob partner) {
        return null;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public Component getName() {
        return Component.translatable("entity.piglin_companion_mod.piglin_companion");
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    public static class CustomAttackGoal extends Goal {
        private final PiglinCompanion mob;
        private final double speed;
        private final boolean longMemory;
        private Goal currentGoal;

        public CustomAttackGoal(PiglinCompanion mob, double speed, boolean longMemory) {
            this.mob = mob;
            this.speed = speed;
            this.longMemory = longMemory;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void start() {
            this.selectGoal();
            if (currentGoal != null) currentGoal.start();
        }

        @Override
        public void stop() {
            if (currentGoal != null) currentGoal.stop();
        }

        @Override
        public void tick() {
            this.selectGoal();
            if (currentGoal != null) currentGoal.tick();
        }

        private void selectGoal() {
            ItemStack itemStack = mob.getMainHandItem();
            if (itemStack.getItem() instanceof BowItem) {
                if (!(currentGoal instanceof RangedBowAttackGoal)) {
                    currentGoal = new RangedBowAttackGoal<>(mob, speed, 20, 15.0F);
                }
            } else {
                if (!(currentGoal instanceof MeleeAttackGoal)) {
                    currentGoal = new MeleeAttackGoal(mob, speed, longMemory);
                }
            }
        }
    }

    public class HurtByTargetNoFriendliesGoal extends HurtByTargetGoal {
        private final PiglinCompanion piglin;

        public HurtByTargetNoFriendliesGoal(PiglinCompanion mob) {
            super(mob);
            this.piglin = mob;
        }

        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            }

            // Проверка: напал ли дружественный пиглин
            LivingEntity attacker = piglin.getLastHurtByMob();
            if (attacker instanceof PiglinCompanion otherPiglin) {
                if (otherPiglin.isTame() && piglin.isTame()) {
                    // Оба приручены — не атакуем
                    return false;
                }
            }

            return true;
        }
    }
}