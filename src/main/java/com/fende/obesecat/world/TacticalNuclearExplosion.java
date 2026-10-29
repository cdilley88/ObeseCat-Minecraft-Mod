package com.fende.obesecat.world;

import com.fende.obesecat.network.TacticalNuclearPayload;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Scaled tactical sub-kiloton (Davy Crockett style) nuclear explosion engine.
 * Adapted directly from the Fat Man Plutonium engine:
 * - Scaled to a compact, punchy tactical crater (radius ~10, depth ~5)
 * - Zero floating leaves and complete canopy vaporized in blast column
 * - Perimeter log charring (converting logs to coal blocks) and scorched earth
 * - Miniature mushroom cloud with prompt optical ionization and shockwave roar
 */
public final class TacticalNuclearExplosion {
    public static final int CRATER_RADIUS = 20;
    public static final int CRATER_MAX_DEPTH = 10;
    public static final int PERIMETER_SCORCH_RADIUS = 32;
    public static final int SHOCKWAVE_DELAY_TICKS = 4;
    public static final int TOTAL_SIMULATION_TICKS = 200;

    private static final double PACKET_DIST_RADIUS = 256.0D;
    private static final List<DetonationSimulation> ACTIVE_DETONATIONS = new ArrayList<>();
    private static final List<PendingDelayedDetonation> PENDING_DETONATIONS = new ArrayList<>();

    private TacticalNuclearExplosion() {
    }

    public static void detonate(ServerLevel level, BlockPos origin) {
        BlockPos blastOrigin = origin.immutable();
        detonateSpectacle(level, blastOrigin);
        ACTIVE_DETONATIONS.add(new DetonationSimulation(level, blastOrigin));
    }

    public static void detonateDelayed(ServerLevel level, BlockPos origin, int delayTicks) {
        if (delayTicks <= 0) {
            detonate(level, origin);
            return;
        }
        PENDING_DETONATIONS.add(new PendingDelayedDetonation(level, origin.immutable(), delayTicks));
    }

    public static void detonateMirv(ServerLevel level, BlockPos centerOrigin) {
        detonateMirv(level, centerOrigin, 0);
    }

    public static void detonateMirv(ServerLevel level, BlockPos centerOrigin, int baseDelayTicks) {
        double cx = centerOrigin.getX() + 0.5D;
        double cy = centerOrigin.getY() + 1.0D;
        double cz = centerOrigin.getZ() + 0.5D;

        level.playSound(null, cx, cy, cz, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.BLOCKS, 6.0F, 0.45F);
        level.playSound(null, cx, cy, cz, SoundEvents.WARDEN_ROAR, SoundSource.BLOCKS, 5.0F, 1.8F);
        level.playSound(null, cx, cy, cz, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 5.0F, 1.3F);

        // Distribute 7 tactical warheads randomly across a ~35-42m area
        int warheadCount = 7;
        double spreadRadius = 38.0D;

        for (int i = 0; i < warheadCount; i++) {
            double angle = (i * (Math.PI * 2.0D) / (warheadCount - 1)) + (level.random.nextDouble() * 0.45D - 0.22D);
            double dist = (i == 0)
                    ? level.random.nextDouble() * 5.0D
                    : 12.0D + level.random.nextDouble() * (spreadRadius - 12.0D);

            int x = centerOrigin.getX() + (int) Math.round(Math.cos(angle) * dist);
            int z = centerOrigin.getZ() + (int) Math.round(Math.sin(angle) * dist);
            int surfaceY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z)).getY();
            BlockPos warheadPos = new BlockPos(x, Math.max(level.getMinBuildHeight() + 2, surfaceY), z);

            int delayTicks = baseDelayTicks + i * 4 + level.random.nextInt(3);
            detonateDelayed(level, warheadPos, delayTicks);
        }
    }

    public static void detonateSpectacle(ServerLevel level, BlockPos origin) {
        BlockPos blastOrigin = origin.immutable();

        // 1. Dispatch 3D tactical mushroom cloud & overlay client packet
        PacketDistributor.sendToPlayersNear(
                level,
                null,
                blastOrigin.getX() + 0.5D,
                blastOrigin.getY() + 0.5D,
                blastOrigin.getZ() + 0.5D,
                PACKET_DIST_RADIUS,
                new TacticalNuclearPayload(blastOrigin, TOTAL_SIMULATION_TICKS, 1.0F)
        );

        // 2. High-energy prompt ionization flash and acoustic surge
        spawnPromptIonization(level, blastOrigin);
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        // Process pending delayed detonations
        if (!PENDING_DETONATIONS.isEmpty()) {
            Iterator<PendingDelayedDetonation> pIt = PENDING_DETONATIONS.iterator();
            while (pIt.hasNext()) {
                PendingDelayedDetonation pending = pIt.next();
                if (pending.level != level) {
                    continue;
                }
                pending.delayTicks--;
                if (pending.delayTicks <= 0) {
                    detonate(pending.level, pending.origin);
                    pIt.remove();
                }
            }
        }

        // Process active detonation simulations
        if (ACTIVE_DETONATIONS.isEmpty()) {
            return;
        }

        Iterator<DetonationSimulation> iterator = ACTIVE_DETONATIONS.iterator();
        while (iterator.hasNext()) {
            DetonationSimulation sim = iterator.next();
            if (sim.level != level) {
                continue;
            }

            sim.tick++;

            if (sim.tick == SHOCKWAVE_DELAY_TICKS) {
                executeCraterDetonation(sim.level, sim.origin);
            }

            if (sim.tick >= 20 && sim.tick <= 140 && sim.tick % 3 == 0) {
                spawnTacticalFallout(sim.level, sim.origin);
            }

            if (sim.tick >= TOTAL_SIMULATION_TICKS) {
                iterator.remove();
            }
        }
    }

    private static void executeCraterDetonation(ServerLevel level, BlockPos origin) {
        carveCustomCrater(level, origin, CRATER_RADIUS, CRATER_MAX_DEPTH);
        scorchPerimeter(level, origin, CRATER_RADIUS, PERIMETER_SCORCH_RADIUS);
    }

    private static BlockPos carveCustomCrater(ServerLevel level, BlockPos origin, int craterRadius, int craterMaxDepth) {
        playBlastArrivalEffects(level, origin, craterRadius);
        applyTacticalShockwavePhysics(level, origin, craterRadius, craterMaxDepth);

        int centerX = origin.getX();
        int centerZ = origin.getZ();
        int minY = level.getMinBuildHeight() + 1;
        int rimPadding = Math.max(2, craterRadius / 5);
        BlockPos craterCenter = origin;
        BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();

        for (int dx = -craterRadius - rimPadding; dx <= craterRadius + rimPadding; dx++) {
            for (int dz = -craterRadius - rimPadding; dz <= craterRadius + rimPadding; dz++) {
                int x = centerX + dx;
                int z = centerZ + dz;

                if (!level.hasChunkAt(mutPos.set(x, origin.getY(), z))) {
                    continue;
                }

                double distance = Math.sqrt((double) dx * dx + (double) dz * dz);
                double rimNoise = (noise(x, z, 19L) * 2.0D) - 1.0D;
                double pocketNoise = noise(x, z, 47L);
                double effectiveRadius = craterRadius + (rimNoise * craterRadius * 0.14D) + (pocketNoise * craterRadius * 0.08D);
                if (distance > effectiveRadius) {
                    continue;
                }

                double normalized = distance / effectiveRadius;
                if (normalized > 0.94D && noise(x, z, 79L) > 0.60D) {
                    continue;
                }

                int surfaceY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutPos.set(x, origin.getY(), z)).getY();
                int canopyY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, mutPos.set(x, origin.getY(), z)).getY();
                int maxDepth = (int) (craterMaxDepth * (1.0D - Math.pow(normalized, 1.8D)));
                maxDepth += (int) ((noise(x, z, 113L) - 0.5D) * Math.max(1.0D, craterMaxDepth * 0.28D));
                if (normalized > 0.75D) {
                    maxDepth = (int) (maxDepth * (0.5D + noise(x, z, 151L) * 0.50D));
                }
                if (maxDepth <= 0) {
                    continue;
                }

                int topY = Math.max(surfaceY + (normalized < 0.30D ? 3 : 2), canopyY + 1);
                int bottomY = Math.max(minY, surfaceY - maxDepth);
                if (dx == 0 && dz == 0) {
                    craterCenter = new BlockPos(x, bottomY, z);
                }

                for (int y = topY; y >= bottomY; y--) {
                    mutPos.set(x, y, z);
                    BlockState state = level.getBlockState(mutPos);
                    if (!state.isAir() && state.getDestroySpeed(level, mutPos) >= 0.0F) {
                        level.setBlock(mutPos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }

                // Clean up any lingering leaves or foliage above topY so zero leaves float
                for (int y = topY + 1; y <= topY + 14; y++) {
                    mutPos.set(x, y, z);
                    BlockState state = level.getBlockState(mutPos);
                    if (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.is(BlockTags.FLOWERS) || state.is(Blocks.VINE)) {
                        level.setBlock(mutPos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }

                maybePlaceBlastFire(level, mutPos, x, bottomY, z, normalized);
            }
        }

        return craterCenter;
    }

    private static void scorchPerimeter(ServerLevel level, BlockPos origin, int innerRadius, int outerRadius) {
        int centerX = origin.getX();
        int centerZ = origin.getZ();
        int minY = level.getMinBuildHeight() + 1;
        BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();

        for (int dx = -outerRadius; dx <= outerRadius; dx++) {
            for (int dz = -outerRadius; dz <= outerRadius; dz++) {
                double distance = Math.sqrt((double) dx * dx + (double) dz * dz);
                if (distance <= innerRadius * 0.88D || distance > outerRadius) {
                    continue;
                }

                int x = centerX + dx;
                int z = centerZ + dz;

                if (!level.hasChunkAt(mutPos.set(x, origin.getY(), z))) {
                    continue;
                }

                double edgeNoise = noise(x, z, 521L);
                double effectiveOuter = outerRadius - (edgeNoise * 2.5D);
                if (distance > effectiveOuter) {
                    continue;
                }

                int canopyY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, mutPos.set(x, origin.getY(), z)).getY();
                int groundY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutPos.set(x, origin.getY(), z)).getY();
                int startY = Math.max(canopyY, groundY);
                int scanLimit = Math.max(minY, Math.min(groundY - 2, startY - 25));

                for (int y = startY; y >= scanLimit; y--) {
                    mutPos.set(x, y, z);
                    BlockState state = level.getBlockState(mutPos);
                    if (state.isAir()) {
                        continue;
                    }

                    if (state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS) || state.is(BlockTags.CROPS)
                            || state.is(BlockTags.SAPLINGS) || state.is(Blocks.VINE)) {
                        level.setBlock(mutPos, Blocks.AIR.defaultBlockState(), 2);
                    } else if (state.is(BlockTags.LOGS)) {
                        level.setBlock(mutPos, Blocks.COAL_BLOCK.defaultBlockState(), 2);
                    } else if (state.is(Blocks.ICE) || state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK)) {
                        level.setBlock(mutPos, Blocks.AIR.defaultBlockState(), 2);
                    } else if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.DIRT)
                            || state.is(Blocks.PODZOL) || state.is(Blocks.ROOTED_DIRT)) {
                        double scorchRoll = noise(x, z, 888L);
                        BlockState scorched = scorchRoll < 0.50D ? Blocks.COARSE_DIRT.defaultBlockState()
                                : scorchRoll < 0.78D ? Blocks.SMOOTH_BASALT.defaultBlockState()
                                : Blocks.GRAVEL.defaultBlockState();
                        level.setBlock(mutPos, scorched, 2);

                        if (scorchRoll > 0.88D && distance < outerRadius * 0.95D) {
                            mutPos.set(x, y + 1, z);
                            if (level.getBlockState(mutPos).isAir() && Blocks.FIRE.defaultBlockState().canSurvive(level, mutPos)) {
                                level.setBlock(mutPos, Blocks.FIRE.defaultBlockState(), 2);
                            }
                        }
                        break;
                    } else if (state.is(Blocks.WATER) || state.is(Blocks.BUBBLE_COLUMN) || state.is(BlockTags.SAND)
                            || state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.BEDROCK)) {
                        break;
                    }
                }
            }
        }
    }

    private static void applyTacticalShockwavePhysics(ServerLevel level, BlockPos origin, int craterRadius, int craterMaxDepth) {
        AABB blastBox = new AABB(
                origin.getX() - PERIMETER_SCORCH_RADIUS - 2.0D,
                origin.getY() - craterMaxDepth - 4.0D,
                origin.getZ() - PERIMETER_SCORCH_RADIUS - 2.0D,
                origin.getX() + PERIMETER_SCORCH_RADIUS + 2.0D,
                origin.getY() + 18.0D,
                origin.getZ() + PERIMETER_SCORCH_RADIUS + 2.0D
        );

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, blastBox)) {
            double dx = entity.getX() - origin.getX();
            double dy = entity.getY() - origin.getY();
            double dz = entity.getZ() - origin.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance <= craterRadius) {
                if (entity instanceof Player player) {
                    if (!player.isCreative() && !player.isSpectator()) {
                        float damage = (float) Math.max(16.0D, (1.0D - (distance / (double) craterRadius)) * 60.0D);
                        player.hurt(level.damageSources().explosion(null, null), damage);
                    }
                } else {
                    entity.discard();
                    continue;
                }
            } else if (distance <= PERIMETER_SCORCH_RADIUS) {
                float damage = (float) Math.max(6.0D, (1.0D - (distance / (double) PERIMETER_SCORCH_RADIUS)) * 24.0D);
                entity.hurt(level.damageSources().explosion(null, null), damage);
                entity.igniteForSeconds(8);
            }

            // Shockwave impulse knockback
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 0.001D && distance <= PERIMETER_SCORCH_RADIUS) {
                double impulse = Math.max(0.3D, 2.0D * (1.0D - (distance / (double) PERIMETER_SCORCH_RADIUS)));
                entity.setDeltaMovement(
                        (dx / len) * impulse,
                        Math.min(1.4D, (dy / len) * impulse + 0.5D),
                        (dz / len) * impulse
                );
                entity.hurtMarked = true;
            }
        }
    }

    private static void spawnPromptIonization(ServerLevel level, BlockPos origin) {
        double ox = origin.getX() + 0.5D;
        double oy = origin.getY() + 1.0D;
        double oz = origin.getZ() + 0.5D;

        level.sendParticles(ParticleTypes.FLASH, ox, oy, oz, 4, 1.0D, 1.0D, 1.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, ox, oy + 2.0D, oz, 35, 2.5D, 2.5D, 2.5D, 0.25D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, ox, oy + 1.0D, oz, 25, 2.0D, 1.5D, 2.0D, 0.12D);
        level.sendParticles(ParticleTypes.SONIC_BOOM, ox, oy, oz, 1, 0.0D, 0.0D, 0.0D, 0.0D);

        level.playSound(null, ox, oy, oz, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 4.0F, 1.9F);
        level.playSound(null, ox, oy, oz, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 3.5F, 0.7F);
    }

    private static void playBlastArrivalEffects(ServerLevel level, BlockPos origin, int craterRadius) {
        double ox = origin.getX() + 0.5D;
        double oy = origin.getY() + 1.0D;
        double oz = origin.getZ() + 0.5D;

        level.playSound(null, ox, oy, oz, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 8.0F, 0.75F);
        level.playSound(null, ox, oy, oz, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS, 7.0F, 0.80F);
        level.playSound(null, ox, oy, oz, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 6.0F, 0.85F);

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, ox, oy + 1.0D, oz, 10, 3.0D, 2.0D, 3.0D, 0.0D);
        level.sendParticles(ParticleTypes.LAVA, ox, oy + 1.5D, oz, 35, 4.0D, 2.5D, 4.0D, 0.0D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, ox, oy + 1.5D, oz, 45, 5.0D, 3.0D, 5.0D, 0.06D);
    }

    private static void spawnTacticalFallout(ServerLevel level, BlockPos origin) {
        double ox = origin.getX() + 0.5D;
        double oy = origin.getY() + 18.0D;
        double oz = origin.getZ() + 0.5D;

        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, ox, oy, oz, 2, 5.0D, 3.0D, 5.0D, 0.015D);
        level.sendParticles(ParticleTypes.SQUID_INK, ox, oy - 4.0D, oz, 2, 4.0D, 3.0D, 4.0D, 0.02D);
    }

    private static void maybePlaceBlastFire(ServerLevel level, BlockPos.MutableBlockPos mutPos, int x, int y, int z, double normalized) {
        if (normalized < 0.20D || normalized > 0.95D || noise(x, z, 211L) < 0.88D) {
            return;
        }

        mutPos.set(x, y, z);
        BlockState fire = Blocks.FIRE.defaultBlockState();
        if (level.getBlockState(mutPos).isAir() && fire.canSurvive(level, mutPos)) {
            level.setBlock(mutPos, fire, 2);
        }
    }

    private static double noise(int x, int z, long salt) {
        long value = (x * 341873128712L) ^ (z * 132897987541L) ^ salt;
        value = (value ^ (value >>> 33)) * 0xff51afd7ed558ccdL;
        value = (value ^ (value >>> 33)) * 0xc4ceb9fe1a85ec53L;
        value = value ^ (value >>> 33);
        return (double) (value & 0xFFFFFFL) / (double) 0x1000000L;
    }

    private static final class DetonationSimulation {
        final ServerLevel level;
        final BlockPos origin;
        int tick = 0;

        DetonationSimulation(ServerLevel level, BlockPos origin) {
            this.level = level;
            this.origin = origin;
        }
    }

    private static final class PendingDelayedDetonation {
        final ServerLevel level;
        final BlockPos origin;
        int delayTicks;

        PendingDelayedDetonation(ServerLevel level, BlockPos origin, int delayTicks) {
            this.level = level;
            this.origin = origin;
            this.delayTicks = delayTicks;
        }
    }
}
