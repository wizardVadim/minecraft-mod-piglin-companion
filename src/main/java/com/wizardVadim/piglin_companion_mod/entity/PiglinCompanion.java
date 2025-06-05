package com.wizardVadim.piglin_companion_mod.entity;

import com.wizardVadim.piglin_companion_mod.goals.CustomAttackGoal;
import com.wizardVadim.piglin_companion_mod.goals.SmartFollowOwnerGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PiglinCompanion extends TamableAnimal implements RangedAttackMob {

    public PiglinCompanion(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.setTame(false);
        this.setRangedAttackDamage(2.0D);
    }

//    Piglin companion params
    private static final HashMap<Item, Float> FOOD_MAP = getFoodMap();
    private static final Integer MAX_LEVEL = 6;
    private static final EntityDataAccessor<Integer> DATA_TEXTURE_VARIANT =
            SynchedEntityData.defineId(PiglinCompanion.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TEXTURE_LEVEL =
            SynchedEntityData.defineId(PiglinCompanion.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LEVEL =
            SynchedEntityData.defineId(PiglinCompanion.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_EXPERIENCE =
            SynchedEntityData.defineId(PiglinCompanion.class, EntityDataSerializers.INT);

    private double rangedAttackDamage;
    private LivingEntity lastKilled;

    public double getRangedAttackDamage() {
        return this.rangedAttackDamage;
    }

    public void setRangedAttackDamage(double damage) {
        this.rangedAttackDamage = damage;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ATTACK_SPEED, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TEXTURE_LEVEL, 1);
        this.entityData.define(DATA_TEXTURE_VARIANT, 0);
        this.entityData.define(DATA_LEVEL, 1);
        this.entityData.define(DATA_EXPERIENCE, 0);
    }

    public int getTextureLevel() {
        return this.entityData.get(DATA_TEXTURE_LEVEL);
    }

    public void setTextureLevel(int level) {
        this.entityData.set(DATA_TEXTURE_LEVEL, level);
    }

    public int getTextureVariant() {
        return this.entityData.get(DATA_TEXTURE_VARIANT);
    }

    public void setTextureVariant(int variant) {
        this.entityData.set(DATA_TEXTURE_VARIANT, variant);
    }

    private static HashMap<Item, Float> getFoodMap() {
        HashMap<Item, Float> map = new HashMap<>();

        map.put(Items.GOLD_NUGGET, 1.0F);
        map.put(Items.GOLD_ORE, 1.5F);
        map.put(Items.GOLD_INGOT, 2.5F);
        map.put(Items.GOLD_BLOCK, 24.0F);

        return map;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new CustomAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0D, 10.0F, 20.0F, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetNoFriendliesGoal(this).setAlertOthers(PiglinCompanion.class));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ItemStack bow = this.getMainHandItem();
        if (!(bow.getItem() instanceof BowItem)) return;

        this.setItemInHand(InteractionHand.MAIN_HAND, bow);
        this.startUsingItem(InteractionHand.MAIN_HAND);

        Arrow arrow = new Arrow(this.level(), this);
        arrow.setBaseDamage(getRangedAttackDamage());
        arrow.setOwner(this);

        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333D) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double distance = Mth.sqrt((float)(dx * dx + dz * dz));

        arrow.shoot(dx, dy + distance * 0.2F, dz, 1.6F, 14 - this.level().getDifficulty().getId() * 4);

        if (!this.level().isClientSide()) {
            this.level().addFreshEntity(arrow);
        }
        this.releaseUsingItem();
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
        }

        if (this.isFood(stack)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            this.heal(getHealPointsByFood(stack));

        }

        return super.mobInteract(player, hand);
    }

    private Float getHealPointsByFood(ItemStack stack) {
        return FOOD_MAP.getOrDefault(stack.getItem(), 0.0F);
    }

    @Override
    public boolean isFood(ItemStack stack) {

        return FOOD_MAP.containsKey(stack.getItem());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TextureVariant", this.getTextureVariant());
        tag.putInt("TextureLevel", this.getTextureLevel());
        tag.putDouble("RangedDamage", this.getRangedAttackDamage());
        tag.putInt("Level", this.getLevel());
        tag.putInt("XP", this.getExperience());

    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        int textureLevel = tag.contains("TextureLevel") ? tag.getInt("TextureLevel") : -1;

        if (textureLevel <= 0 || textureLevel > 5) {
            textureLevel = 1;
            System.out.println("Fixing old PiglinCompanion: textureLevel was invalid, defaulting to 1");
        }

        int textureVariant = tag.contains("TextureVariant") ? tag.getInt("TextureVariant") : 0;

        if (textureVariant < 0 || textureVariant > 2) {
            textureVariant = getOwner().getRandom().nextInt(3);
            System.out.println("Fixing old PiglinCompanion: textureVariant was invalid, defaulting to random 0:2");
        }

        double rangedDamage = tag.contains("RangedDamage") ? tag.getDouble("RangedDamage") : 2.0D;

        if (rangedDamage < 2.0D || rangedDamage > 10.0D) {
            rangedDamage = 2.0D;
            System.out.println("Fixing old PiglinCompanion: rangedDamage was invalid, defaulting to 2.0D");
        }

        int level = tag.contains("Level") ? tag.getInt("Level") : 1;

        if (level < 1 || level > MAX_LEVEL) {
            level = 1;
            System.out.println("Fixing old PiglinCompanion: level was invalid, defaulting to 1");
        }

        int xp = tag.contains("XP") ? tag.getInt("XP") : 1;

        if (xp < 0) {
            xp = 0;
            System.out.println("Fixing old PiglinCompanion: xp was invalid, defaulting to 0");
        }

        this.setExperience(xp);
        this.setLevel(level);
        this.updateStatsByLevel(level);
        this.setTextureLevel(textureLevel);
        this.setTextureVariant(textureVariant);
        this.setRangedAttackDamage(rangedDamage);
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

    @Override
    public void aiStep() {
        super.aiStep();

        healByTicks(50, 0.5F);
        checkKills();
    }

    private void checkKills() {
        LivingEntity lastHurt = this.getLastHurtMob();

        if (!level().isClientSide && lastHurt != null && !lastHurt.isAlive() && lastHurt != lastKilled) {
            lastKilled = lastHurt;  // Запоминаем, чтобы не начислить повторно
            System.out.println("checkKills:: awarding kill score for " + lastHurt.getName().getString());
            this.awardKillScore(lastHurt, 1, this.level().damageSources().mobAttack(this));
        }
    }

    public void healByTicks(int ticksInterval, float healPoints) {
        if (!this.level().isClientSide && this.getHealth() < this.getMaxHealth()) {
            if (this.tickCount % ticksInterval == 0) {
                this.heal(healPoints);
            }
        }
    }

    private void updateStatsByLevel(int level) {
        switch (level) {
            case 1:
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(24.0D);
                this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5.0D);
                this.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(4.0D);
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3D);
                setRangedAttackDamage(2.0D);
                break;
            case 2:
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(28.0D);
                this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6.0D);
                this.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(5.0D);
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.32D);
                setRangedAttackDamage(3.0D);
                break;
            case 3:
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(32.0D);
                this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7.0D);
                this.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(7.0D);
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.34D);
                setRangedAttackDamage(7.0D);
                break;
            case 4:
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0D);
                this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10.0D);
                this.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(8.0D);
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.36D);
                setRangedAttackDamage(10.0D);
                break;
            case 5:
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(48.0D);
                this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(16.0D);
                this.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(9.0D);
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.38D);
                setRangedAttackDamage(14.0D);
                break;
            case 6:
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(60.0D);
                this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(25.0D);
                this.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(11.0D);
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.40D);
                setRangedAttackDamage(20.0D);
                break;
            default:
                this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(24.0D);
                this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5.0D);
                this.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(4.0D);
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3D);
                setRangedAttackDamage(2.0D);
                break;
        }
        this.setHealth((float) this.getAttribute(Attributes.MAX_HEALTH).getBaseValue());

        System.out.println("INFO (NEW LEVEL): current max health: " + this.getAttribute(Attributes.MAX_HEALTH).getValue());
        System.out.println("INFO (NEW LEVEL): current attack: " + this.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
        System.out.println("INFO (NEW LEVEL): current attack speed: " + this.getAttribute(Attributes.ATTACK_SPEED).getValue());
        System.out.println("INFO (NEW LEVEL): current movement speed: " + this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
        System.out.println("INFO (NEW LEVEL): current ranged attack: " + this.getRangedAttackDamage());
    }

    @Override
    public void awardKillScore(Entity killed, int score, DamageSource damageSource) {
        super.awardKillScore(killed, score, damageSource);

        if (killed instanceof LivingEntity living) {
            if (this.isTame() && living.getType().getCategory() == MobCategory.MONSTER) {
                this.addExperience(10);
            }
        }
    }

    public void addExperience(int amount) {

        System.out.println("addExperience:: amount added " + amount);

        int currentXp = getExperience();
        int newXp = currentXp + amount;

        while (newXp >= getXpForNextLevel() && getLevel() < MAX_LEVEL) {
            int xpForNext = getXpForNextLevel();
            newXp -= xpForNext;
            setLevel(getLevel() + 1);
            System.out.println("addExperience:: new level " + getLevel());

            if (this.isTame() && this.getOwner() instanceof Player player) {
                player.displayClientMessage(Component.literal(this.getDisplayName().getString() + " повысил уровень до " + getLevel()), true);
            }
        }

        setExperience(newXp);
        System.out.println("addExperience:: current xp " + getExperience());
    }

    private int getXpForNextLevel() {
        return 50 + (getLevel() - 1) * 25;
    }

//    Level getters and setters
    public int getLevel() {
        return this.entityData.get(DATA_LEVEL);
    }

    public void setLevel(int level) {

        if (level < 1 || level > MAX_LEVEL) {
            System.out.println("Level must be lower then " + MAX_LEVEL + " level and bigger then 1");
            return;
        }

        switch (level) {
            case 1:
                setTextureLevel(1);
                break;
            case 3:
                setTextureLevel(3);
                break;
            case 5:
                setTextureLevel(5);
                break;
        }
        this.entityData.set(DATA_LEVEL, level);
        updateStatsByLevel(level);
    }

    public int getExperience() {
        return this.entityData.get(DATA_EXPERIENCE);
    }

    public void setExperience(int xp) {
        this.entityData.set(DATA_EXPERIENCE, xp);
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

            LivingEntity attacker = piglin.getLastHurtByMob();
            if (attacker instanceof PiglinCompanion otherPiglin) {
                if (otherPiglin.isTame() && piglin.isTame()) {
                    return false;
                }
            }

            return true;
        }
    }
}