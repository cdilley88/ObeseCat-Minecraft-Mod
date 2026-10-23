package com.fende.obesecat.counterstrike.block;

import com.fende.obesecat.counterstrike.block.entity.PneumaticDoorBlockEntity;
import com.fende.obesecat.counterstrike.registry.CSBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PneumaticDoorBlock extends BaseEntityBlock {
    public static final MapCodec<PneumaticDoorBlock> CODEC = simpleCodec(PneumaticDoorBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape NORTH_SHAPE = Block.box(0, 0, 13, 16, 16, 16);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0, 0, 0, 16, 16, 3);
    private static final VoxelShape EAST_SHAPE  = Block.box(0, 0, 0, 3, 16, 16);
    private static final VoxelShape WEST_SHAPE  = Block.box(13, 0, 0, 16, 16, 16);

    private static final VoxelShape NORTH_OPEN_SHAPE = Block.box(0, 0, 0, 3, 16, 16);
    private static final VoxelShape SOUTH_OPEN_SHAPE = Block.box(13, 0, 0, 16, 16, 16);
    private static final VoxelShape EAST_OPEN_SHAPE  = Block.box(0, 0, 0, 16, 16, 3);
    private static final VoxelShape WEST_OPEN_SHAPE  = Block.box(0, 0, 13, 16, 16, 16);

    public PneumaticDoorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public PneumaticDoorBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(5.0F, 1200.0F)
                .sound(SoundType.NETHERITE_BLOCK)
                .requiresCorrectToolForDrops());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, HALF);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean open = state.getValue(OPEN);
        Direction facing = state.getValue(FACING);

        if (!open) {
            return switch (facing) {
                case NORTH -> NORTH_SHAPE;
                case SOUTH -> SOUTH_SHAPE;
                case EAST  -> EAST_SHAPE;
                case WEST  -> WEST_SHAPE;
                default    -> Shapes.block();
            };
        }

        return switch (facing) {
            case NORTH -> NORTH_OPEN_SHAPE;
            case SOUTH -> SOUTH_OPEN_SHAPE;
            case EAST  -> EAST_OPEN_SHAPE;
            case WEST  -> WEST_OPEN_SHAPE;
            default    -> Shapes.empty();
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PneumaticDoorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, CSBlockEntities.PNEUMATIC_DOOR.get(), PneumaticDoorBlockEntity::serverTick);
    }

    public static void setOpenState(Level level, BlockPos pos, BlockState state, boolean open) {
        if (!state.hasProperty(OPEN) || state.getValue(OPEN) == open) {
            return;
        }

        DoubleBlockHalf half = state.getValue(HALF);
        BlockPos otherPos = (half == DoubleBlockHalf.LOWER) ? pos.above() : pos.below();
        BlockState otherState = level.getBlockState(otherPos);

        level.setBlock(pos, state.setValue(OPEN, open), 3);
        if (otherState.is(state.getBlock()) && otherState.getValue(HALF) != half) {
            level.setBlock(otherPos, otherState.setValue(OPEN, open), 3);
        }

        if (open) {
            level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.4F);
            level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.7F, 1.8F);
        } else {
            level.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 1.2F);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            if (player.getMainHandItem().getItem() instanceof com.fende.obesecat.counterstrike.item.CommandCursorItem && player.isShiftKeyDown()) {
                if (level.getBlockEntity(pos) instanceof PneumaticDoorBlockEntity door && door.getCorePos() != null) {
                    if (level.getBlockEntity(door.getCorePos()) instanceof com.fende.obesecat.counterstrike.block.entity.ConstructionYardCoreBlockEntity core) {
                        core.packUp(level, door.getCorePos(), player);
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal("§a[EVA] §fStructure disassembled. Transport module secured."),
                                true
                        );
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            boolean currentlyOpen = state.getValue(OPEN);
            setOpenState(level, pos, state, !currentlyOpen);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof PneumaticDoorBlockEntity door && door.getCorePos() != null) {
                if (level.getBlockEntity(door.getCorePos()) instanceof com.fende.obesecat.counterstrike.block.entity.ConstructionYardCoreBlockEntity core) {
                    core.packUp(level, door.getCorePos(), player);
                }
            }
        }
        DoubleBlockHalf half = state.getValue(HALF);
        BlockPos otherPos = (half == DoubleBlockHalf.LOWER) ? pos.above() : pos.below();
        BlockState otherState = level.getBlockState(otherPos);
        if (otherState.is(this) && otherState.getValue(HALF) != half) {
            level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 35);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
