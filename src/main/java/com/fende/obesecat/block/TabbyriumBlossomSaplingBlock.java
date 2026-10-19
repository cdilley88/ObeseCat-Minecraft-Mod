package com.fende.obesecat.block;

import com.fende.obesecat.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabbyriumBlossomSaplingBlock extends Block implements BonemealableBlock {
    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0);

    public TabbyriumBlossomSaplingBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(0.0F)
                .sound(SoundType.GRASS)
                .noCollission()
                .lightLevel(state -> 5)
                .randomTicks());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return belowState.is(BlockTags.DIRT)
                || belowState.is(Blocks.STONE)
                || belowState.is(ModBlocks.TABBYRIUM_CRUST.get())
                || belowState.is(ModBlocks.TABBYRIUM_STALK.get())
                || belowState.isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(7) == 0) {
            growTree(level, pos, random);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        growTree(level, pos, random);
    }

    public void growTree(ServerLevel level, BlockPos pos, RandomSource random) {
        // Random 3, 4, or 5 block high stalk trunk
        int height = 3 + random.nextInt(3);

        // Build vertical stalk trunk
        for (int y = 0; y < height; y++) {
            level.setBlock(pos.above(y), ModBlocks.TABBYRIUM_STALK.get().defaultBlockState(), 3);
        }

        // Place Spore Pod crown at the top
        BlockPos podPos = pos.above(height);
        level.setBlock(podPos, ModBlocks.TABBYRIUM_SPORE_POD.get().defaultBlockState(), 3);

        int crustPlaced = 0;
        BlockPos baseGround = pos.below();
        if (TabbyriumStalkBlock.isConvertibleGround(level.getBlockState(baseGround))) {
            level.setBlock(baseGround, ModBlocks.TABBYRIUM_CRUST.get().defaultBlockState(), 3);
            crustPlaced++;
        }

        // Generate 3 to 4 organic tendrils stretching from the stalk (max 18 crust blocks total)
        List<Direction> dirs = new ArrayList<>(List.of(
                Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
        ));
        Collections.shuffle(dirs, new java.util.Random(random.nextLong()));
        int tendrilCount = 3 + random.nextInt(2); // 3 or 4 tendrils

        for (int i = 0; i < tendrilCount && i < dirs.size(); i++) {
            if (crustPlaced >= TabbyriumStalkBlock.MAX_CRUST_PER_BLOSSOM) break;

            Direction dir = dirs.get(i);

            // Horizontal root knuckle extending from the stalk
            BlockPos knucklePos = pos.relative(dir);
            BlockState knuckleState = level.getBlockState(knucklePos);
            if (knuckleState.isAir() || knuckleState.canBeReplaced()) {
                level.setBlock(knucklePos, ModBlocks.TABBYRIUM_STALK.get().defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS, dir.getAxis()), 3);
                BlockPos knuckleGround = knucklePos.below();
                if (TabbyriumStalkBlock.isConvertibleGround(level.getBlockState(knuckleGround))) {
                    level.setBlock(knuckleGround, ModBlocks.TABBYRIUM_CRUST.get().defaultBlockState(), 3);
                    crustPlaced++;
                }
            }

            // Trace tendril path snaking outward along the ground
            int tendrilLength = 3 + random.nextInt(3); // 3 to 5 blocks
            BlockPos current = knucklePos;

            for (int step = 1; step <= tendrilLength; step++) {
                if (crustPlaced >= TabbyriumStalkBlock.MAX_CRUST_PER_BLOSSOM) break;

                current = current.relative(dir);
                // 30% chance to meander 1 block perpendicular
                if (random.nextInt(3) == 0) {
                    current = current.relative(random.nextBoolean() ? dir.getClockWise() : dir.getCounterClockWise());
                }

                BlockPos surface = TabbyriumStalkBlock.findGroundSurface(level, current);
                if (surface != null) {
                    BlockState surfaceState = level.getBlockState(surface);
                    if (TabbyriumStalkBlock.isConvertibleGround(surfaceState) && !surfaceState.is(ModBlocks.TABBYRIUM_CRUST.get())) {
                        level.setBlock(surface, ModBlocks.TABBYRIUM_CRUST.get().defaultBlockState(), 3);
                        crustPlaced++;

                        // Sprout clusters along the tendril and at the tip
                        BlockPos above = surface.above();
                        if (level.getBlockState(above).isAir() && (step == tendrilLength || random.nextInt(3) == 0)) {
                            level.setBlock(above, ModBlocks.TABBYRIUM_CLUSTER.get().defaultBlockState()
                                    .setValue(TabbyriumClusterBlock.AGE, random.nextInt(2)), 3);
                        }
                    }
                }
            }
        }

        level.playSound(null, podPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2F, 1.4F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                podPos.getX() + 0.5D, podPos.getY() + 0.5D, podPos.getZ() + 0.5D,
                25, 1.0D, 1.0D, 1.0D, 0.1D);
    }
}
