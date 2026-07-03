package com.fende.obesecat.world;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.entity.CowKing;
import com.fende.obesecat.registry.ModEntities;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CowKingFortSpawner {
    private static final Logger LOGGER = LoggerFactory.getLogger(CowKingFortSpawner.class);
    public static final String STRUCTURE_ENTITY_TAG = "obesecat_cow_king";
    public static final int TRIGGER_RANGE_BLOCKS = 32;
    public static final int COOLDOWN_TICKS = 20 * 60 * 10;
    private static final ResourceKey<Structure> FORT_STRUCTURE = ResourceKey.create(
            Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "cow_king_fort")
    );
    private static final Map<ServerLevel, FortRegistry> FORTS_BY_LEVEL = new WeakHashMap<>();

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }

        if (!CowLevelPortalManager.SECRET_COW_LEVEL.equals(level.dimension())) {
            return;
        }

        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Holder.Reference<Structure> fortHolder = registry.getHolder(FORT_STRUCTURE).orElse(null);
        if (fortHolder == null) {
            return;
        }

        for (StructureStart structureStart : chunk.getAllStarts().values()) {
            if (!structureStart.isValid() || structureStart.getStructure() != fortHolder.value()) {
                continue;
            }

            if (!structureStart.getChunkPos().equals(chunk.getPos())) {
                continue;
            }

            registerFort(level, AABB.of(structureStart.getBoundingBox()));
        }
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !CowLevelPortalManager.SECRET_COW_LEVEL.equals(level.dimension())) {
            return;
        }

        tick(level);
    }

    public static void registerFort(ServerLevel level, AABB bounds) {
        fortsFor(level).register(bounds);
    }

    public static void tick(ServerLevel level) {
        FortRegistry registry = FORTS_BY_LEVEL.get(level);
        if (registry == null) {
            return;
        }

        long gameTime = level.getGameTime();
        for (FortState fort : registry.forts.values()) {
            if (isCowKingActive(level, fort)) {
                continue;
            }

            if (gameTime - fort.lastSpawnGameTime < COOLDOWN_TICKS) {
                continue;
            }

            ServerPlayer nearbyPlayer = findNearbyPlayer(level, fort.bounds);
            if (nearbyPlayer == null) {
                continue;
            }

            spawnCowKing(level, fort, nearbyPlayer.position(), gameTime);
        }
    }

    public static void ensureCowKing(ServerLevel level, AABB bounds) {
        registerFort(level, bounds);
        FortState fort = fortsFor(level).get(bounds);
        if (fort == null) {
            return;
        }

        if (isCowKingActive(level, fort)) {
            return;
        }

        spawnCowKing(level, fort, null, level.getGameTime());
    }

    private static void spawnCowKing(ServerLevel level, FortState fort, Vec3 playerPosition, long gameTime) {
        CowKing cowKing = ModEntities.COW_KING.get().create(level);
        if (cowKing == null) {
            LOGGER.warn("Failed to create Cow King for fort at {}", fort.bounds.getCenter());
            return;
        }

        BlockPos spawnPos = findSpawnPos(level, fort.bounds, playerPosition);
        if (spawnPos == null) {
            LOGGER.warn("Failed to find a spawn position for Cow King fort at {}", fort.bounds.getCenter());
            return;
        }

        cowKing.moveTo(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                0.0F,
                0.0F
        );
        cowKing.setPersistenceRequired();
        cowKing.addTag(STRUCTURE_ENTITY_TAG);
        level.addFreshEntity(cowKing);
        fort.activeCowKingId = cowKing.getUUID();
        fort.lastSpawnGameTime = gameTime;
        LOGGER.info("Spawned Cow King outside Cow King Fort at {}", cowKing.blockPosition());
    }

    private static boolean isCowKingActive(ServerLevel level, FortState fort) {
        if (fort.activeCowKingId != null) {
            Entity active = level.getEntity(fort.activeCowKingId);
            if (active instanceof CowKing cowKing && cowKing.isAlive()) {
                cowKing.setPersistenceRequired();
                cowKing.addTag(STRUCTURE_ENTITY_TAG);
                return true;
            }
            fort.activeCowKingId = null;
        }

        List<CowKing> existing = level.getEntitiesOfClass(CowKing.class, fort.bounds.inflate(1.0D), cowKing -> true);
        if (existing.isEmpty()) {
            return false;
        }

        CowKing cowKing = existing.get(0);
        cowKing.setPersistenceRequired();
        cowKing.addTag(STRUCTURE_ENTITY_TAG);
        fort.activeCowKingId = cowKing.getUUID();
        return true;
    }

    private static ServerPlayer findNearbyPlayer(ServerLevel level, AABB bounds) {
        Vec3 center = bounds.getCenter();
        double triggerRangeSquared = TRIGGER_RANGE_BLOCKS * (double) TRIGGER_RANGE_BLOCKS;
        ServerPlayer closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;
        for (ServerPlayer player : level.players()) {
            double distance = player.distanceToSqr(center.x, center.y, center.z);
            if (distance <= triggerRangeSquared && distance < closestDistance) {
                closestDistance = distance;
                closestPlayer = player;
            }
        }

        return closestPlayer;
    }

    private static BlockPos findSpawnPos(ServerLevel level, AABB bounds, Vec3 playerPosition) {
        Vec3 center = bounds.getCenter();
        int x = Mth.floor(center.x);
        int z = Mth.floor(center.z);

        if (playerPosition != null) {
            double dx = playerPosition.x - center.x;
            double dz = playerPosition.z - center.z;
            if (Math.abs(dx) >= Math.abs(dz)) {
                x = dx >= 0.0D ? Mth.floor(bounds.maxX) + 1 : Mth.floor(bounds.minX) - 1;
                z = Mth.floor(center.z);
            } else {
                x = Mth.floor(center.x);
                z = dz >= 0.0D ? Mth.floor(bounds.maxZ) + 1 : Mth.floor(bounds.minZ) - 1;
            }
        } else {
            x = Mth.floor(bounds.maxX) + 1;
            z = Mth.floor(center.z);
        }

        int y = findStandingY(level, x, z);
        BlockPos candidate = new BlockPos(x, y, z);
        if (canStandAt(level, candidate)) {
            return candidate;
        }

        for (int offset = 1; offset <= 8; offset++) {
            BlockPos above = candidate.above(offset);
            if (canStandAt(level, above)) {
                return above;
            }

            BlockPos below = candidate.below(offset);
            if (canStandAt(level, below)) {
                return below;
            }
        }

        return null;
    }

    private static int findStandingY(ServerLevel level, double x, double z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(x), Mth.floor(z)) + 1;
    }

    private static boolean canStandAt(ServerLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos) || level.isOutsideBuildHeight(pos.above()) || level.isOutsideBuildHeight(pos.below())) {
            return false;
        }

        BlockState floorState = level.getBlockState(pos.below());
        if (!floorState.isFaceSturdy(level, pos.below(), Direction.UP) || floorState.is(BlockTags.FIRE)) {
            return false;
        }

        return isOpen(level, pos) && isOpen(level, pos.above());
    }

    private static boolean isOpen(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty();
    }

    private static FortRegistry fortsFor(ServerLevel level) {
        return FORTS_BY_LEVEL.computeIfAbsent(level, ignored -> new FortRegistry());
    }

    private CowKingFortSpawner() {
    }

    private static final class FortRegistry {
        private final Map<Long, FortState> forts = new HashMap<>();

        private void register(AABB bounds) {
            long key = keyFor(bounds);
            forts.putIfAbsent(key, new FortState(bounds));
        }

        private FortState get(AABB bounds) {
            return forts.get(keyFor(bounds));
        }

        private static long keyFor(AABB bounds) {
            BlockPos center = BlockPos.containing(bounds.getCenter());
            return new net.minecraft.world.level.ChunkPos(center).toLong();
        }
    }

    private static final class FortState {
        private final AABB bounds;
        private UUID activeCowKingId;
        private long lastSpawnGameTime;

        private FortState(AABB bounds) {
            this.bounds = bounds;
        }
    }
}
