package com.fende.obesecat.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class AtomicFireBlock extends Block {
    public static final MapCodec<AtomicFireBlock> CODEC = simpleCodec(AtomicFireBlock::new);

    public AtomicFireBlock(Properties properties) {
        super(properties);
    }

    public AtomicFireBlock() {
        this(Properties.of()
                .mapColor(MapColor.FIRE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .replaceable()
                .noCollission()
                .instabreak()
                .lightLevel(state -> 15)
                .pushReaction(PushReaction.DESTROY)
                .noLootTable());
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
