package com.fende.obesecat.entity;

import com.fende.obesecat.network.NuclearFlashPayload;
import com.fende.obesecat.registry.ModBlocks;
import com.fende.obesecat.registry.ModItems;
import com.fende.obesecat.world.AtomicFireSphere;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.neoforged.neoforge.network.PacketDistributor;

public class ObeseCat extends Cat {
    private static final int MAX_REACTIVE_MEALS = 3;
    private static final int EXPLOSION_COUNTDOWN_TICKS = 200;
    private static final int FLASH_TO_CRATER_DELAY_TICKS = 10;
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
        if (stack.is(ModItems.PLUTONIUM_CAT_FOOD.get()) && this.getExplosionCountdownTicks() <= 0) {
            if (!this.level().isClientSide()) {
                feedReactiveFood(player, stack, PLUTONIUM_CRATER_RADIUS, PLUTONIUM_CRATER_MAX_DEPTH, false);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        if (stack.is(ModItems.LITHIUM_DEUTERIDE_CAT_FOOD.get()) && this.getExplosionCountdownTicks() <= 0) {
            if (!this.level().isClientSide()) {
                feedReactiveFood(player, stack, LITHIUM_CRATER_RADIUS, LITHIUM_CRATER_MAX_DEPTH, true);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.detonationDelayTicks > 0) {
            this.detonationDelayTicks--;
            if (this.detonationDelayTicks <= 0) {
                explodeFatMan();
                this.discard();
            }
            return;
        }

        int countdown = this.getExplosionCountdownTicks();
        if (countdown <= 0) {
            return;
        }

        if (!this.level().isClientSide()) {
            if (countdown == 1) {
                this.entityData.set(EXPLOSION_COUNTDOWN, 0);
                triggerNuclearFlash(this.blockPosition(), this.armedCraterRadius);
                this.detonationDelayTicks = FLASH_TO_CRATER_DELAY_TICKS;
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
        int meals = compound.contains("ReactiveMeals") ? compound.getInt("ReactiveMeals") : compound.getInt("PlutoniumMeals");
        this.entityData.set(REACTIVE_MEALS, Math.min(meals, MAX_REACTIVE_MEALS));
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
        return 1.5F + (0.25F * this.getReactiveMeals());
    }

    public float getHeightScale() {
        return 1.2F + (0.15F * this.getReactiveMeals());
    }

    private void feedReactiveFood(Player player, ItemStack stack, int craterRadius, int craterMaxDepth, boolean atomicFireSphere) {
        int meals = Math.min(this.getReactiveMeals() + 1, MAX_REACTIVE_MEALS);
        this.entityData.set(REACTIVE_MEALS, meals);
        this.setPersistenceRequired();

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (meals >= MAX_REACTIVE_MEALS) {
            this.armedCraterRadius = craterRadius;
            this.armedCraterMaxDepth = craterMaxDepth;
            this.armedAtomicFireSphere = atomicFireSphere;
            this.entityData.set(EXPLOSION_COUNTDOWN, EXPLOSION_COUNTDOWN_TICKS);
        }
    }

    private void explodeFatMan() {
        BlockPos origin = this.blockPosition();
        BlockPos craterCenter = carveCustomCrater(this.armedCraterRadius, this.armedCraterMaxDepth);
        if (this.armedAtomicFireSphere && this.level() instanceof ServerLevel serverLevel) {
            placeTrinititeAtCraterCenter(serverLevel, craterCenter);
            AtomicFireSphere.createDelayed(serverLevel, origin, 2);
        }
    }

    private BlockPos carveCustomCrater(int craterRadius, int craterMaxDepth) {
        Level level = this.level();
        BlockPos origin = this.blockPosition();
        playCustomBlastEffects(level, origin, craterRadius);
        deleteMobsAtGroundZero(level, origin, craterRadius, craterMaxDepth);

        int centerX = origin.getX();
        int centerZ = origin.getZ();
        int minY = level.getMinBuildHeight() + 1;
        int rimPadding = Math.max(3, craterRadius / 7);
        BlockPos craterCenter = origin;

        for (int dx = -craterRadius - rimPadding; dx <= craterRadius + rimPadding; dx++) {
            for (int dz = -craterRadius - rimPadding; dz <= craterRadius + rimPadding; dz++) {
                int x = centerX + dx;
                int z = centerZ + dz;
                double distance = Math.sqrt((double) dx * dx + (double) dz * dz);
                double rimNoise = (noise(x, z, 17L) * 2.0D) - 1.0D;
                double pocketNoise = noise(x, z, 41L);
                double effectiveRadius = craterRadius + (rimNoise * craterRadius * 0.13D) + (pocketNoise * craterRadius * 0.06D);
                if (distance > effectiveRadius) {
                    continue;
                }

                double normalized = distance / effectiveRadius;
                if (normalized > 0.92D && noise(x, z, 73L) > 0.55D) {
                    continue;
                }

                int surfaceY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, origin.getY(), z)).getY();
                int maxDepth = (int) (craterMaxDepth * (1.0D - Math.pow(normalized, 1.8D)));
                maxDepth += (int) ((noise(x, z, 113L) - 0.5D) * Math.max(2.0D, craterMaxDepth * 0.24D));
                if (normalized > 0.72D) {
                    maxDepth = (int) (maxDepth * (0.5D + noise(x, z, 151L) * 0.45D));
                }
                if (maxDepth <= 0) {
                    continue;
                }

                int topY = surfaceY + (normalized < 0.28D ? 3 : 1);
                int bottomY = Math.max(minY, surfaceY - maxDepth);
                if (dx == 0 && dz == 0) {
                    craterCenter = new BlockPos(x, bottomY, z);
                }

                for (int y = topY; y >= bottomY; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir() && state.getDestroySpeed(level, pos) >= 0.0F) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }

                maybePlaceBlastFire(level, x, bottomY, z, normalized);
            }
        }

        return craterCenter;
    }

    private void placeTrinititeAtCraterCenter(ServerLevel level, BlockPos craterCenter) {
        BlockState trinitite = ModBlocks.TRINITITE.get().defaultBlockState();
        BlockPos.MutableBlockPos mutablePos = craterCenter.mutable();

        for (int offset = 0; offset <= 8; offset++) {
            mutablePos.set(craterCenter.getX(), craterCenter.getY() + offset, craterCenter.getZ());
            BlockState state = level.getBlockState(mutablePos);
            if (state.isAir() || state.canBeReplaced()) {
                level.setBlock(mutablePos, trinitite, 3);
                return;
            }
        }

        level.setBlock(craterCenter.above(), trinitite, 3);
    }

    private void deleteMobsAtGroundZero(Level level, BlockPos origin, int craterRadius, int craterMaxDepth) {
        AABB blastBox = new AABB(
                origin.getX() - craterRadius - 2.0D,
                origin.getY() - craterMaxDepth - 4.0D,
                origin.getZ() - craterRadius - 2.0D,
                origin.getX() + craterRadius + 2.0D,
                origin.getY() + Math.max(12.0D, craterRadius * 0.4D),
                origin.getZ() + craterRadius + 2.0D
        );

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, blastBox, entity -> entity != this && !(entity instanceof Player))) {
            double dx = entity.getX() - origin.getX();
            double dz = entity.getZ() - origin.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            double effectiveRadius = craterRadius + ((noise(entity.blockPosition().getX(), entity.blockPosition().getZ(), 307L) * 2.0D) - 1.0D) * craterRadius * 0.16D;
            if (distance <= effectiveRadius) {
                entity.discard();
            }
        }
    }

    private void playCustomBlastEffects(Level level, BlockPos origin, int craterRadius) {
        if (level instanceof ServerLevel serverLevel) {
            int emitterCount = Math.max(1, craterRadius / 25);
            int smokeCount = Math.max(35, craterRadius * 2);
            double smokeSpread = craterRadius * 0.8D;
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D, emitterCount, 5.0D, 1.5D, 5.0D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, origin.getX() + 0.5D, origin.getY() + 2.0D, origin.getZ() + 0.5D, smokeCount, smokeSpread, Math.max(6.0D, craterRadius * 0.18D), smokeSpread, 0.08D);
        }
    }

    private void triggerNuclearFlash(BlockPos origin, int craterRadius) {
        if (this.level() instanceof ServerLevel serverLevel) {
            double flashRadius = craterRadius >= LITHIUM_CRATER_RADIUS ? 384.0D : 128.0D;
            float flashIntensity = craterRadius >= LITHIUM_CRATER_RADIUS ? 1.0F : 0.65F;
            PacketDistributor.sendToPlayersNear(
                    serverLevel,
                    null,
                    origin.getX() + 0.5D,
                    origin.getY() + 0.5D,
                    origin.getZ() + 0.5D,
                    flashRadius,
                    new NuclearFlashPayload(origin, flashIntensity)
            );
        }
    }

    private void maybePlaceBlastFire(Level level, int x, int y, int z, double normalized) {
        if (normalized < 0.22D || normalized > 0.95D || noise(x, z, 211L) < 0.93D) {
            return;
        }

        BlockPos firePos = new BlockPos(x, y, z);
        BlockState fire = Blocks.FIRE.defaultBlockState();
        if (level.getBlockState(firePos).isAir() && fire.canSurvive(level, firePos)) {
            level.setBlock(firePos, fire, 3);
        }
    }

    private static double noise(int x, int z, long salt) {
        long value = (x * 341873128712L) ^ (z * 132897987541L) ^ salt;
        value = (value ^ (value >>> 33)) * 0xff51afd7ed558ccdL;
        value = (value ^ (value >>> 33)) * 0xc4ceb9fe1a85ec53L;
        value = value ^ (value >>> 33);
        return (double) (value & 0xFFFFFFL) / (double) 0x1000000L;
    }
}
