package com.fende.obesecat.counterstrike.deploy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the relative 3D volume required by a deployable multiblock structure.
 */
public record StructureBounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {

    public static StructureBounds centered(int width, int height, int depth) {
        int halfW = width / 2;
        int halfD = depth / 2;
        return new StructureBounds(-halfW, width - halfW - 1, 0, height - 1, -halfD, depth - halfD - 1);
    }

    public static StructureBounds of(int width, int height, int depth) {
        return new StructureBounds(0, width - 1, 0, height - 1, 0, depth - 1);
    }

    public static StructureBounds constructionYard() {
        return new StructureBounds(-2, 2, -1, 3, 0, 5);
    }

    public static StructureBounds fissionTurbine() {
        return new StructureBounds(-1, 2, -1, 2, 0, 4);
    }

    public static StructureBounds barracks() {
        return new StructureBounds(-2, 2, -1, 2, 0, 4);
    }

    /**
     * Checks if all blocks within the relative bounds are air or replaceable,
     * ignoring the position of the deployer block itself.
     */
    public boolean isClear(Level level, BlockPos originPos, BlockPos excludePos) {
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos target = originPos.offset(x, y, z);
                    if (target.equals(excludePos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(target);
                    if (!state.isAir() && !state.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns a list of positions that are currently obstructing deployment.
     */
    public List<BlockPos> getObstructions(Level level, BlockPos originPos, BlockPos excludePos) {
        List<BlockPos> obstructions = new ArrayList<>();
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos target = originPos.offset(x, y, z);
                    if (target.equals(excludePos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(target);
                    if (!state.isAir() && !state.canBeReplaced()) {
                        obstructions.add(target);
                    }
                }
            }
        }
        return obstructions;
    }

    public int totalBlocks() {
        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }
}
