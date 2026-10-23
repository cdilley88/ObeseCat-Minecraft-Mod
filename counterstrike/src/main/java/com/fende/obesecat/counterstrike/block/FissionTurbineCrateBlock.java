package com.fende.obesecat.counterstrike.block;

import com.fende.obesecat.counterstrike.block.entity.FissionTurbineCoreBlockEntity;
import com.fende.obesecat.counterstrike.deploy.IDeployableStructure;
import com.fende.obesecat.counterstrike.deploy.StructureBounds;
import com.fende.obesecat.counterstrike.item.CommandCursorItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class FissionTurbineCrateBlock extends Block implements IDeployableStructure {
    public static final MapCodec<FissionTurbineCrateBlock> CODEC = simpleCodec(FissionTurbineCrateBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public FissionTurbineCrateBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public FissionTurbineCrateBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GREEN)
                .strength(4.0F, 600.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());
    }

    @Override
    protected MapCodec<? extends Block> codec() {
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
    public StructureBounds getBounds() {
        return StructureBounds.fissionTurbine();
    }

    @Override
    public String getStructureName() {
        return "Tabbyrium Fission Turbine";
    }

    @Override
    public boolean canDeploy(Level level, BlockPos originPos, Player player) {
        BlockState state = level.getBlockState(originPos);
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;

        for (int v = 1; v <= 3; v++) {
            for (int u = -1; u <= 2; u++) {
                for (int w = 0; w < 5; w++) {
                    BlockPos target = FissionTurbineCoreBlockEntity.getRelativePos(originPos, facing, u, v, w);
                    if (target.equals(originPos)) {
                        continue;
                    }
                    BlockState targetState = level.getBlockState(target);
                    if (!targetState.isAir() && !targetState.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }

        // Check floor layer
        for (int u = -1; u <= 2; u++) {
            for (int w = 0; w < 5; w++) {
                BlockPos floorTarget = FissionTurbineCoreBlockEntity.getRelativePos(originPos, facing, u, 0, w);
                BlockState floorState = level.getBlockState(floorTarget);
                if (floorState.getDestroySpeed(level, floorTarget) < 0) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean deploy(Level level, BlockPos originPos, Player player) {
        BlockState state = level.getBlockState(originPos);
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;

        if (!canDeploy(level, originPos, player)) {
            return false;
        }

        BlockPos corePos = FissionTurbineCoreBlockEntity.getRelativePos(originPos, facing, 0, 1, 3);
        FissionTurbineCoreBlockEntity.buildStructure(level, originPos, facing, corePos, player != null ? player.getUUID() : null);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    originPos.getX() + 0.5, originPos.getY() + 1.0, originPos.getZ() + 0.5,
                    35, 1.5, 1.0, 1.5, 0.05);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    corePos.getX() + 0.5, corePos.getY() + 1.5, corePos.getZ() + 0.5,
                    20, 1.0, 1.0, 1.0, 0.1);
        }

        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            if (!(player.getMainHandItem().getItem() instanceof CommandCursorItem)) {
                player.displayClientMessage(
                        Component.literal("§e[EVA] §fTabbyrium Fission Turbine crate placed. Use Tactical Command Cursor to deploy."),
                        true
                );
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
