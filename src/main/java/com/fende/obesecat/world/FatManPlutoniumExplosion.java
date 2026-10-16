package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModBlocks;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Standardized Fat Man Plutonium Implosion explosion engine.
 * Combines the visual/acoustic spectacle of the Big Gemini thermonuclear engine
 * (prompt flash, fireball, towering mushroom cloud, shockwave roar, and fallout)
 * with the organic 100m crater, clean sky clearance (zero floating leaves),
 * perimeter tree charcoal scorching, and central Trinitite crystallization.
 */
public final class FatManPlutoniumExplosion {
    public static final int CRATER_RADIUS = 100;
    public static final int CRATER_MAX_DEPTH = 50;
    public static final int PERIMETER_SCORCH_RADIUS = 145;
    public static final int SHOCKWAVE_DELAY_TICKS = 4;

    private static final List<DetonationSimulation> ACTIVE_DETONATIONS = new ArrayList<>();

    private FatManPlutoniumExplosion() {
    }

    public static void detonate(ServerLevel level, BlockPos origin) {
        BlockPos blastOrigin = origin.immutable();

        // 1. Trigger the Big Gemini visual and acoustic spectacle (fireball, mushroom cloud, flash, roar, fallout)
        BigGeminiThermonuclearExplosion.detonateSpectacle(level, blastOrigin);

        // 2. Queue the delayed crater excavation timed to shockwave arrival at tick 4
        ACTIVE_DETONATIONS.add(new DetonationSimulation(level, blastOrigin));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || ACTIVE_DETONATIONS.isEmpty()) {
            return;
        }

        Iterator<DetonationSimulation> iterator = ACTIVE_DETONATIONS.iterator();
        while (iterator.hasNext()) {
            DetonationSimulation sim = iterator.next();
            if (sim.level != level) {
                continue;
            }

            sim.tick++;
            if (sim.tick >= SHOCKWAVE_DELAY_TICKS) {
                executeCraterDetonation(sim.level, sim.origin);
                iterator.remove();
            }
        }
    }

    private static void executeCraterDetonation(ServerLevel level, BlockPos origin) {
        BlockPos craterCenter = carveCustomCrater(level, origin, CRATER_RADIUS, CRATER_MAX_DEPTH);
        scorchPerimeter(level, origin, CRATER_RADIUS, PERIMETER_SCORCH_RADIUS);
        placeTrinititeAtCraterCenter(level, craterCenter);
    }

    private static BlockPos carveCustomCrater(ServerLevel level, BlockPos origin, int craterRadius, int craterMaxDepth) {
        playCustomBlastEffects(level, origin, craterRadius);
        deleteMobsAtGroundZero(level, origin, craterRadius, craterMaxDepth);

        int centerX = origin.getX();
        int centerZ = origin.getZ();
        int minY = level.getMinBuildHeight() + 1;
        int rimPadding = Math.max(3, craterRadius / 7);
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

                int surfaceY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutPos.set(x, origin.getY(), z)).getY();
                int canopyY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, mutPos.set(x, origin.getY(), z)).getY();
                int maxDepth = (int) (craterMaxDepth * (1.0D - Math.pow(normalized, 1.8D)));
                maxDepth += (int) ((noise(x, z, 113L) - 0.5D) * Math.max(2.0D, craterMaxDepth * 0.24D));
                if (normalized > 0.72D) {
                    maxDepth = (int) (maxDepth * (0.5D + noise(x, z, 151L) * 0.45D));
                }
                if (maxDepth <= 0) {
                    continue;
                }

                // Erase from the true canopy top all the way down into the crater
                int topY = Math.max(surfaceY + (normalized < 0.28D ? 4 : 2), canopyY + 1);
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

                // Clean up any lingering leaves or foliage above topY so no leaves ever float
                for (int y = topY + 1; y <= topY + 16; y++) {
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
                if (distance <= innerRadius * 0.90D || distance > outerRadius) {
                    continue;
                }

                int x = centerX + dx;
                int z = centerZ + dz;

                if (!level.hasChunkAt(mutPos.set(x, origin.getY(), z))) {
                    continue;
                }

                // Check noise so perimeter edge is jagged and organic
                double edgeNoise = noise(x, z, 521L);
                double effectiveOuter = outerRadius - (edgeNoise * 8.0D);
                if (distance > effectiveOuter) {
                    continue;
                }

                int canopyY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, mutPos.set(x, origin.getY(), z)).getY();
                int groundY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutPos.set(x, origin.getY(), z)).getY();
                int startY = Math.max(canopyY, groundY);
                int scanLimit = Math.max(minY, Math.min(groundY - 3, startY - 35));

                // 1. Vaporize leaves and convert logs to charcoal from canopy down to ground
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
                        // Reached natural soil under the tree or on the surface
                        double scorchRoll = noise(x, z, 888L);
                        BlockState scorched = scorchRoll < 0.45D ? Blocks.COARSE_DIRT.defaultBlockState()
                                : scorchRoll < 0.72D ? Blocks.SMOOTH_BASALT.defaultBlockState()
                                : Blocks.GRAVEL.defaultBlockState();
                        level.setBlock(mutPos, scorched, 2);

                        // Scattered perimeter fire on top of scorched ground
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

    private static void placeTrinititeAtCraterCenter(ServerLevel level, BlockPos craterCenter) {
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

        for (Player player : level.getEntitiesOfClass(Player.class, blastBox)) {
            if (player.isCreative() || player.isSpectator()) {
                continue;
            }
            double dx = player.getX() - origin.getX();
            double dz = player.getZ() - origin.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance <= craterRadius) {
                float damage = (float) Math.max(15.0D, (1.0D - (distance / (double) craterRadius)) * 80.0D);
                player.hurt(level.damageSources().explosion(null, null), damage);
            }
        }
    }

    private static void playCustomBlastEffects(ServerLevel level, BlockPos origin, int craterRadius) {
        int emitterCount = Math.max(1, craterRadius / 25);
        int smokeCount = Math.max(35, craterRadius * 2);
        double smokeSpread = craterRadius * 0.8D;
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D, emitterCount, 5.0D, 1.5D, 5.0D, 0.0D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, origin.getX() + 0.5D, origin.getY() + 2.0D, origin.getZ() + 0.5D, smokeCount, smokeSpread, Math.max(6.0D, craterRadius * 0.18D), smokeSpread, 0.08D);
    }

    private static void maybePlaceBlastFire(ServerLevel level, BlockPos.MutableBlockPos mutPos, int x, int y, int z, double normalized) {
        if (normalized < 0.22D || normalized > 0.95D || noise(x, z, 211L) < 0.93D) {
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
}
