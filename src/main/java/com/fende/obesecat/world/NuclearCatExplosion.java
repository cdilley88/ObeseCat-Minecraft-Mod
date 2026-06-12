package com.fende.obesecat.world;

import com.fende.obesecat.network.NuclearFlashPayload;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NuclearCatExplosion {
    public static final int LITHIUM_CRATER_RADIUS = 100;
    public static final int LITHIUM_CRATER_MAX_DEPTH = 50;
    public static final int PLUTONIUM_CRATER_RADIUS = Math.max(1, LITHIUM_CRATER_RADIUS / 6);
    public static final int PLUTONIUM_CRATER_MAX_DEPTH = Math.max(1, LITHIUM_CRATER_MAX_DEPTH / 6);
    public static final int FLASH_TO_CRATER_DELAY_TICKS = 10;

    private static final List<PendingExplosion> PENDING_EXPLOSIONS = new ArrayList<>();

    private NuclearCatExplosion() {
    }

    public static void flashThenDetonate(ServerLevel level, BlockPos origin, int craterRadius, int craterMaxDepth) {
        flashThenDetonateDelayed(level, origin, craterRadius, craterMaxDepth, 0);
    }

    public static void flashThenDetonateDelayed(ServerLevel level, BlockPos origin, int craterRadius, int craterMaxDepth, int preFlashDelayTicks) {
        if (preFlashDelayTicks > 0) {
            PENDING_EXPLOSIONS.add(new PendingExplosion(level, origin.immutable(), craterRadius, craterMaxDepth, preFlashDelayTicks, true));
            return;
        }

        triggerFlash(level, origin, craterRadius);
        PENDING_EXPLOSIONS.add(new PendingExplosion(level, origin.immutable(), craterRadius, craterMaxDepth, FLASH_TO_CRATER_DELAY_TICKS, false));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || PENDING_EXPLOSIONS.isEmpty()) {
            return;
        }

        Iterator<PendingExplosion> iterator = PENDING_EXPLOSIONS.iterator();
        while (iterator.hasNext()) {
            PendingExplosion pending = iterator.next();
            if (pending.level != level) {
                continue;
            }

            pending.delayTicks--;
            if (pending.delayTicks <= 0) {
                if (pending.waitingToFlash) {
                    triggerFlash(level, pending.origin, pending.craterRadius);
                    pending.delayTicks = FLASH_TO_CRATER_DELAY_TICKS;
                    pending.waitingToFlash = false;
                } else {
                    carveCrater(level, pending.origin, pending.craterRadius, pending.craterMaxDepth);
                    iterator.remove();
                }
            }
        }
    }

    public static void carveCrater(ServerLevel level, BlockPos origin, int craterRadius, int craterMaxDepth) {
        playBlastEffects(level, origin, craterRadius);
        deleteMobsAtGroundZero(level, origin, craterRadius, craterMaxDepth);

        int centerX = origin.getX();
        int centerZ = origin.getZ();
        int minY = level.getMinBuildHeight() + 1;
        int rimPadding = Math.max(3, craterRadius / 7);

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
    }

    private static void triggerFlash(ServerLevel level, BlockPos origin, int craterRadius) {
        double flashRadius = craterRadius >= LITHIUM_CRATER_RADIUS ? 384.0D : 128.0D;
        float flashIntensity = craterRadius >= LITHIUM_CRATER_RADIUS ? 1.0F : 0.65F;
        PacketDistributor.sendToPlayersNear(
                level,
                null,
                origin.getX() + 0.5D,
                origin.getY() + 0.5D,
                origin.getZ() + 0.5D,
                flashRadius,
                new NuclearFlashPayload(origin, flashIntensity)
        );
    }

    private static void playBlastEffects(ServerLevel level, BlockPos origin, int craterRadius) {
        int emitterCount = Math.max(1, craterRadius / 25);
        int smokeCount = Math.max(35, craterRadius * 2);
        double smokeSpread = craterRadius * 0.8D;
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D, emitterCount, 5.0D, 1.5D, 5.0D, 0.0D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, origin.getX() + 0.5D, origin.getY() + 2.0D, origin.getZ() + 0.5D, smokeCount, smokeSpread, Math.max(6.0D, craterRadius * 0.18D), smokeSpread, 0.08D);
    }

    private static void deleteMobsAtGroundZero(ServerLevel level, BlockPos origin, int craterRadius, int craterMaxDepth) {
        AABB blastBox = new AABB(
                origin.getX() - craterRadius - 2.0D,
                origin.getY() - craterMaxDepth - 4.0D,
                origin.getZ() - craterRadius - 2.0D,
                origin.getX() + craterRadius + 2.0D,
                origin.getY() + Math.max(12.0D, craterRadius * 0.4D),
                origin.getZ() + craterRadius + 2.0D
        );

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, blastBox, entity -> !(entity instanceof Player))) {
            double dx = entity.getX() - origin.getX();
            double dz = entity.getZ() - origin.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            double effectiveRadius = craterRadius + ((noise(entity.blockPosition().getX(), entity.blockPosition().getZ(), 307L) * 2.0D) - 1.0D) * craterRadius * 0.16D;
            if (distance <= effectiveRadius) {
                entity.discard();
            }
        }
    }

    private static void maybePlaceBlastFire(ServerLevel level, int x, int y, int z, double normalized) {
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

    private static final class PendingExplosion {
        private final ServerLevel level;
        private final BlockPos origin;
        private final int craterRadius;
        private final int craterMaxDepth;
        private int delayTicks;
        private boolean waitingToFlash;

        private PendingExplosion(ServerLevel level, BlockPos origin, int craterRadius, int craterMaxDepth, int delayTicks, boolean waitingToFlash) {
            this.level = level;
            this.origin = origin;
            this.craterRadius = craterRadius;
            this.craterMaxDepth = craterMaxDepth;
            this.delayTicks = delayTicks;
            this.waitingToFlash = waitingToFlash;
        }
    }
}
