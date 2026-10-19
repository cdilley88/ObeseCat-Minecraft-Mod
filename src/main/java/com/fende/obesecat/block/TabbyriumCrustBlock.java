package com.fende.obesecat.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import com.fende.obesecat.registry.ModBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

public class TabbyriumCrustBlock extends Block {
    public TabbyriumCrustBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE)
                .lightLevel(state -> 4)
                .randomTicks());
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);

        // 1. Sprout new clusters on crust if air above
        if (aboveState.isAir()) {
            if (random.nextInt(8) == 0) {
                level.setBlockAndUpdate(abovePos,
                        ModBlocks.TABBYRIUM_CLUSTER.get().defaultBlockState().setValue(TabbyriumClusterBlock.AGE, 0));
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        abovePos.getX() + 0.5D, abovePos.getY() + 0.3D, abovePos.getZ() + 0.5D,
                        3, 0.2D, 0.1D, 0.2D, 0.02D);
            }
        } else if (aboveState.is(ModBlocks.TABBYRIUM_CLUSTER.get())) {
            // 2. Grow existing cluster towards mature stage 3
            int currentAge = aboveState.getValue(TabbyriumClusterBlock.AGE);
            if (currentAge < 3 && random.nextInt(4) == 0) {
                level.setBlockAndUpdate(abovePos, aboveState.setValue(TabbyriumClusterBlock.AGE, currentAge + 1));
                level.sendParticles(ParticleTypes.GLOW,
                        abovePos.getX() + 0.5D, abovePos.getY() + 0.5D, abovePos.getZ() + 0.5D,
                        4, 0.2D, 0.2D, 0.2D, 0.01D);
            }
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, false, true, true));
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 1.05D;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.MYCELIUM, x, y, z, 0.0D, 0.02D, 0.0D);
        }
    }
}
