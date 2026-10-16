package com.fende.obesecat.entity;

import com.fende.obesecat.energy.CatFoodEnergy;
import com.fende.obesecat.registry.ModBlocks;
import com.fende.obesecat.world.FatManPlutoniumExplosion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;

public class ObeseCat extends Cat {
    private static final int EXPLOSION_COUNTDOWN_TICKS = 200;
    private static final int FLASH_TO_CRATER_DELAY_TICKS = 10;
    private static final int LITHIUM_CRATER_RADIUS = 100;
    private static final int LITHIUM_CRATER_MAX_DEPTH = 50;
    private static final int PLUTONIUM_CRATER_RADIUS = Math.max(1, LITHIUM_CRATER_RADIUS / 6);
    private static final int PLUTONIUM_CRATER_MAX_DEPTH = Math.max(1, LITHIUM_CRATER_MAX_DEPTH / 6);
    private static final EntityDataAccessor<Integer> CAT_FOOD_ENERGY =
            SynchedEntityData.defineId(ObeseCat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EXPLOSION_COUNTDOWN =
            SynchedEntityData.defineId(ObeseCat.class, EntityDataSerializers.INT);
    private int armedCraterRadius = PLUTONIUM_CRATER_RADIUS;
    private int armedCraterMaxDepth = PLUTONIUM_CRATER_MAX_DEPTH;
    private int detonationDelayTicks = 0;
    private boolean armedAtomicFireSphere = false;
    private final IEnergyStorage energyStorage = new FatManEnergyStorage();

    public ObeseCat(EntityType<? extends Cat> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CAT_FOOD_ENERGY, 0);
        builder.define(EXPLOSION_COUNTDOWN, 0);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int points = CatFoodEnergy.getFuelPoints(stack);
        if (points > 0 && this.getExplosionCountdownTicks() <= 0) {
            if (!this.level().isClientSide()) {
                feedCatFood(player, stack, points);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        super.tick();

        int countdown = this.getExplosionCountdownTicks();
        if (countdown <= 0) {
            return;
        }

        if (!this.level().isClientSide()) {
            if (countdown % 20 == 0) {
                this.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.2F + ((EXPLOSION_COUNTDOWN_TICKS - countdown) / (float) EXPLOSION_COUNTDOWN_TICKS) * 0.8F);
            }
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 2, 0.15D, 0.15D, 0.15D, 0.01D);
                if (countdown <= 60) {
                    serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.5D, this.getZ(), 3, 0.15D, 0.15D, 0.15D, 0.02D);
                }
            }

            if (countdown == 1) {
                this.entityData.set(EXPLOSION_COUNTDOWN, 0);
                if (this.level() instanceof ServerLevel serverLevel) {
                    FatManPlutoniumExplosion.detonate(serverLevel, this.blockPosition());
                }
                this.discard();
            } else {
                this.entityData.set(EXPLOSION_COUNTDOWN, countdown - 1);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("CatFoodEnergy", this.getCatFoodEnergy());
        compound.putInt("ExplosionCountdown", this.getExplosionCountdownTicks());
        compound.putInt("ArmedCraterRadius", this.armedCraterRadius);
        compound.putInt("ArmedCraterMaxDepth", this.armedCraterMaxDepth);
        compound.putInt("DetonationDelayTicks", this.detonationDelayTicks);
        compound.putBoolean("ArmedAtomicFireSphere", this.armedAtomicFireSphere);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        int storedEnergy;
        if (compound.contains("CatFoodEnergy")) {
            storedEnergy = compound.getInt("CatFoodEnergy");
        } else {
            int legacyMeals = compound.contains("ReactiveMeals")
                    ? compound.getInt("ReactiveMeals")
                    : compound.getInt("PlutoniumMeals");
            storedEnergy = CatFoodEnergy.toFe(Math.max(legacyMeals, 0) * 100);
        }
        this.entityData.set(CAT_FOOD_ENERGY, Math.max(0, Math.min(
                CatFoodEnergy.toFe(CatFoodEnergy.FAT_MAN_CAPACITY_POINTS),
                storedEnergy
        )));
        this.entityData.set(EXPLOSION_COUNTDOWN, Math.max(compound.getInt("ExplosionCountdown"), 0));
        boolean legacyLithium = compound.getBoolean("ArmedCustomLithium");
        this.armedCraterRadius = compound.contains("ArmedCraterRadius")
                ? Math.max(1, compound.getInt("ArmedCraterRadius"))
                : legacyLithium ? LITHIUM_CRATER_RADIUS : PLUTONIUM_CRATER_RADIUS;
        this.armedCraterMaxDepth = compound.contains("ArmedCraterMaxDepth")
                ? Math.max(1, compound.getInt("ArmedCraterMaxDepth"))
                : legacyLithium ? LITHIUM_CRATER_MAX_DEPTH : PLUTONIUM_CRATER_MAX_DEPTH;
        this.detonationDelayTicks = Math.max(compound.getInt("DetonationDelayTicks"), 0);
        this.armedAtomicFireSphere = compound.contains("ArmedAtomicFireSphere")
                ? compound.getBoolean("ArmedAtomicFireSphere")
                : legacyLithium;
    }

    public int getReactiveMeals() {
        return Math.min(3, getCatFoodPoints() / 100);
    }

    public int getCatFoodEnergy() {
        return this.entityData.get(CAT_FOOD_ENERGY);
    }

    public int getCatFoodPoints() {
        return CatFoodEnergy.toPoints(getCatFoodEnergy());
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public int getExplosionCountdownTicks() {
        return this.entityData.get(EXPLOSION_COUNTDOWN);
    }

    public int getExplosionCountdownSeconds() {
        int ticks = this.getExplosionCountdownTicks();
        return ticks <= 0 ? 0 : (ticks + 19) / 20;
    }

    public float getWidthScale() {
        float chargeStages = Math.min(3.0F, getCatFoodPoints() / 100.0F);
        return 1.5F + (0.25F * chargeStages);
    }

    public float getHeightScale() {
        float chargeStages = Math.min(3.0F, getCatFoodPoints() / 100.0F);
        return 1.2F + (0.15F * chargeStages);
    }

    private void feedCatFood(Player player, ItemStack stack, int points) {
        int received = energyStorage.receiveEnergy(CatFoodEnergy.toFe(points), false);
        if (received < CatFoodEnergy.toFe(points)) {
            player.displayClientMessage(Component.translatable("message.obesecat.fat_man.full"), true);
            return;
        }

        this.setPersistenceRequired();
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        this.playSound(SoundEvents.CAT_EAT, 1.0F, 0.9F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.5D, this.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.05D);
        }

        if (getExplosionCountdownTicks() > 0) {
            player.displayClientMessage(Component.translatable("message.obesecat.fat_man.armed"), true);
        } else {
            player.displayClientMessage(Component.translatable(
                    "message.obesecat.fat_man.charge",
                    getCatFoodPoints(),
                    CatFoodEnergy.FAT_MAN_DETONATION_POINTS
            ), true);
        }
    }

    private void armFromCatFood() {
        if (getExplosionCountdownTicks() > 0
                || getCatFoodPoints() < CatFoodEnergy.FAT_MAN_DETONATION_POINTS) {
            return;
        }
        this.armedCraterRadius = LITHIUM_CRATER_RADIUS;
        this.armedCraterMaxDepth = LITHIUM_CRATER_MAX_DEPTH;
        this.armedAtomicFireSphere = true;
        this.entityData.set(EXPLOSION_COUNTDOWN, EXPLOSION_COUNTDOWN_TICKS);
        this.playSound(SoundEvents.TNT_PRIMED, 1.0F, 1.0F);
    }

    private final class FatManEnergyStorage implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (maxReceive <= 0 || getExplosionCountdownTicks() > 0) {
                return 0;
            }
            int capacity = CatFoodEnergy.toFe(CatFoodEnergy.FAT_MAN_CAPACITY_POINTS);
            int accepted = Math.min(maxReceive, capacity - getCatFoodEnergy());
            if (!simulate && accepted > 0) {
                entityData.set(CAT_FOOD_ENERGY, getCatFoodEnergy() + accepted);
                setPersistenceRequired();
                armFromCatFood();
            }
            return accepted;
        }

        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return getCatFoodEnergy(); }
        @Override public int getMaxEnergyStored() { return CatFoodEnergy.toFe(CatFoodEnergy.FAT_MAN_CAPACITY_POINTS); }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return getExplosionCountdownTicks() <= 0; }
    }
}
