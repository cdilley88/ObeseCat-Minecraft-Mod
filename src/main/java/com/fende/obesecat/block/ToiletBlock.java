package com.fende.obesecat.block;

import com.fende.obesecat.registry.ModSounds;
import com.fende.obesecat.world.ToiletSinkAnimation;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class ToiletBlock extends Block {
    public static final MapCodec<ToiletBlock> CODEC = simpleCodec(ToiletBlock::new);

    public ToiletBlock(Properties properties) {
        super(properties);
    }

    public ToiletBlock() {
        this(Properties.of()
                .mapColor(MapColor.DIRT)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .strength(0.5F)
                .sound(SoundType.GRAVEL)
                .noOcclusion());
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        SoundEvent stink = switch (serverLevel.random.nextInt(3)) {
            case 0 -> ModSounds.TOILET_STINK_1.get();
            case 1 -> ModSounds.TOILET_STINK_2.get();
            default -> ModSounds.TOILET_STINK_3.get();
        };
        serverLevel.playSound(null, pos, stink, SoundSource.BLOCKS, 1.0F, 0.95F + serverLevel.random.nextFloat() * 0.1F);
        ToiletSinkAnimation.create(serverLevel, pos, state);
        return InteractionResult.CONSUME;
    }
}
