package com.fende.obesecat.block;

import com.fende.obesecat.block.entity.AtomicCanOpenerBlockEntity;
import com.fende.obesecat.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
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

public class AtomicCanOpenerBlock extends BaseEntityBlock {
    public static final MapCodec<AtomicCanOpenerBlock> CODEC = simpleCodec(AtomicCanOpenerBlock::new);

    public AtomicCanOpenerBlock(BlockBehaviour.Properties properties) { super(properties); }
    public AtomicCanOpenerBlock() {
        this(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops()
                .strength(5.0F, 12.0F).sound(SoundType.METAL));
    }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AtomicCanOpenerBlockEntity opener) {
            player.openMenu(opener, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof AtomicCanOpenerBlockEntity opener) opener.dropContents(level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AtomicCanOpenerBlockEntity(pos, state); }
    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.CAN_OPENER.get(), AtomicCanOpenerBlockEntity::serverTick);
    }
}
