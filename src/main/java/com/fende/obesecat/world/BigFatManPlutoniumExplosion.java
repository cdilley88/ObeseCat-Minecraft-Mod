package com.fende.obesecat.world;

import com.fende.obesecat.network.BigGeminiNuclearPayload;
import com.fende.obesecat.registry.ModBlocks;
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
 * Super-heavy BIG Fat Man Plutonium Implosion explosion engine.
 * Punches a massive 140m caldera well into deepslate (depth 95m)
 * while preserving a 20m safety buffer above bedrock, leaving a
 * gargantuan dark stone crater lined with vitrified Trinitite.
 */
public final class BigFatManPlutoniumExplosion {
    public static final int CRATER_RADIUS = 140;
    public static final int CRATER_MAX_DEPTH = 95;
    public static final int PERIMETER_SCORCH_RADIUS = 190;
    public static final int BEDROCK_SAFETY_BUFFER = 20;
    public static final int SHOCKWAVE_DELAY_TICKS = 4;
    public static final int TOTAL_SIMULATION_TICKS = 400;

    private static final double PACKET_DIST_RADIUS = 768.0D;
    private static final List<DetonationSimulation> ACTIVE_DETONATIONS = new ArrayList<>();
    private static final List<PendingDelayedDetonation> PENDING_DETONATIONS = new ArrayList<>();

    private BigFatManPlutoniumExplosion() {
    }

    public static void detonate(ServerLevel level, BlockPos origin) {
        BlockPos blastOrigin = origin.immutable();

        // 1. Dispatch multi-phase thermonuclear client visual packet (scale 1.35)
        PacketDistributor.sendToPlayersNear(
                level,
                null,
                blastOrigin.getX() + 0.5D,
                blastOrigin.getY() + 0.5D,
                blastOrigin.getZ() + 0.5D,
                PACKET_DIST_RADIUS,
                new BigGeminiNuclearPayload(blastOrigin, TOTAL_SIMULATION_TICKS, 1.35F));

        // 2. High-energy atmospheric prompt flash sounds and ionization
        BigGeminiThermonuclearExplosion.detonateSpectacle(level, blastOrigin);

        // 3. Queue delayed deepslate crater excavation timed to shockwave arrival at tick 4
        ACTIVE_DETONATIONS.add(new DetonationSimulation(level, blastOrigin));
    }

    public static void detonateDelayed(ServerLevel level, BlockPos origin, int delayTicks) {
        if (delayTicks <= 0) {
            detonate(level, origin);
            return;
        }
        PENDING_DETONATIONS.add(new PendingDelayedDetonation(level, origin.immutable(), delayTicks));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

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
            if (sim.tick >= SHOCKWAVE_DELAY_TICKS) {
                executeCraterDetonation(sim.level, sim.origin);
                iterator.remove();
            }
        }
    }

    private static void executeCraterDetonation(ServerLevel level, BlockPos origin) {
        BlockPos craterCenter = carveDeepslateCrater(level, origin, CRATER_RADIUS, CRATER_MAX_DEPTH);
        scorchPerimeter(level, origin, CRATER_RADIUS, PERIMETER_SCORCH_RADIUS);
        placeTrinititeBasinAtCraterCenter(level, craterCenter);
    }

    private static BlockPos carveDeepslateCrater(ServerLevel level, BlockPos origin, int craterRadius, int craterMaxDepth) {
        playCustomBlastEffects(level, origin, craterRadius);
        deleteMobsAtGroundZero(level, origin, craterRadius, craterMaxDepth);

        int centerX = origin.getX();
        int centerZ = origin.getZ();
        // Cut well into deepslate, but strictly stop above bedrock
        int minY = level.getMinBuildHeight() + BEDROCK_SAFETY_BUFFER;
        int rimPadding = Math.max(4, craterRadius / 8);
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
                double effectiveRadius = craterRadius + (rimNoise * craterRadius * 0.12D) + (pocketNoise * craterRadius * 0.05D);
                if (distance > effectiveRadius) {
                    continue;
                }

                double normalized = distance / effectiveRadius;
                if (normalized > 0.93D && noise(x, z, 79L) > 0.58D) {
                    continue;
                }

                int surfaceY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutPos.set(x, origin.getY(), z)).getY();
                int canopyY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, mutPos.set(x, origin.getY(), z)).getY();
                int maxDepth = (int) (craterMaxDepth * (1.0D - Math.pow(normalized, 1.7D)));
                maxDepth += (int) ((noise(x, z, 117L) - 0.5D) * Math.max(3.0D, craterMaxDepth * 0.22D));
                if (normalized > 0.70D) {
                    maxDepth = (int) (maxDepth * (0.5D + noise(x, z, 157L) * 0.45D));
                }
                if (maxDepth <= 0) {
                    continue;
                }

                int topY = Math.max(surfaceY + (normalized < 0.25D ? 5 : 2), canopyY + 1);
                // Guaranteed safety buffer above bedrock while cutting deep into deepslate
                int bottomY = Math.max(minY, surfaceY - maxDepth);
                if (dx == 0 && dz == 0) {
                    craterCenter = new BlockPos(x, bottomY, z);
                }

                for (int y = topY; y >= bottomY; y--) {
                    mutPos.set(x, y, z);
                    BlockState state = level.getBlockState(mutPos);
                    if (!state.isAir() && state.getDestroySpeed(level, mutPos) >= 0.0F && !state.is(Blocks.BEDROCK)) {
                        level.setBlock(mutPos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }

                // Clean up any lingering leaves/foliage above topY
                for (int y = topY + 1; y <= topY + 20; y++) {
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

                double edgeNoise = noise(x, z, 613L);
                double effectiveOuter = outerRadius - (edgeNoise * 10.0D);
                if (distance > effectiveOuter) {
                    continue;
                }

                int canopyY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, mutPos.set(x, origin.getY(), z)).getY();
                int groundY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutPos.set(x, origin.getY(), z)).getY();
                int startY = Math.max(canopyY, groundY);
                int scanLimit = Math.max(minY, Math.min(groundY - 4, startY - 40));

                for (int y = startY; y >= scanLimit; y--) {
                    mutPos.set(x, y, z);
                    BlockState state = level.getBlockState(mutPos);
                    if (state.isAir()) continue;

                    if (state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS) || state.is(BlockTags.CROPS)
                            || state.is(BlockTags.SAPLINGS) || state.is(Blocks.VINE)) {
                        level.setBlock(mutPos, Blocks.AIR.defaultBlockState(), 2);
                    } else if (state.is(BlockTags.LOGS)) {
                        level.setBlock(mutPos, Blocks.COAL_BLOCK.defaultBlockState(), 2);
                    } else if (state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK)) {
                        double scorchRoll = noise(x, z, 773L);
                        BlockState scorched = scorchRoll < 0.35D ? Blocks.COARSE_DIRT.defaultBlockState()
                                : scorchRoll < 0.60D ? Blocks.BASALT.defaultBlockState()
                                : scorchRoll < 0.80D ? Blocks.SMOOTH_BASALT.defaultBlockState()
                                : Blocks.OBSIDIAN.defaultBlockState();
                        level.setBlock(mutPos, scorched, 2);

                        if (scorchRoll > 0.85D && distance < outerRadius * 0.92D) {
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

    private static void placeTrinititeBasinAtCraterCenter(ServerLevel level, BlockPos craterCenter) {
        BlockState trinitite = ModBlocks.TRINITITE.get().defaultBlockState();
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

        // Place a wide vitrified impact basin of Trinitite across the deepslate crater floor
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue; // Rounded corners

                int px = craterCenter.getX() + dx;
                int pz = craterCenter.getZ() + dz;

                for (int dy = -1; dy <= 4; dy++) {
                    mut.set(px, craterCenter.getY() + dy, pz);
                    BlockState current = level.getBlockState(mut);
                    BlockState below = level.getBlockState(mut.below());

                    if ((current.isAir() || current.canBeReplaced()) && !below.isAir() && below.getDestroySpeed(level, mut.below()) >= 0.0F) {
                        level.setBlock(mut.below(), trinitite, 3);
                        break;
                    }
                }
            }
        }
    }

    private static void deleteMobsAtGroundZero(ServerLevel level, BlockPos origin, int craterRadius, int craterMaxDepth) {
        AABB blastBox = new AABB(
                origin.getX() - craterRadius - 4.0D,
                origin.getY() - craterMaxDepth - 6.0D,
                origin.getZ() - craterRadius - 4.0D,
                origin.getX() + craterRadius + 4.0D,
                origin.getY() + Math.max(16.0D, craterRadius * 0.45D),
                origin.getZ() + craterRadius + 4.0D
        );

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, blastBox, entity -> !(entity instanceof Player))) {
            double dx = entity.getX() - origin.getX();
            double dz = entity.getZ() - origin.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            double effectiveRadius = craterRadius + ((noise(entity.blockPosition().getX(), entity.blockPosition().getZ(), 337L) * 2.0D) - 1.0D) * craterRadius * 0.16D;
            if (distance <= effectiveRadius) {
                entity.discard();
            }
        }

        for (Player player : level.getEntitiesOfClass(Player.class, blastBox)) {
            if (player.isCreative() || player.isSpectator()) continue;
            double dx = player.getX() - origin.getX();
            double dz = player.getZ() - origin.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance <= craterRadius) {
                float damage = (float) Math.max(25.0D, (1.0D - (distance / (double) craterRadius)) * 120.0D);
                player.hurt(level.damageSources().explosion(null, null), damage);
            }
        }
    }

    private static void playCustomBlastEffects(ServerLevel level, BlockPos origin, int craterRadius) {
        int emitterCount = Math.max(2, craterRadius / 20);
        int smokeCount = Math.max(50, craterRadius * 3);
        double smokeSpread = craterRadius * 0.85D;
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D, emitterCount, 7.0D, 2.0D, 7.0D, 0.0D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, origin.getX() + 0.5D, origin.getY() + 2.0D, origin.getZ() + 0.5D, smokeCount, smokeSpread, Math.max(8.0D, craterRadius * 0.22D), smokeSpread, 0.09D);
    }

    private static void maybePlaceBlastFire(ServerLevel level, BlockPos.MutableBlockPos mutPos, int x, int y, int z, double normalized) {
        if (normalized < 0.18D || normalized > 0.96D || noise(x, z, 227L) < 0.91D) {
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
