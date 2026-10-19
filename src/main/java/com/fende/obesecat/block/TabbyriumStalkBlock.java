package com.fende.obesecat.block;

import com.fende.obesecat.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class TabbyriumStalkBlock extends RotatedPillarBlock {
    public TabbyriumStalkBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(2.0F, 3.0F)
                .sound(SoundType.WOOD)
                .lightLevel(state -> 4)
                .randomTicks());
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Stalk leaches surrounding earth into Tabbyrium Crust
        int radius = 4;
        for (int i = 0; i < 2; i++) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            int dy = random.nextInt(5) - 3;
            BlockPos targetPos = pos.offset(dx, dy, dz);
            if (!level.isLoaded(targetPos)) continue;

            BlockState targetState = level.getBlockState(targetPos);
            if (isConvertibleGround(targetState)) {
                level.setBlockAndUpdate(targetPos, ModBlocks.TABBYRIUM_CRUST.get().defaultBlockState());
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        targetPos.getX() + 0.5D, targetPos.getY() + 1.1D, targetPos.getZ() + 0.5D,
                        3, 0.2D, 0.1D, 0.2D, 0.02D);
            }
        }
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

    private boolean isConvertibleGround(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.SAND)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.MUD);
    }
}
