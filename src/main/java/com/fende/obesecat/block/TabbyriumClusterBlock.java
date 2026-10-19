package com.fende.obesecat.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TabbyriumClusterBlock extends Block {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);

    private static final VoxelShape SHAPE_AGE_0 = Block.box(4.0, 0.0, 4.0, 12.0, 4.0, 12.0);
    private static final VoxelShape SHAPE_AGE_1 = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
    private static final VoxelShape SHAPE_AGE_2 = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);
    private static final VoxelShape SHAPE_AGE_3 = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public TabbyriumClusterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(AGE)) {
            case 0 -> SHAPE_AGE_0;
            case 1 -> SHAPE_AGE_1;
            case 2 -> SHAPE_AGE_2;
            default -> SHAPE_AGE_3;
        };
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, true, true));
            living.hurt(level.damageSources().magic(), 1.0F);
        }
        super.stepOn(level, pos, state, entity);
    }
}
