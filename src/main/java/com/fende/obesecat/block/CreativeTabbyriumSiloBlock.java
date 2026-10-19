package com.fende.obesecat.block;

import com.fende.obesecat.block.entity.CreativeTabbyriumSiloBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class CreativeTabbyriumSiloBlock extends BaseEntityBlock {
    public static final MapCodec<CreativeTabbyriumSiloBlock> CODEC = simpleCodec(CreativeTabbyriumSiloBlock::new);

    public CreativeTabbyriumSiloBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public CreativeTabbyriumSiloBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.GOLD)
                .strength(5.0F, 12.0F)
                .sound(SoundType.METAL));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreativeTabbyriumSiloBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("message.obesecat.creative_tabbyrium_silo.status"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
