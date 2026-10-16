package com.fende.obesecat.world;

import com.fende.obesecat.network.GeminiNuclearPayload;
import com.fende.obesecat.registry.ModBlocks;
import com.fende.obesecat.registry.ModSounds;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class GeminiThermonuclearExplosion {
    public static final int CRATER_RADIUS = 60;
    public static final int CRATER_MAX_DEPTH = 34;
    public static final int THERMAL_RADIUS = 120;
    public static final int TOTAL_SIMULATION_TICKS = 500;
    public static final int SHOCKWAVE_START_TICK = 12;
    public static final int SHOCKWAVE_DURATION_TICKS = 26;
    public static final int APEX_HEIGHT = 100;
    private static final double PACKET_DIST_RADIUS = 512.0D;

    private static final List<DetonationSimulation> ACTIVE_DETONATIONS = new ArrayList<>();

    private GeminiThermonuclearExplosion() {
    }

    public static void detonate(ServerLevel level, BlockPos origin, @Nullable Player initiator) {
        BlockPos blastOrigin = origin.immutable();

        // 1. Dispatch multi-phase client visual packet
        PacketDistributor.sendToPlayersNear(
                level,
                null,
                blastOrigin.getX() + 0.5D,
                blastOrigin.getY() + 0.5D,
                blastOrigin.getZ() + 0.5D,
                PACKET_DIST_RADIUS,
                new GeminiNuclearPayload(blastOrigin, TOTAL_SIMULATION_TICKS, 1.0F)
        );

        // 2. Prompt atmospheric ionization flash effects at Ground Zero
        spawnPromptIonization(level, blastOrigin);

        // 3. Register live ticking atmospheric simulation
        ACTIVE_DETONATIONS.add(new DetonationSimulation(level, blastOrigin, initiator));
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

        // --- PHASE 1: Supersonic Shockwave Arrival & Sonic Detonation (Tick 12) ---
        if (t == SHOCKWAVE_START_TICK) {
            playShockwaveArrivalEffects(level, origin);
        }

        // --- PHASE 2: Expanding Supersonic Excavation & Thermal Vitrification (Ticks 12 to 38) ---
        if (t >= SHOCKWAVE_START_TICK && t < SHOCKWAVE_START_TICK + SHOCKWAVE_DURATION_TICKS) {
            int shockTick = t - SHOCKWAVE_START_TICK;
            double prevProg = (double) shockTick / (double) SHOCKWAVE_DURATION_TICKS;
            double nextProg = (double) (shockTick + 1) / (double) SHOCKWAVE_DURATION_TICKS;
            double innerR = prevProg * THERMAL_RADIUS;
            double outerR = nextProg * THERMAL_RADIUS;

            carveShockwaveRing(level, origin, innerR, outerR);
            applyShockwaveEntityPhysics(level, origin, innerR, outerR);
        }

        // --- PHASE 3: Stratospheric Mushroom Cloud Vortex (Ticks 12 to 420) ---
        if (t >= SHOCKWAVE_START_TICK && t <= 420) {
            renderMushroomCloud(level, origin, t);
        }

        // --- PHASE 4: Radioactive Ash Fallout / Black Rain (Ticks 45 to 480) ---
        if (t >= 45 && t <= 480 && t % 2 == 0) {
            spawnAtmosphericFallout(level, origin);
        }
    }

    private static void spawnPromptIonization(ServerLevel level, BlockPos origin) {
        double ox = origin.getX() + 0.5D;
        double oy = origin.getY() + 1.0D;
        double oz = origin.getZ() + 0.5D;

        level.sendParticles(ParticleTypes.FLASH, ox, oy, oz, 4, 1.0D, 1.0D, 1.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, ox, oy + 2.0D, oz, 45, 3.5D, 3.5D, 3.5D, 0.25D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, ox, oy + 1.0D, oz, 35, 4.0D, 2.0D, 4.0D, 0.15D);
        level.sendParticles(ParticleTypes.SONIC_BOOM, ox, oy, oz, 1, 0.0D, 0.0D, 0.0D, 0.0D);

        level.playSound(null, ox, oy, oz, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 4.0F, 1.8F);
    }

    private static void playShockwaveArrivalEffects(ServerLevel level, BlockPos origin) {
        double ox = origin.getX() + 0.5D;
        double oy = origin.getY() + 1.0D;
        double oz = origin.getZ() + 0.5D;

        level.playSound(null, ox, oy, oz, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 10.0F, 0.45F);
        level.playSound(null, ox, oy, oz, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS, 8.0F, 0.60F);
        level.playSound(null, ox, oy, oz, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 7.0F, 0.70F);
        level.playSound(null, ox, oy, oz, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.BLOCKS, 6.0F, 0.50F);

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, ox, oy + 1.0D, oz, 15, 6.0D, 3.0D, 6.0D, 0.0D);
        level.sendParticles(ParticleTypes.DRAGON_BREATH, ox, oy + 2.0D, oz, 80, 8.0D, 4.0D, 8.0D, 0.12D);
        level.sendParticles(ParticleTypes.LAVA, ox, oy + 1.0D, oz, 50, 6.0D, 2.0D, 6.0D, 0.0D);
    }

    private static void carveShockwaveRing(ServerLevel level, BlockPos origin, double innerR, double outerR) {
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

                // Ensure chunk is loaded to prevent lag
                if (!level.hasChunkAt(new BlockPos(x, origin.getY(), z))) {
                    continue;
                }

                double rimNoise = (noise(x, z, 137L) * 2.0D) - 1.0D;
                double effectiveCraterR = CRATER_RADIUS + (rimNoise * CRATER_RADIUS * 0.12D);

                if (dist <= effectiveCraterR) {
                    // === EXCAVATION & VITRIFICATION ZONE ===
                    double norm = dist / effectiveCraterR;
                    int surfaceY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutPos.set(x, origin.getY(), z)).getY();

                    int baseDepth = (int) (CRATER_MAX_DEPTH * (1.0D - Math.pow(norm, 1.82D)));
                    int depth = baseDepth + (int) ((noise(x, z, 911L) - 0.5D) * 4.5D);
                    if (norm > 0.75D) {
                        depth = (int) (depth * (0.6D + noise(x, z, 313L) * 0.4D));
                    }

                    if (depth > 0) {
                        int bottomY = Math.max(minY, surfaceY - depth);
                        int topY = surfaceY + (norm < 0.25D ? 3 : 1);

                        // 1. Excavate column to air
                        for (int y = topY; y > bottomY; y--) {
                            mutPos.set(x, y, z);
                            BlockState state = level.getBlockState(mutPos);
                            if (!state.isAir() && state.getDestroySpeed(level, mutPos) >= 0.0F) {
                                level.setBlock(mutPos, Blocks.AIR.defaultBlockState(), 2);
                            }
                        }

                        // 2. Thermally metamorphose the crater floor (Vitrified Bedrock Glaze)
                        vitrifyCraterFloor(level, mutPos, x, bottomY, z, norm, centerX, centerZ);
                    }
                } else if (dist <= effectiveCraterR * 1.14D) {
                    // === UPHEAVED EJECTA RIM ===
                    double ejectaNorm = (dist - effectiveCraterR) / (effectiveCraterR * 0.14D);
                    double ridgeStrength = Math.sin(ejectaNorm * Math.PI);
                    if (ridgeStrength > 0.15D && noise(x, z, 719L) > 0.35D) {
                        int ejectaHeight = (int) Math.round(ridgeStrength * 3.2D + (noise(x, z, 521L) - 0.5D) * 1.8D);
                        if (ejectaHeight > 0) {
                            placeUpheavedEjecta(level, mutPos, x, z, ejectaHeight);
                        }
                    }
                } else if (dist <= THERMAL_RADIUS) {
                    // === THERMAL FLASH INCINERATION ZONE ===
                    incinerateThermalPerimeter(level, mutPos, x, z, dist);
                }
            }
        }
    }

    private static void vitrifyCraterFloor(ServerLevel level, BlockPos.MutableBlockPos mutPos, int x, int bottomY, int z, double norm, int centerX, int centerZ) {
        mutPos.set(x, bottomY, z);
        double matNoise = noise(x, z, 401L);

        BlockState glazedBlock;
        // Exactly 4 blocks of Trinitite produced at the center epicenter of the crater
        boolean isCenterTrinitite = (x == centerX || x == centerX + 1) && (z == centerZ || z == centerZ + 1);
        if (isCenterTrinitite) {
            glazedBlock = ModBlocks.TRINITITE.get().defaultBlockState();
        } else if (norm < 0.22D) {
            // Ground Zero: Deeply scorched natural bedrock (Deepslate, Smooth Basalt, Tuff, Cobbled Deepslate)
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
            // Inner Basin: Blackstone, Smooth Basalt, Basalt, Cobbled Deepslate
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
            // Outer Basin & Slopes: Basalt, Cobbled Deepslate, Stone, Gravel
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

        // Solidify the sub-layer beneath with natural rock
        mutPos.set(x, bottomY - 1, z);
        if (!level.getBlockState(mutPos).isAir()) {
            BlockState subLayer = norm < 0.50D ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.SMOOTH_BASALT.defaultBlockState();
            level.setBlock(mutPos, subLayer, 2);
        }

        // Random blast fire scattered across the crater floor to light it up naturally
        if (!isCenterTrinitite && noise(x, z, 827L) > 0.86D) {
            mutPos.set(x, bottomY + 1, z);
            if (level.getBlockState(mutPos).isAir() && Blocks.FIRE.defaultBlockState().canSurvive(level, mutPos)) {
                level.setBlock(mutPos, Blocks.FIRE.defaultBlockState(), 2);
            }
        }
    }

    private static void placeUpheavedEjecta(ServerLevel level, BlockPos.MutableBlockPos mutPos, int x, int z, int height) {
        int surfaceY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutPos.set(x, 100, z)).getY();

        for (int h = 1; h <= height; h++) {
            mutPos.set(x, surfaceY + h, z);
            double mat = noise(x + h, z, 941L);
            BlockState ejecta = mat < 0.35D ? Blocks.COBBLESTONE.defaultBlockState()
                    : mat < 0.65D ? Blocks.BASALT.defaultBlockState()
                    : mat < 0.85D ? Blocks.COARSE_DIRT.defaultBlockState()
                    : Blocks.GRAVEL.defaultBlockState();
            level.setBlock(mutPos, ejecta, 2);
        }
    }

    private static void incinerateThermalPerimeter(ServerLevel level, BlockPos.MutableBlockPos mutPos, int x, int z, double dist) {
        int surfaceY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, mutPos.set(x, 100, z)).getY();

        // 1. Vaporize foliage & trees in the upper canopy
        for (int y = surfaceY; y >= surfaceY - 10; y--) {
            mutPos.set(x, y, z);
            BlockState state = level.getBlockState(mutPos);
            if (state.isAir()) {
                continue;
            }

            // Vaporize leaves, flowers, grass, crops into air
            if (state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS) || state.is(BlockTags.CROPS) || state.is(BlockTags.SAPLINGS)) {
                level.setBlock(mutPos, Blocks.AIR.defaultBlockState(), 2);
            } else if (state.is(BlockTags.LOGS)) {
                // All standing tree logs turn directly into pure charcoal blocks
                level.setBlock(mutPos, Blocks.COAL_BLOCK.defaultBlockState(), 2);
            } else if (state.is(Blocks.WATER)) {
                // Flash-evaporate water into steam
                level.setBlock(mutPos, Blocks.AIR.defaultBlockState(), 2);
                level.sendParticles(ParticleTypes.CLOUD, x + 0.5D, y + 1.0D, z + 0.5D, 2, 0.4D, 0.8D, 0.4D, 0.05D);
            } else if (state.is(Blocks.ICE) || state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK)) {
                level.setBlock(mutPos, Blocks.AIR.defaultBlockState(), 2);
            }
        }

        // 2. Scorch the top soil layer
        int groundY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutPos.set(x, 100, z)).getY();
        mutPos.set(x, groundY, z);
        BlockState ground = level.getBlockState(mutPos);
        if (ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.MOSS_BLOCK) || ground.is(Blocks.DIRT)) {
            double scorchRoll = noise(x, z, 617L);
            BlockState scorched = scorchRoll < 0.50D ? Blocks.COARSE_DIRT.defaultBlockState()
                    : scorchRoll < 0.75D ? Blocks.GRAVEL.defaultBlockState()
                    : scorchRoll < 0.90D ? Blocks.GRAVEL.defaultBlockState()
                    : Blocks.BASALT.defaultBlockState();
            level.setBlock(mutPos, scorched, 2);

            // Scattered perimeter fire
            if (scorchRoll > 0.94D && dist < THERMAL_RADIUS * 0.9D) {
                mutPos.set(x, groundY + 1, z);
                if (level.getBlockState(mutPos).isAir() && Blocks.FIRE.defaultBlockState().canSurvive(level, mutPos)) {
                    level.setBlock(mutPos, Blocks.FIRE.defaultBlockState(), 2);
                }
            }
        }
    }

    private static void applyShockwaveEntityPhysics(ServerLevel level, BlockPos origin, double innerR, double outerR) {
        AABB box = new AABB(
                origin.getX() - outerR - 2.0D, origin.getY() - 20.0D, origin.getZ() - outerR - 2.0D,
                origin.getX() + outerR + 2.0D, origin.getY() + 50.0D, origin.getZ() + outerR + 2.0D
        );

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            double dx = entity.getX() - (origin.getX() + 0.5D);
            double dy = entity.getY() - (origin.getY() + 0.5D);
            double dz = entity.getZ() - (origin.getZ() + 0.5D);
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist < innerR || dist > outerR) {
                continue;
            }

            // Close proximity: total disintegration
            if (dist < CRATER_RADIUS * 0.35D) {
                if (!(entity instanceof Player player && player.isCreative())) {
                    entity.discard();
                    continue;
                }
            }

            // Supersonic blast wave impulse
            double invDist = dist > 0.001D ? 1.0D / dist : 1.0D;
            double normDx = dx * invDist;
            double normDz = dz * invDist;

            double impulseStrength = (1.0D - (dist / THERMAL_RADIUS)) * 3.4D;
            if (impulseStrength > 0.1D) {
                entity.setDeltaMovement(
                        entity.getDeltaMovement().add(normDx * impulseStrength, 0.75D + impulseStrength * 0.45D, normDz * impulseStrength)
                );
                entity.hurt(level.damageSources().explosion(null, null), (float) (impulseStrength * 36.0F));
                entity.igniteForSeconds((int) (impulseStrength * 12.0F));
            }
        }
    }

    private static void renderMushroomCloud(ServerLevel level, BlockPos origin, int tick) {
        double ox = origin.getX() + 0.5D;
        double oy = origin.getY();
        double oz = origin.getZ() + 0.5D;

        // 1. Rising Stem Growth
        double stemClimb = Math.min((double) APEX_HEIGHT, (tick - SHOCKWAVE_START_TICK) * 2.4D);

        for (double y = 2.0D; y < stemClimb; y += 4.0D) {
            double heightFrac = y / APEX_HEIGHT;
            double stemR = 2.2D + 1.8D * Math.sin(heightFrac * Math.PI);
            double angle = (tick * 0.18D) + (y * 0.12D);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            // Core fiery updraft
            level.sendParticles(ParticleTypes.FLAME, ox + cos * (stemR * 0.35D), oy + y, oz + sin * (stemR * 0.35D), 2, 0.2D, 0.4D, 0.2D, 0.04D);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, ox - sin * (stemR * 0.3D), oy + y, oz + cos * (stemR * 0.3D), 1, 0.15D, 0.3D, 0.15D, 0.02D);
            if (tick % 3 == 0) {
                level.sendParticles(ParticleTypes.LAVA, ox, oy + y, oz, 1, 0.2D, 0.3D, 0.2D, 0.0D);
            }

            // Churning smoke sheath
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, ox + cos * stemR, oy + y, oz + sin * stemR, 2, 0.4D, 0.3D, 0.4D, 0.015D);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, ox - cos * stemR, oy + y, oz - sin * stemR, 2, 0.5D, 0.3D, 0.5D, 0.02D);
            level.sendParticles(ParticleTypes.SQUID_INK, ox + sin * stemR, oy + y, oz - cos * stemR, 1, 0.3D, 0.2D, 0.3D, 0.01D);
        }

        // 2. Rolling Toroidal Mushroom Cap (The Donut)
        if (tick >= 24) {
            double capProgress = Math.min(1.0D, (tick - 24) / 100.0D);
            double majorR = 12.0D + capProgress * 26.0D; // Expands outward up to 38 blocks
            double minorR = 6.0D + capProgress * 4.5D;
            double capCenterY = oy + APEX_HEIGHT - 12.0D;

            int ringSteps = 16;
            for (int i = 0; i < ringSteps; i++) {
                double theta = (i * (Math.PI * 2.0D) / ringSteps) + (tick * 0.03D);
                double rollingPhi = (tick * 0.14D) + (i * 0.4D);

                double r = majorR + minorR * Math.cos(rollingPhi);
                double py = capCenterY + minorR * Math.sin(rollingPhi);
                double px = ox + r * Math.cos(theta);
                double pz = oz + r * Math.sin(theta);

                // Dense stratospheric billows
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz, 2, 1.2D, 0.8D, 1.2D, 0.02D);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, px, py, pz, 2, 1.0D, 0.7D, 1.0D, 0.025D);

                // Glowing inner core of cap
                if (i % 2 == 0) {
                    level.sendParticles(ParticleTypes.DRAGON_BREATH, px * 0.85D + ox * 0.15D, py, pz * 0.85D + oz * 0.15D, 1, 0.6D, 0.5D, 0.6D, 0.01D);
                    level.sendParticles(ParticleTypes.FLAME, px * 0.75D + ox * 0.25D, py, pz * 0.75D + oz * 0.25D, 1, 0.4D, 0.3D, 0.4D, 0.02D);
                }
            }
        }

        // 3. Wilson Condensation Rings (Supersonic Shock Halo)
        if (tick == 20 || tick == 32 || tick == 46) {
            double haloY = oy + 32.0D + (tick - 20) * 1.2D;
            double haloR = 15.0D + (tick - 20) * 1.5D;
            for (int i = 0; i < 28; i++) {
                double angle = i * (Math.PI * 2.0D) / 28.0D;
                double hx = ox + haloR * Math.cos(angle);
                double hz = oz + haloR * Math.sin(angle);
                level.sendParticles(ParticleTypes.CLOUD, hx, haloY, hz, 2, 0.8D, 0.3D, 0.8D, 0.08D);
                level.sendParticles(ParticleTypes.END_ROD, hx, haloY, hz, 1, 0.5D, 0.2D, 0.5D, 0.05D);
            }
        }

        // 4. Base Surge Ground Discs (Dust wave rushing outward)
        if (tick >= 14 && tick <= 40) {
            double surgeR = (tick - 14) * 2.4D;
            for (int i = 0; i < 18; i++) {
                double angle = i * (Math.PI * 2.0D) / 18.0D;
                double sx = ox + surgeR * Math.cos(angle);
                double sz = oz + surgeR * Math.sin(angle);
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, sx, oy + 1.0D, sz, 1, 1.2D, 0.4D, 1.2D, 0.03D);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, sx, oy + 1.5D, sz, 1, 0.8D, 0.3D, 0.8D, 0.04D);
            }
        }
    }

    private static void spawnAtmosphericFallout(ServerLevel level, BlockPos origin) {
        double ox = origin.getX() + 0.5D;
        double oy = origin.getY() + 35.0D;
        double oz = origin.getZ() + 0.5D;

        level.sendParticles(ParticleTypes.ASH, ox, oy, oz, 55, 38.0D, 14.0D, 38.0D, 0.015D);
        level.sendParticles(ParticleTypes.WHITE_ASH, ox, oy, oz, 35, 34.0D, 12.0D, 34.0D, 0.012D);
        level.sendParticles(ParticleTypes.FALLING_OBSIDIAN_TEAR, ox, oy + 8.0D, oz, 12, 28.0D, 6.0D, 28.0D, 0.0D);
    }

    private static double noise(int x, int z, long salt) {
        long value = (x * 341873128712L) ^ (z * 132897987541L) ^ salt;
        value = (value ^ (value >>> 33)) * 0xff51afd7ed558ccdL;
        value = (value ^ (value >>> 33)) * 0xc4ceb9fe1a85ec53L;
        value = value ^ (value >>> 33);
        return (double) (value & 0xFFFFFFL) / (double) 0x1000000L;
    }

    private static final class DetonationSimulation {
        private final ServerLevel level;
        private final BlockPos origin;
        @Nullable
        private final Player initiator;
        private int tick = 0;

        private DetonationSimulation(ServerLevel level, BlockPos origin, @Nullable Player initiator) {
            this.level = level;
            this.origin = origin;
            this.initiator = initiator;
        }
    }
}
