package com.fende.obesecat.entity;

import com.fende.obesecat.item.NuclearCatFoodItem;
import com.fende.obesecat.world.FatManPlutoniumExplosion;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ObeseCat extends Cat {
    private static final int EXPLOSION_COUNTDOWN_TICKS = 200;
    private static final int LITHIUM_CRATER_RADIUS = 100;
    private static final int LITHIUM_CRATER_MAX_DEPTH = 50;
    private static final int PLUTONIUM_CRATER_RADIUS = Math.max(1, LITHIUM_CRATER_RADIUS / 6);
    private static final int PLUTONIUM_CRATER_MAX_DEPTH = Math.max(1, LITHIUM_CRATER_MAX_DEPTH / 6);

    private static final EntityDataAccessor<Integer> REACTIVE_MEALS =
            SynchedEntityData.defineId(ObeseCat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EXPLOSION_COUNTDOWN =
            SynchedEntityData.defineId(ObeseCat.class, EntityDataSerializers.INT);

    private int armedCraterRadius = PLUTONIUM_CRATER_RADIUS;
    private int armedCraterMaxDepth = PLUTONIUM_CRATER_MAX_DEPTH;
    private int detonationDelayTicks = 0;
    private boolean armedAtomicFireSphere = false;

    public ObeseCat(EntityType<? extends Cat> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(REACTIVE_MEALS, 0);
        builder.define(EXPLOSION_COUNTDOWN, 0);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof NuclearCatFoodItem food && this.getExplosionCountdownTicks() <= 0) {
            if (!this.level().isClientSide()) {
                feedNuclearFood(player, stack, food);
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
        compound.putInt("ReactiveMeals", this.getReactiveMeals());
        compound.putInt("ExplosionCountdown", this.getExplosionCountdownTicks());
        compound.putInt("ArmedCraterRadius", this.armedCraterRadius);
        compound.putInt("ArmedCraterMaxDepth", this.armedCraterMaxDepth);
        compound.putInt("DetonationDelayTicks", this.detonationDelayTicks);
        compound.putBoolean("ArmedAtomicFireSphere", this.armedAtomicFireSphere);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        int meals = 0;
        if (compound.contains("ReactiveMeals")) {
            meals = compound.getInt("ReactiveMeals");
        } else if (compound.contains("PlutoniumMeals")) {
            meals = compound.getInt("PlutoniumMeals");
        } else if (compound.contains("CatFoodEnergy")) {
            meals = Math.min(3, compound.getInt("CatFoodEnergy") / 10_000);
        }
        this.entityData.set(REACTIVE_MEALS, Math.max(0, Math.min(3, meals)));
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
        return this.entityData.get(REACTIVE_MEALS);
    }

    public int getExplosionCountdownTicks() {
        return this.entityData.get(EXPLOSION_COUNTDOWN);
    }

    public int getExplosionCountdownSeconds() {
        int ticks = this.getExplosionCountdownTicks();
        return ticks <= 0 ? 0 : (ticks + 19) / 20;
    }

    public float getWidthScale() {
        return 1.5F + (0.25F * getReactiveMeals());
    }

    public float getHeightScale() {
        return 1.2F + (0.15F * getReactiveMeals());
    }

    private void feedNuclearFood(Player player, ItemStack stack, NuclearCatFoodItem food) {
        int currentMeals = getReactiveMeals();
        if (currentMeals >= 3) {
            player.displayClientMessage(Component.translatable("message.obesecat.fat_man.full"), true);
            return;
        }

        int newMeals = Math.min(3, currentMeals + food.getMealsProvided());
        this.entityData.set(REACTIVE_MEALS, newMeals);
        this.setPersistenceRequired();

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        this.playSound(SoundEvents.CAT_EAT, 1.0F, 0.9F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.5D, this.getZ(), 5, 0.2D, 0.2D, 0.2D, 0.05D);
        }

        if (newMeals >= 3) {
            this.armedCraterRadius = food.isInstantDetonation() ? LITHIUM_CRATER_RADIUS : PLUTONIUM_CRATER_RADIUS;
            this.armedCraterMaxDepth = food.isInstantDetonation() ? LITHIUM_CRATER_MAX_DEPTH : PLUTONIUM_CRATER_MAX_DEPTH;
            this.armedAtomicFireSphere = food.isInstantDetonation();
            this.entityData.set(EXPLOSION_COUNTDOWN, EXPLOSION_COUNTDOWN_TICKS);
            this.playSound(SoundEvents.TNT_PRIMED, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable("message.obesecat.fat_man.armed"), true);
        } else {
            player.displayClientMessage(Component.translatable("message.obesecat.fat_man.charge", newMeals, 3), true);
        }
    }
}
