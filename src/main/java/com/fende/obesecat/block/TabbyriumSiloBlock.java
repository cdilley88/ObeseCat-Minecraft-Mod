package com.fende.obesecat.block;

import com.fende.obesecat.block.entity.TabbyriumSiloBlockEntity;
import com.fende.obesecat.item.TabbyriumSiloItem;
import com.fende.obesecat.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TabbyriumSiloBlock extends BaseEntityBlock {
    public static final MapCodec<TabbyriumSiloBlock> CODEC = simpleCodec(TabbyriumSiloBlock::new);
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 4);

    public TabbyriumSiloBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LEVEL, 0));
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TabbyriumSiloBlockEntity silo) {
            // C&C Easter Egg: If sneak-clicked or when full
            if (player.isShiftKeyDown()) {
                player.displayClientMessage(Component.literal("§c[EVA] §fWARNING: SILOS NEEDED."), true);
                level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASEDRUM.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
            } else {
                player.openMenu(silo, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof TabbyriumSiloBlockEntity silo) {
            int stored = silo.getEnergyStorage().getEnergyStored();
            if (stored > 0) {
                for (ItemStack drop : drops) {
                    if (drop.is(this.asItem())) {
                        TabbyriumSiloItem.setStoredEnergy(drop, stored);
                    }
                }
            }
        }
        return drops;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TabbyriumSiloBlockEntity silo) {
            silo.dropContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TabbyriumSiloBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.TABBYRIUM_SILO.get(), TabbyriumSiloBlockEntity::serverTick);
    }
}
