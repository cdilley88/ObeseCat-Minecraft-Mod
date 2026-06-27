package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class ManhattanBunkerWorkstation {
    public static int replaceLecterns(LevelAccessor level, BoundingBox box) {
        int replaced = 0;

        for (int y = box.minY(); y <= box.maxY(); y++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                for (int x = box.minX(); x <= box.maxX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!level.getBlockState(pos).is(Blocks.LECTERN)) {
                        continue;
                    }

                    level.setBlock(pos, ModBlocks.NUCLEAR_LIBRARY.get().defaultBlockState(), 3);
                    replaced++;
                }
            }
        }

        return replaced;
    }

    public static BlockPos findWorkstationPos(LevelAccessor level, BoundingBox box) {
        for (int y = box.minY(); y <= box.maxY(); y++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                for (int x = box.minX(); x <= box.maxX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.getBlockState(pos).is(ModBlocks.NUCLEAR_LIBRARY.get())) {
                        return pos;
                    }
                }
            }
        }

        return null;
    }

    public static BlockPos findNearestWorkstation(LevelAccessor level, BlockPos origin, int radius) {
        return findWorkstationPos(level, new BoundingBox(
                origin.getX() - radius,
                origin.getY() - radius,
                origin.getZ() - radius,
                origin.getX() + radius,
                origin.getY() + radius,
                origin.getZ() + radius
        ));
    }

    public static BlockPos findSpawnPos(LevelAccessor level, BoundingBox box) {
        BlockPos workstationPos = findWorkstationPos(level, box);
        if (workstationPos == null) {
            return null;
        }

        BlockPos above = workstationPos.above();
        if (isSpawnable(level, above)) {
            return above;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacent = workstationPos.relative(direction);
            if (isSpawnable(level, adjacent)) {
                return adjacent;
            }
        }

        return null;
    }

    private static boolean isSpawnable(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && !level.getBlockState(pos.below()).isAir();
    }

    public static int cleanInteriorFloor(LevelAccessor level, BoundingBox box) {
        int cleaned = 0;
        int floorY = box.minY();

        for (int z = box.minZ() + 1; z <= box.maxZ() - 1; z++) {
            for (int x = box.minX() + 1; x <= box.maxX() - 1; x++) {
                BlockPos floorPos = new BlockPos(x, floorY, z);
                BlockPos abovePos = floorPos.above();

                BlockState floorState = level.getBlockState(floorPos);
                if (shouldReplaceFloorBlock(floorState)) {
                    level.setBlock(floorPos, Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                    cleaned++;
                }

                BlockState aboveState = level.getBlockState(abovePos);
                if (shouldClearInteriorPlant(aboveState)) {
                    level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 3);
                    cleaned++;
                }
            }
        }

        return cleaned;
    }

    private static boolean shouldReplaceFloorBlock(BlockState state) {
        if (state.is(Blocks.SMOOTH_STONE)
                || state.is(Blocks.SMOOTH_STONE_SLAB)
                || state.is(Blocks.POLISHED_ANDESITE)
                || state.is(Blocks.POLISHED_ANDESITE_SLAB)
                || state.is(Blocks.IRON_BARS)
                || state.is(Blocks.IRON_DOOR)
                || state.is(Blocks.LANTERN)
                || state.is(Blocks.CHAIN)
                || state.is(ModBlocks.NUCLEAR_LIBRARY.get())) {
            return false;
        }

        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.MOSS_BLOCK);
    }

    private static boolean shouldClearInteriorPlant(BlockState state) {
        return state.getBlock() instanceof BushBlock
                || state.getBlock() instanceof DoublePlantBlock
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN);
    }

    private ManhattanBunkerWorkstation() {
    }
}
