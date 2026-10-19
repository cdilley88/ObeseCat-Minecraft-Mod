package com.fende.obesecat.block;

import com.fende.obesecat.block.entity.TabbyriumSiloBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

public class TabbyriumSiloBlock extends BaseEntityBlock {
    public static final MapCodec<TabbyriumSiloBlock> CODEC = simpleCodec(TabbyriumSiloBlock::new);

    public TabbyriumSiloBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public TabbyriumSiloBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops()
                .strength(4.0F, 8.0F)
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TabbyriumSiloBlockEntity silo) {
            int stored = silo.getEnergyStorage().getEnergyStored();
            int max = silo.getEnergyStorage().getMaxEnergyStored();

            // C&C Easter Egg: If full or sneak-clicked
            if (player.isShiftKeyDown() || stored >= max) {
                player.displayClientMessage(Component.literal("§c[EVA] §fWARNING: SILOS NEEDED."), true);
                level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASEDRUM.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
            } else {
                player.displayClientMessage(Component.translatable("message.obesecat.tabbyrium_silo.status", stored, max), true);
                level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.4F, 1.2F);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TabbyriumSiloBlockEntity(pos, state);
    }
}
