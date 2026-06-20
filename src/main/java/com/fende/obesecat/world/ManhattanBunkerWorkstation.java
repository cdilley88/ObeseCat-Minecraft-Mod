package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
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

    public static BlockPos findSpawnPos(LevelAccessor level, BoundingBox box) {
        for (int y = box.minY(); y <= box.maxY(); y++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                for (int x = box.minX(); x <= box.maxX(); x++) {
                    BlockPos workstationPos = new BlockPos(x, y, z);
                    if (!level.getBlockState(workstationPos).is(ModBlocks.NUCLEAR_LIBRARY.get())) {
                        continue;
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
                }
            }
        }

        return null;
    }

    private static boolean isSpawnable(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && !level.getBlockState(pos.below()).isAir();
    }

    private ManhattanBunkerWorkstation() {
    }
}
