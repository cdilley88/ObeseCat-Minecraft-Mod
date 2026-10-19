package com.fende.obesecat.block;

import com.fende.obesecat.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TabbyriumBlossomSaplingBlock extends Block implements BonemealableBlock {
    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0);

    public TabbyriumBlossomSaplingBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(0.0F)
                .sound(SoundType.GRASS)
                .noCollission()
                .lightLevel(state -> 5)
                .randomTicks());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return belowState.is(BlockTags.DIRT)
                || belowState.is(Blocks.STONE)
                || belowState.is(ModBlocks.TABBYRIUM_CRUST.get())
                || belowState.is(ModBlocks.TABBYRIUM_STALK.get())
                || belowState.isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(7) == 0) {
            growTree(level, pos, random);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        growTree(level, pos, random);
    }

    public void growTree(ServerLevel level, BlockPos pos, RandomSource random) {
        int height = 4 + random.nextInt(3);
        // Build trunk using Tabbyrium Stalk
        for (int y = 0; y < height; y++) {
            level.setBlock(pos.above(y), ModBlocks.TABBYRIUM_STALK.get().defaultBlockState(), 3);
        }
        // Place Spore Pod at the crown
        BlockPos podPos = pos.above(height);
        level.setBlock(podPos, ModBlocks.TABBYRIUM_SPORE_POD.get().defaultBlockState(), 3);

        // Place initial crust and crystal clusters around the base (radius 2)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos targetPos = pos.offset(dx, 0, dz);
                BlockPos belowPos = targetPos.below();
                BlockState belowState = level.getBlockState(belowPos);
                if (belowState.is(BlockTags.DIRT) || belowState.is(Blocks.STONE) || belowState.is(Blocks.SAND) || belowState.is(Blocks.GRAVEL)) {
                    level.setBlock(belowPos, ModBlocks.TABBYRIUM_CRUST.get().defaultBlockState(), 3);
                }
                if (level.getBlockState(targetPos).isAir() && (Math.abs(dx) <= 1 && Math.abs(dz) <= 1)) {
                    if (random.nextInt(3) != 0) {
                        int age = random.nextInt(2);
                        level.setBlock(targetPos, ModBlocks.TABBYRIUM_CLUSTER.get().defaultBlockState()
                                .setValue(TabbyriumClusterBlock.AGE, age), 3);
                    }
                }
            }
        }

        level.playSound(null, podPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2F, 1.4F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                podPos.getX() + 0.5D, podPos.getY() + 0.5D, podPos.getZ() + 0.5D,
                25, 1.0D, 1.0D, 1.0D, 0.1D);
    }
}
