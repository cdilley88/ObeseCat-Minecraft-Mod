package com.fende.obesecat.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class TrinititeBlock extends TransparentBlock {
    public static final MapCodec<TrinititeBlock> CODEC = simpleCodec(TrinititeBlock::new);

    public TrinititeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public TrinititeBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GREEN)
                .instrument(NoteBlockInstrument.PLING)
                .requiresCorrectToolForDrops()
                .strength(3.0F, 6.0F)
                .sound(SoundType.GLASS)
                .lightLevel(state -> 15)
                .noOcclusion());
    }

    @Override
    protected MapCodec<? extends TransparentBlock> codec() {
        return CODEC;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.isSteppingCarefully() && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().hotFloor(), 1.0F);
            if (!entity.fireImmune()) {
                entity.igniteForSeconds(4.0F);
            }
        }

        super.stepOn(level, pos, state, entity);
    }
}
