package com.fende.obesecat.block;

import com.fende.obesecat.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
        BlockPos base = TabbyriumStalkBlock.findStalkBase(level, pos);
        int crustCount = TabbyriumStalkBlock.countNearbyCrust(level, base,
                TabbyriumStalkBlock.SPREAD_RADIUS, TabbyriumStalkBlock.SPREAD_VERTICAL);

        // Only extend tendrils if beneath the 18 crust maximum
        if (crustCount < TabbyriumStalkBlock.MAX_CRUST_PER_BLOSSOM) {
            TabbyriumStalkBlock.extendTendril(level, base, random);
        }

        // Spores stimulate crystal cluster growth along existing tendrils
        TabbyriumStalkBlock.nurtureClustersNearby(level, base, random);
    }
}
