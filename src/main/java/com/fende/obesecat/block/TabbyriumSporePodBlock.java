package com.fende.obesecat.block;

import com.fende.obesecat.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class TabbyriumSporePodBlock extends Block {
    public TabbyriumSporePodBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(3.0F, 9.0F)
                .sound(SoundType.SLIME_BLOCK)
                .lightLevel(state -> 15)
                .randomTicks());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double px = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.8D;
        double py = pos.getY() + 0.5D + (random.nextDouble() - 0.5D) * 0.8D;
        double pz = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.8D;
        level.addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0.0D, 0.05D, 0.0D);

        if (random.nextInt(20) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.CHORUS_FLOWER_GROW, SoundSource.BLOCKS, 0.5F, 0.8F, false);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int radius = 7;
        for (int i = 0; i < 3; i++) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            int dy = random.nextInt(7) - 3;
            BlockPos targetPos = pos.offset(dx, dy, dz);
            if (!level.isLoaded(targetPos)) continue;

            BlockState targetState = level.getBlockState(targetPos);
            if (isConvertibleGround(targetState)) {
                level.setBlockAndUpdate(targetPos, ModBlocks.TABBYRIUM_CRUST.get().defaultBlockState());
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        targetPos.getX() + 0.5D, targetPos.getY() + 1.1D, targetPos.getZ() + 0.5D,
                        3, 0.2D, 0.1D, 0.2D, 0.02D);
            } else if (targetState.isAir()) {
                BlockState belowState = level.getBlockState(targetPos.below());
                if (belowState.is(ModBlocks.TABBYRIUM_CRUST.get()) || belowState.is(Blocks.STONE)) {
                    level.setBlockAndUpdate(targetPos,
                            ModBlocks.TABBYRIUM_CLUSTER.get().defaultBlockState().setValue(TabbyriumClusterBlock.AGE, 0));
                }
            } else if (targetState.is(ModBlocks.TABBYRIUM_CLUSTER.get())) {
                int currentAge = targetState.getValue(TabbyriumClusterBlock.AGE);
                if (currentAge < 3) {
                    level.setBlockAndUpdate(targetPos,
                            targetState.setValue(TabbyriumClusterBlock.AGE, currentAge + 1));
                    level.sendParticles(ParticleTypes.GLOW,
                            targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D,
                            4, 0.2D, 0.2D, 0.2D, 0.01D);
                }
            }
        }
    }

    private boolean isConvertibleGround(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.SAND)
                || state.is(Blocks.GRAVEL);
    }
}
