package com.fende.obesecat.block;

import com.fende.obesecat.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class TabbyriumStalkBlock extends RotatedPillarBlock {
    public static final int MAX_CRUST_PER_BLOSSOM = 18;
    public static final int SPREAD_RADIUS = 8;
    public static final int SPREAD_VERTICAL = 4;

    public TabbyriumStalkBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(2.0F, 3.0F)
                .sound(SoundType.WOOD)
                .lightLevel(state -> 4)
                .randomTicks());
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos base = findStalkBase(level, pos);
        int crustCount = countNearbyCrust(level, base, SPREAD_RADIUS, SPREAD_VERTICAL);

        if (crustCount >= MAX_CRUST_PER_BLOSSOM) {
            // Hard cap reached: do not spread more crust. Nurture existing crystal clusters instead.
            nurtureClustersNearby(level, base, random);
            return;
        }

        // Under 18 crust blocks: extend tendrils from existing crust/stalk
        extendTendril(level, base, random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(12) == 0) {
            double px = pos.getX() + random.nextDouble();
            double py = pos.getY() + random.nextDouble();
            double pz = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.MYCELIUM, px, py, pz, 0.0D, 0.01D, 0.0D);
        }
    }

    public static BlockPos findStalkBase(ServerLevel level, BlockPos pos) {
        BlockPos current = pos;
        while (level.getBlockState(current.below()).is(ModBlocks.TABBYRIUM_STALK.get())) {
            current = current.below();
        }
        return current;
    }

    public static int countNearbyCrust(ServerLevel level, BlockPos center, int horizontalRadius, int verticalRadius) {
        int count = 0;
        BlockPos min = center.offset(-horizontalRadius, -verticalRadius, -horizontalRadius);
        BlockPos max = center.offset(horizontalRadius, verticalRadius, horizontalRadius);
        for (BlockPos p : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockState(p).is(ModBlocks.TABBYRIUM_CRUST.get())) {
                count++;
                if (count >= MAX_CRUST_PER_BLOSSOM) {
                    return count;
                }
            }
        }
        return count;
    }

    public static BlockPos findGroundSurface(ServerLevel level, BlockPos base) {
        for (int dy = 2; dy >= -SPREAD_VERTICAL; dy--) {
            BlockPos check = base.above(dy);
            BlockState state = level.getBlockState(check);
            if (!state.isAir() && !state.canBeReplaced()) {
                BlockState above = level.getBlockState(check.above());
                if (above.isAir() || above.canBeReplaced() || above.is(ModBlocks.TABBYRIUM_CLUSTER.get())) {
                    return check;
                }
            }
        }
        return null;
    }

    public static boolean isConvertibleGround(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.SAND)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.MUD);
    }

    public static void extendTendril(ServerLevel level, BlockPos base, RandomSource random) {
        List<BlockPos> existingCrust = new ArrayList<>();
        BlockPos min = base.offset(-SPREAD_RADIUS, -SPREAD_VERTICAL, -SPREAD_RADIUS);
        BlockPos max = base.offset(SPREAD_RADIUS, SPREAD_VERTICAL, SPREAD_RADIUS);

        for (BlockPos p : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockState(p).is(ModBlocks.TABBYRIUM_CRUST.get())) {
                existingCrust.add(p.immutable());
            }
        }

        if (existingCrust.isEmpty()) {
            BlockPos ground = findGroundSurface(level, base.below());
            if (ground != null && isConvertibleGround(level.getBlockState(ground))) {
                level.setBlockAndUpdate(ground, ModBlocks.TABBYRIUM_CRUST.get().defaultBlockState());
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        ground.getX() + 0.5D, ground.getY() + 1.1D, ground.getZ() + 0.5D,
                        3, 0.2D, 0.1D, 0.2D, 0.02D);
            }
            return;
        }

        // Pick an existing crust node to extend the tendril forward
        BlockPos origin = existingCrust.get(random.nextInt(existingCrust.size()));
        Direction stepDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos target = origin.relative(stepDir);
        BlockPos ground = findGroundSurface(level, target);

        if (ground != null) {
            BlockState targetState = level.getBlockState(ground);
            if (isConvertibleGround(targetState) && !targetState.is(ModBlocks.TABBYRIUM_CRUST.get())) {
                level.setBlockAndUpdate(ground, ModBlocks.TABBYRIUM_CRUST.get().defaultBlockState());
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        ground.getX() + 0.5D, ground.getY() + 1.1D, ground.getZ() + 0.5D,
                        3, 0.2D, 0.1D, 0.2D, 0.02D);

                // Chance to sprout cluster on the new tendril tip
                BlockPos above = ground.above();
                if (level.getBlockState(above).isAir() && random.nextInt(4) == 0) {
                    level.setBlockAndUpdate(above,
                            ModBlocks.TABBYRIUM_CLUSTER.get().defaultBlockState().setValue(TabbyriumClusterBlock.AGE, 0));
                }
            }
        }
    }

    public static void nurtureClustersNearby(ServerLevel level, BlockPos base, RandomSource random) {
        List<BlockPos> existingCrust = new ArrayList<>();
        BlockPos min = base.offset(-SPREAD_RADIUS, -SPREAD_VERTICAL, -SPREAD_RADIUS);
        BlockPos max = base.offset(SPREAD_RADIUS, SPREAD_VERTICAL, SPREAD_RADIUS);

        for (BlockPos p : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockState(p).is(ModBlocks.TABBYRIUM_CRUST.get())) {
                existingCrust.add(p.immutable());
            }
        }

        if (existingCrust.isEmpty()) return;

        BlockPos crustPos = existingCrust.get(random.nextInt(existingCrust.size()));
        BlockPos clusterPos = crustPos.above();
        BlockState clusterState = level.getBlockState(clusterPos);

        if (clusterState.isAir()) {
            if (random.nextInt(4) == 0) {
                level.setBlockAndUpdate(clusterPos,
                        ModBlocks.TABBYRIUM_CLUSTER.get().defaultBlockState().setValue(TabbyriumClusterBlock.AGE, 0));
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        clusterPos.getX() + 0.5D, clusterPos.getY() + 0.3D, clusterPos.getZ() + 0.5D,
                        3, 0.2D, 0.1D, 0.2D, 0.02D);
            }
        } else if (clusterState.is(ModBlocks.TABBYRIUM_CLUSTER.get())) {
            int currentAge = clusterState.getValue(TabbyriumClusterBlock.AGE);
            if (currentAge < 3 && random.nextInt(3) == 0) {
                level.setBlockAndUpdate(clusterPos, clusterState.setValue(TabbyriumClusterBlock.AGE, currentAge + 1));
                level.sendParticles(ParticleTypes.GLOW,
                        clusterPos.getX() + 0.5D, clusterPos.getY() + 0.5D, clusterPos.getZ() + 0.5D,
                        4, 0.2D, 0.2D, 0.2D, 0.01D);
            }
        }
    }
}
