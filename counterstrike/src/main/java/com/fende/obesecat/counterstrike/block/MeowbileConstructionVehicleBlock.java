package com.fende.obesecat.counterstrike.block;

import com.fende.obesecat.counterstrike.block.entity.ConstructionYardCoreBlockEntity;
import com.fende.obesecat.counterstrike.block.entity.ConstructionYardHullBlockEntity;
import com.fende.obesecat.counterstrike.deploy.IDeployableStructure;
import com.fende.obesecat.counterstrike.deploy.StructureBounds;
import com.fende.obesecat.counterstrike.item.CommandCursorItem;
import com.fende.obesecat.counterstrike.registry.CSBlocks;
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

public class MeowbileConstructionVehicleBlock extends Block implements IDeployableStructure {
    public static final MapCodec<MeowbileConstructionVehicleBlock> CODEC = simpleCodec(MeowbileConstructionVehicleBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public MeowbileConstructionVehicleBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public MeowbileConstructionVehicleBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_YELLOW)
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
        return StructureBounds.constructionYard();
    }

    @Override
    public String getStructureName() {
        return "Construction Yard";
    }

    @Override
    public boolean canDeploy(Level level, BlockPos originPos, Player player) {
        BlockState state = level.getBlockState(originPos);
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;

        for (int v = 1; v <= 4; v++) {
            for (int u = -2; u <= 2; u++) {
                for (int w = 0; w < 6; w++) {
                    BlockPos target = ConstructionYardCoreBlockEntity.getRelativePos(originPos, facing, u, v, w);
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

        // Ensure floor positions aren't indestructible bedrock
        for (int u = -2; u <= 2; u++) {
            for (int w = 0; w < 6; w++) {
                BlockPos floorTarget = ConstructionYardCoreBlockEntity.getRelativePos(originPos, facing, u, 0, w);
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

        // Calculate internal Master Core position (depth w = 4 inside the walk-in bunker)
        BlockPos corePos = ConstructionYardCoreBlockEntity.getRelativePos(originPos, facing, 0, 1, 4);

        // Build the 5x6x5 structure with ground-level floor, pneumatic door, and interior core
        ConstructionYardCoreBlockEntity.buildStructure(level, originPos, facing, corePos, player != null ? player.getUUID() : null);

        // Smoke / assembly particle effects
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    originPos.getX() + 0.5, originPos.getY() + 1.0, originPos.getZ() + 0.5,
                    50, 2.5, 1.2, 2.5, 0.05);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    corePos.getX() + 0.5, corePos.getY() + 1.5, corePos.getZ() + 0.5,
                    30, 1.5, 1.0, 1.5, 0.1);
        }

        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            if (!(player.getMainHandItem().getItem() instanceof CommandCursorItem)) {
                player.displayClientMessage(
                        Component.literal("§e[EVA] §fMeow-bile Construction Vehicle deployed in sector. Use Tactical Command Cursor to establish base."),
                        true
                );
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
