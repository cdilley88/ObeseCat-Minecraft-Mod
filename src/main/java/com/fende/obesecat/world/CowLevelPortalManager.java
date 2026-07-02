package com.fende.obesecat.world;

import com.fende.obesecat.ObeseCatMod;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

public final class CowLevelPortalManager {
    public static final ResourceKey<Level> OVERWORLD = Level.OVERWORLD;
    public static final ResourceKey<Level> SECRET_COW_LEVEL = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "secret_cow_level")
    );

    private CowLevelPortalManager() {
    }

    public static void teleportToSecretCowLevel(ServerPlayer player) {
        ServerLevel targetLevel = player.server.getLevel(SECRET_COW_LEVEL);
        if (targetLevel == null) {
            return;
        }

        BlockPos targetPos = findTopSafeStandingPosition(targetLevel, 0, 0);
        if (targetPos == null) {
            return;
        }
        teleport(player, targetLevel, targetPos);
    }

    public static void returnFromSecretCowLevel(ServerPlayer player) {
        ServerLevel overworld = player.server.getLevel(OVERWORLD);
        if (overworld == null) {
            return;
        }

        DimensionTransition personalRespawn = findPersonalOverworldRespawn(player);
        if (personalRespawn != null) {
            Vec3 destination = personalRespawn.pos();
            player.teleportTo(personalRespawn.newLevel(), destination.x, destination.y, destination.z, player.getYRot(), player.getXRot());
            return;
        }

        BlockPos sharedSpawn = findSharedSpawnStandingPosition(overworld);
        if (sharedSpawn == null) {
            return;
        }
        teleport(player, overworld, sharedSpawn);
    }

    private static void teleport(ServerPlayer player, ServerLevel targetLevel, BlockPos targetPos) {
        Vec3 destination = Vec3.atBottomCenterOf(targetPos);
        player.teleportTo(targetLevel, destination.x, destination.y, destination.z, player.getYRot(), player.getXRot());
    }

    private static DimensionTransition findPersonalOverworldRespawn(ServerPlayer player) {
        if (player.getRespawnPosition() == null || !OVERWORLD.equals(player.getRespawnDimension())) {
            return null;
        }

        DimensionTransition transition = player.findRespawnPositionAndUseSpawnBlock(false, DimensionTransition.DO_NOTHING);
        if (transition == null || !OVERWORLD.equals(transition.newLevel().dimension())) {
            return null;
        }

        return transition;
    }

    @Nullable
    private static BlockPos findSharedSpawnStandingPosition(ServerLevel level) {
        BlockPos sharedSpawn = level.getSharedSpawnPos();
        if (canStandAt(level, sharedSpawn)) {
            return sharedSpawn;
        }

        return findTopSafeStandingPosition(level, sharedSpawn.getX(), sharedSpawn.getZ());
    }

    @Nullable
    private static BlockPos findTopSafeStandingPosition(ServerLevel level, int x, int z) {
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos candidate = new BlockPos(x, surfaceY + 1, z);
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
}
