package com.fende.obesecat.block;

import com.fende.obesecat.block.entity.TabbyriumRefineryBlockEntity;
import com.fende.obesecat.registry.ModBlockEntities;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TabbyriumRefineryBlock extends BaseEntityBlock {
    public static final MapCodec<TabbyriumRefineryBlock> CODEC = simpleCodec(TabbyriumRefineryBlock::new);

    public TabbyriumRefineryBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public TabbyriumRefineryBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops()
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TabbyriumRefineryBlockEntity refinery) {
            if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
                player.displayClientMessage(Component.literal("§a[EVA] §fRefinery operational. Establishing battlefield control."), true);
                level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8F, 1.8F);
                return InteractionResult.SUCCESS;
            }
            player.openMenu(refinery, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TabbyriumRefineryBlockEntity refinery) {
            refinery.dropContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TabbyriumRefineryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.TABBYRIUM_REFINERY.get(), TabbyriumRefineryBlockEntity::serverTick);
    }
}
