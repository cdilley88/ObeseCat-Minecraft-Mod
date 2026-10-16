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
import org.jetbrains.annotations.Nullable;

public final class BigGeminiThermonuclearExplosion {
    public static final int CALDERA_RADIUS = 180;
    public static final int CALDERA_MAX_DEPTH = 100;
    public static final int RIM_SCORCH_RADIUS = 200;
    public static final int TOTAL_SIMULATION_TICKS = 400;
    public static final int SHOCKWAVE_START_TICK = 4;
    public static final int CALDERA_EXCAVATION_DURATION = 28;
    public static final int RIM_SCORCH_DURATION = 14;

    private static final double PACKET_DIST_RADIUS = 640.0D;
    private static final List<DetonationSimulation> ACTIVE_DETONATIONS = new ArrayList<>();

    private BigGeminiThermonuclearExplosion() {
    }

    public static void detonate(ServerLevel level, BlockPos origin, @Nullable Player initiator) {
        BlockPos blastOrigin = origin.immutable();

        // 1. Dispatch multi-phase thermonuclear client visual packet
        PacketDistributor.sendToPlayersNear(
                level,
                null,
                blastOrigin.getX() + 0.5D,
                blastOrigin.getY() + 0.5D,
                blastOrigin.getZ() + 0.5D,
                PACKET_DIST_RADIUS,
                new BigGeminiNuclearPayload(blastOrigin, TOTAL_SIMULATION_TICKS, 1.0F));

        // 2. High-energy atmospheric prompt flash sounds and ionization
        spawnPromptIonization(level, blastOrigin);

        // 3. Register live ticking atmospheric simulation
        ACTIVE_DETONATIONS.add(new DetonationSimulation(level, blastOrigin, initiator));
    }

    public static void detonateSpectacle(ServerLevel level, BlockPos origin) {
        BlockPos blastOrigin = origin.immutable();

        // 1. Dispatch multi-phase thermonuclear client visual packet
        PacketDistributor.sendToPlayersNear(
                level,
                null,
                blastOrigin.getX() + 0.5D,
                blastOrigin.getY() + 0.5D,
                blastOrigin.getZ() + 0.5D,
                PACKET_DIST_RADIUS,
                new BigGeminiNuclearPayload(blastOrigin, TOTAL_SIMULATION_TICKS, 1.0F));

        // 2. High-energy atmospheric prompt flash sounds and ionization
        spawnPromptIonization(level, blastOrigin);

        // 3. Register live ticking atmospheric spectacle simulation (visual & audio only, no caldera excavation)
        ACTIVE_DETONATIONS.add(new DetonationSimulation(level, blastOrigin, null, true));
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
            tickSimulation(sim);

            if (sim.tick >= TOTAL_SIMULATION_TICKS) {
                iterator.remove();
            }
        }
    }

    private static void tickSimulation(DetonationSimulation sim) {
        ServerLevel level = sim.level;
        BlockPos origin = sim.origin;
        int t = sim.tick;

        // --- 1. PROMPT ARRIVAL EFFECTS (Tick 4) ---
        if (t == SHOCKWAVE_START_TICK) {
            playPrimaryArrivalEffects(level, origin);
        }

        if (sim.spectacleOnly) {
            // Visual/audio spectacle only (used by Fat Man detonation)
            // Mushroom cloud, fireball, screen flash/shake, shockwave roar, and fallout all run.
            // Terrain deformation is handled by caller.
            if (t >= 45 && t <= 380 && t % 2 == 0) {
                spawnFallout(level, origin);
            }
            return;
        }

        // --- 2. SMOOTH CONTINUOUS CALDERA EXCAVATION (Ticks 4 to 32) ---
        if (t >= SHOCKWAVE_START_TICK && t < SHOCKWAVE_START_TICK + CALDERA_EXCAVATION_DURATION) {
            int shockTick = t - SHOCKWAVE_START_TICK;
            double prevProg = (double) shockTick / (double) CALDERA_EXCAVATION_DURATION;
            double nextProg = (double) (shockTick + 1) / (double) CALDERA_EXCAVATION_DURATION;
            double innerR = prevProg * CALDERA_RADIUS;
            double outerR = nextProg * CALDERA_RADIUS;

            carveCalderaRing(level, origin, innerR, outerR);
            applyThermonuclearKineticPhysics(level, origin, innerR, outerR);
        }

        // --- 3. LIGHTWEIGHT SURFACE RIM SCORCH (Ticks 32 to 46) ---
        int rimStartTick = SHOCKWAVE_START_TICK + CALDERA_EXCAVATION_DURATION;
        if (t >= rimStartTick && t < rimStartTick + RIM_SCORCH_DURATION) {
            int rimTick = t - rimStartTick;
            double prevProg = (double) rimTick / (double) RIM_SCORCH_DURATION;
            double nextProg = (double) (rimTick + 1) / (double) RIM_SCORCH_DURATION;
            double innerR = CALDERA_RADIUS + prevProg * (RIM_SCORCH_RADIUS - CALDERA_RADIUS);
            double outerR = CALDERA_RADIUS + nextProg * (RIM_SCORCH_RADIUS - CALDERA_RADIUS);

            scorchRimSurface(level, origin, innerR, outerR);
            applyThermonuclearKineticPhysics(level, origin, innerR, outerR);
        }

        // --- 4. RADIOACTIVE STRATOSPHERIC FALLOUT (Ticks 45 to 380) ---
        if (t >= 45 && t <= 380 && t % 2 == 0) {
            spawnFallout(level, origin);
        }
    }

    private static void spawnPromptIonization(ServerLevel level, BlockPos origin) {
        double ox = origin.getX() + 0.5D;
        double oy = origin.getY() + 1.0D;
        double oz = origin.getZ() + 0.5D;

        level.sendParticles(ParticleTypes.FLASH, ox, oy, oz, 8, 2.0D, 2.0D, 2.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, ox, oy + 4.0D, oz, 80, 5.0D, 5.0D, 5.0D, 0.35D);
        level.sendParticles(ParticleTypes.SONIC_BOOM, ox, oy, oz, 2, 0.0D, 0.0D, 0.0D, 0.0D);

        level.playSound(null, ox, oy, oz, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 6.0F, 1.9F);
        level.playSound(null, ox, oy, oz, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 5.0F, 0.5F);
    }

    private static void playPrimaryArrivalEffects(ServerLevel level, BlockPos origin) {
        double ox = origin.getX() + 0.5D;
        double oy = origin.getY() + 1.0D;
        double oz = origin.getZ() + 0.5D;

        level.playSound(null, ox, oy, oz, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 12.0F, 0.40F);
        level.playSound(null, ox, oy, oz, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS, 10.0F, 0.50F);
        level.playSound(null, ox, oy, oz, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 9.0F, 0.60F);

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, ox, oy + 2.0D, oz, 30, 10.0D, 5.0D, 10.0D, 0.0D);
        level.sendParticles(ParticleTypes.LAVA, ox, oy + 2.0D, oz, 80, 8.0D, 4.0D, 8.0D, 0.0D);
    }

    private static void carveCalderaRing(ServerLevel level, BlockPos origin, double innerR, double outerR) {
        int centerX = origin.getX();
        int centerZ = origin.getZ();
        int minY = level.getMinBuildHeight() + 1;

        int searchRadius = (int) Math.ceil(outerR);
        int innerFloor = (int) Math.floor(innerR);

        BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();

        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                double dist = Math.sqrt((double) dx * dx + (double) dz * dz);
                if (dist < innerFloor || dist > outerR) {
                    continue;
                }

                int x = centerX + dx;
                int z = centerZ + dz;

                if (!level.hasChunkAt(new BlockPos(x, origin.getY(), z))) {
                    continue;
                }

                double rimNoise = (noise(x, z, 211L) * 2.0D) - 1.0D;
                double effectiveCalderaR = CALDERA_RADIUS + (rimNoise * CALDERA_RADIUS * 0.10D);

                if (dist <= effectiveCalderaR) {
                    double norm = dist / effectiveCalderaR;
                    int surfaceY = level
                            .getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutPos.set(x, origin.getY(), z))
                            .getY();
                    int canopyY = level
                            .getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, mutPos.set(x, origin.getY(), z))
                            .getY();

                    // Multi-tiered stepped caldera profile: sheer walls down to deep abyssal lake
                    double depthCurve;
                    if (norm < 0.35D) {
                        depthCurve = 1.0D - (norm * 0.25D); // Deep abyssal floor
                    } else if (norm < 0.70D) {
                        depthCurve = 0.85D - Math.pow((norm - 0.35D) / 0.35D, 1.4D) * 0.45D; // Middle vitrified shelf
                    } else {
                        depthCurve = 0.40D * Math.pow(1.0D - (norm - 0.70D) / 0.30D, 1.8D); // Outer terrace
                    }

                    int baseDepth = (int) (CALDERA_MAX_DEPTH * depthCurve);
                    int depth = baseDepth + (int) ((noise(x, z, 777L) - 0.5D) * 5.0D);

                    if (depth > 0) {
                        int bottomY = Math.max(minY, surfaceY - depth);
                        int topY = Math.max(surfaceY + (norm < 0.20D ? 4 : 2), canopyY + 1);

                        // 1. Excavate column to air (erasing whole tree canopy)
                        for (int y = topY; y > bottomY; y--) {
                            mutPos.set(x, y, z);
                            BlockState state = level.getBlockState(mutPos);
                            if (!state.isAir() && state.getDestroySpeed(level, mutPos) >= 0.0F) {
                                level.setBlock(mutPos, Blocks.AIR.defaultBlockState(), 2);
                            }
                        }

                        // Clean up any lingering leaves or canopy foliage above topY
                        for (int y = topY + 1; y <= topY + 16; y++) {
                            mutPos.set(x, y, z);
                            BlockState state = level.getBlockState(mutPos);
                            if (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.is(BlockTags.FLOWERS) || state.is(Blocks.VINE)) {
                                level.setBlock(mutPos, Blocks.AIR.defaultBlockState(), 2);
                            }
                        }

                        // 2. Vitrify and metamorphose the caldera bed
                        vitrifyCalderaFloor(level, mutPos, x, bottomY, z, norm, centerX, centerZ);
                    }
                }
            }
        }
    }

    private static void vitrifyCalderaFloor(ServerLevel level, BlockPos.MutableBlockPos mutPos, int x, int bottomY,
            int z, double norm, int centerX, int centerZ) {
        mutPos.set(x, bottomY, z);
        double matNoise = noise(x, z, 555L);

        // Exactly two 8-block clusters of Trinitite concentrated on geothermal bedrock
        // fissures
        boolean isTrinititeCluster1 = Math.abs(x - centerX) <= 1 && Math.abs(z - centerZ) <= 1;
        boolean isTrinititeCluster2 = Math.abs(x - (centerX + 3)) <= 1 && Math.abs(z - (centerZ - 2)) <= 1
                && matNoise > 0.45D;

        BlockState glazedBlock;
        if (isTrinititeCluster1 || isTrinititeCluster2) {
            glazedBlock = ModBlocks.TRINITITE.get().defaultBlockState();
        } else if (norm < 0.28D) {
            // Abyssal Floor: Natural deeply scorched rock (Deepslate, Smooth Basalt, Tuff,
            // Cobbled Deepslate)
            if (matNoise < 0.35D) {
                glazedBlock = Blocks.DEEPSLATE.defaultBlockState();
            } else if (matNoise < 0.65D) {
                glazedBlock = Blocks.COBBLED_DEEPSLATE.defaultBlockState();
            } else if (matNoise < 0.85D) {
                glazedBlock = Blocks.SMOOTH_BASALT.defaultBlockState();
            } else {
                glazedBlock = Blocks.TUFF.defaultBlockState();
            }
        } else if (norm < 0.65D) {
            // Middle Caldera Shelf: Blackstone, Smooth Basalt, Basalt, Cobbled Deepslate
            if (matNoise < 0.35D) {
                glazedBlock = Blocks.BLACKSTONE.defaultBlockState();
            } else if (matNoise < 0.65D) {
                glazedBlock = Blocks.SMOOTH_BASALT.defaultBlockState();
            } else if (matNoise < 0.85D) {
                glazedBlock = Blocks.BASALT.defaultBlockState();
            } else {
                glazedBlock = Blocks.COBBLED_DEEPSLATE.defaultBlockState();
            }
        } else {
            // Terraced Upper Rim: Basalt, Cobbled Deepslate, Stone, Gravel
            if (matNoise < 0.35D) {
                glazedBlock = Blocks.BASALT.defaultBlockState();
            } else if (matNoise < 0.65D) {
                glazedBlock = Blocks.COBBLED_DEEPSLATE.defaultBlockState();
            } else if (matNoise < 0.85D) {
                glazedBlock = Blocks.STONE.defaultBlockState();
            } else {
                glazedBlock = Blocks.GRAVEL.defaultBlockState();
            }
        }

        level.setBlock(mutPos, glazedBlock, 2);

        // Sublayer solid natural bedrock seal
        mutPos.set(x, bottomY - 1, z);
        if (!level.getBlockState(mutPos).isAir()) {
            BlockState subLayer = norm < 0.50D ? Blocks.DEEPSLATE.defaultBlockState()
                    : Blocks.SMOOTH_BASALT.defaultBlockState();
            level.setBlock(mutPos, subLayer, 2);
        }

        // Random fire blocks scattered across the crater floor to light it up naturally
        if (!isTrinititeCluster1 && !isTrinititeCluster2 && noise(x, z, 827L) > 0.86D) {
            mutPos.set(x, bottomY + 1, z);
            if (level.getBlockState(mutPos).isAir() && Blocks.FIRE.defaultBlockState().canSurvive(level, mutPos)) {
                level.setBlock(mutPos, Blocks.FIRE.defaultBlockState(), 2);
            }
        }
    }

    private static void scorchRimSurface(ServerLevel level, BlockPos origin, double innerR, double outerR) {
        int centerX = origin.getX();
        int centerZ = origin.getZ();

        int searchRadius = (int) Math.ceil(outerR);
        int innerFloor = (int) Math.floor(innerR);

        BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();

        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                double dist = Math.sqrt((double) dx * dx + (double) dz * dz);
                if (dist < innerFloor || dist > outerR) {
                    continue;
                }

                int x = centerX + dx;
                int z = centerZ + dz;

                if (!level.hasChunkAt(new BlockPos(x, origin.getY(), z))) {
                    continue;
                }

                int canopyY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, mutPos.set(x, origin.getY(), z)).getY();
                int groundY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutPos.set(x, origin.getY(), z)).getY();
                int startY = Math.max(canopyY, groundY);
                int scanLimit = Math.max(level.getMinBuildHeight() + 1, Math.min(groundY - 3, startY - 35));

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
                        double sRoll = noise(x, z, 888L);
                        BlockState scorched = sRoll < 0.45D ? Blocks.COARSE_DIRT.defaultBlockState()
                                : sRoll < 0.75D ? Blocks.SMOOTH_BASALT.defaultBlockState()
                                        : Blocks.GRAVEL.defaultBlockState();
                        level.setBlock(mutPos, scorched, 2);

                        // Scattered perimeter fire on top of scorched ground
                        if (sRoll > 0.88D && dist < RIM_SCORCH_RADIUS * 0.95D) {
                            mutPos.set(x, y + 1, z);
                            if (level.getBlockState(mutPos).isAir()
                                    && Blocks.FIRE.defaultBlockState().canSurvive(level, mutPos)) {
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

    private static void applyThermonuclearKineticPhysics(ServerLevel level, BlockPos origin, double innerR,
            double outerR) {
        AABB box = new AABB(
                origin.getX() - outerR - 4.0D, origin.getY() - 30.0D, origin.getZ() - outerR - 4.0D,
                origin.getX() + outerR + 4.0D, origin.getY() + 70.0D, origin.getZ() + outerR + 4.0D);

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            double dx = entity.getX() - (origin.getX() + 0.5D);
            double dy = entity.getY() - (origin.getY() + 0.5D);
            double dz = entity.getZ() - (origin.getZ() + 0.5D);
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist >= innerR - 2.0D && dist <= outerR + 4.0D) {
                double norm = Math.max(0.1D, dist / RIM_SCORCH_RADIUS);
                float damage = (float) (140.0D / (norm * norm + 0.3D));
                entity.hurt(level.damageSources().explosion(null, null), damage);
                entity.setRemainingFireTicks(400);

                double impulse = Math.max(0.4D, 3.2D * (1.0D - norm));
                double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (len > 0.001D) {
                    entity.setDeltaMovement(
                            (dx / len) * impulse,
                            Math.min(2.5D, (dy / len) * impulse + 0.6D),
                            (dz / len) * impulse);
                    entity.hurtMarked = true;
                }
            }
        }
    }

    private static void spawnFallout(ServerLevel level, BlockPos origin) {
        double ox = origin.getX() + 0.5D;
        double oy = origin.getY() + 110.0D;
        double oz = origin.getZ() + 0.5D;

        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, ox, oy, oz, 4, 30.0D, 12.0D, 30.0D, 0.02D);
        level.sendParticles(ParticleTypes.SQUID_INK, ox, oy - 20.0D, oz, 5, 25.0D, 15.0D, 25.0D, 0.05D);
        level.sendParticles(ParticleTypes.LAVA, ox, oy - 40.0D, oz, 2, 18.0D, 8.0D, 18.0D, 0.0D);
    }

    private static double noise(int x, int z, long seed) {
        long h = seed + (long) x * 374761393L + (long) z * 668265263L;
        h = (h ^ (h >>> 13)) * 1274126177L;
        return (double) (h & 0x7FFFFFFFL) / (double) 0x7FFFFFFFL;
    }

    private static final class DetonationSimulation {
        final ServerLevel level;
        final BlockPos origin;
        @Nullable
        final Player initiator;
        final boolean spectacleOnly;
        int tick = 0;

        DetonationSimulation(ServerLevel level, BlockPos origin, @Nullable Player initiator) {
            this(level, origin, initiator, false);
        }

        DetonationSimulation(ServerLevel level, BlockPos origin, @Nullable Player initiator, boolean spectacleOnly) {
            this.level = level;
            this.origin = origin;
            this.initiator = initiator;
            this.spectacleOnly = spectacleOnly;
        }
    }
}
