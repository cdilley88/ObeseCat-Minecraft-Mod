package com.fende.obesecat.counterstrike.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class TurbineReactorCoreBlock extends Block {
    public static final MapCodec<TurbineReactorCoreBlock> CODEC = simpleCodec(TurbineReactorCoreBlock::new);

    public TurbineReactorCoreBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public TurbineReactorCoreBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GREEN)
                .strength(6.0F, 1200.0F)
                .sound(SoundType.AMETHYST)
                .lightLevel(state -> 15)
                .requiresCorrectToolForDrops());
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            player.displayClientMessage(
                    Component.literal("§a[EVA] §fTabbyrium Fission Core operational. Emitting +200 GW Grid Power."),
                    true
            );
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
