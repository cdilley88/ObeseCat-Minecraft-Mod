package com.fende.obesecat.counterstrike.block;

import com.fende.obesecat.counterstrike.block.entity.ConstructionYardCoreBlockEntity;
import com.fende.obesecat.counterstrike.item.CommandCursorItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import com.fende.obesecat.counterstrike.registry.CSBlockEntities;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ConstructionYardCoreBlock extends BaseEntityBlock {
    public static final MapCodec<ConstructionYardCoreBlock> CODEC = simpleCodec(ConstructionYardCoreBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ConstructionYardCoreBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public ConstructionYardCoreBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GREEN)
                .strength(6.0F, 1200.0F)
                .sound(SoundType.NETHERITE_BLOCK)
                .lightLevel(state -> 14)
                .requiresCorrectToolForDrops());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConstructionYardCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, CSBlockEntities.CONSTRUCTION_YARD_CORE.get(), ConstructionYardCoreBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            if (player.getMainHandItem().getItem() instanceof CommandCursorItem && player.isShiftKeyDown()) {
                if (level.getBlockEntity(pos) instanceof ConstructionYardCoreBlockEntity core) {
                    core.packUp(level, pos, player);
                    player.displayClientMessage(
                            Component.literal("§a[EVA] §fStructure disassembled. Transport module secured."),
                            true
                    );
                    return InteractionResult.SUCCESS;
                }
            }
            if (level.getBlockEntity(pos) instanceof ConstructionYardCoreBlockEntity core) {
                player.openMenu(core, pos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ConstructionYardCoreBlockEntity core) {
            core.packUp(level, pos, player);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
