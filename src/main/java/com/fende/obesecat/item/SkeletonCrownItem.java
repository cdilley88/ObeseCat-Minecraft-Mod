package com.fende.obesecat.item;

import com.fende.obesecat.entity.SkeletonKing;
import com.fende.obesecat.registry.ModEntities;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SkeletonCrownItem extends Item {
    public SkeletonCrownItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockState clickedState = level.getBlockState(pos);
        if (!clickedState.is(Blocks.BONE_BLOCK)) {
            return InteractionResult.PASS;
        }

        AltarMatch match = findAltar(level, pos);
        if (match == null) {
            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("message.obesecat.skeleton_king_altar_hint"),
                        true
                );
                player.playSound(SoundEvents.SKELETON_HURT, 0.8F, 0.6F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            if (!player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }

            // Remove the 4 bone blocks
            level.destroyBlock(match.center, false);
            level.destroyBlock(match.bottom, false);
            level.destroyBlock(match.arm1, false);
            level.destroyBlock(match.arm2, false);

            BlockPos center = match.center;

            // Explosion and soul fire bursts
            serverLevel.explode(
                    null,
                    center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D,
                    3.5F,
                    Level.ExplosionInteraction.MOB
            );

            serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D,
                    90, 1.4D, 1.6D, 1.4D, 0.1D
            );
            serverLevel.sendParticles(
                    ParticleTypes.LARGE_SMOKE,
                    center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D,
                    50, 1.2D, 1.2D, 1.2D, 0.05D
            );

            serverLevel.playSound(null, center, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.5F, 0.75F);
            serverLevel.playSound(null, center, SoundEvents.SKELETON_AMBIENT, SoundSource.HOSTILE, 2.5F, 0.45F);

            // Summon Skeleton King
            SkeletonKing king = ModEntities.SKELETON_KING.get().create(serverLevel);
            if (king != null) {
                king.moveTo(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D, player.getYRot() + 180.0F, 0.0F);
                king.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(center), MobSpawnType.EVENT, null);
                serverLevel.addFreshEntity(king);
            }

            player.displayClientMessage(
                    Component.translatable("message.obesecat.skeleton_king_awakens"),
                    true
            );
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    private static AltarMatch findAltar(Level level, BlockPos clicked) {
        BlockPos[] candidates = {
                clicked,
                clicked.above(),
                clicked.north(),
                clicked.south(),
                clicked.east(),
                clicked.west()
        };

        for (BlockPos center : candidates) {
            AltarMatch match = checkCenter(level, center);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    @Nullable
    private static AltarMatch checkCenter(Level level, BlockPos center) {
        if (!level.getBlockState(center).is(Blocks.BONE_BLOCK)) return null;
        BlockPos bottom = center.below();
        if (!level.getBlockState(bottom).is(Blocks.BONE_BLOCK)) return null;

        // Check X-axis arms (East & West)
        BlockPos east = center.east();
        BlockPos west = center.west();
        if (level.getBlockState(east).is(Blocks.BONE_BLOCK) && level.getBlockState(west).is(Blocks.BONE_BLOCK)) {
            return new AltarMatch(center, bottom, east, west);
        }

        // Check Z-axis arms (North & South)
        BlockPos north = center.north();
        BlockPos south = center.south();
        if (level.getBlockState(north).is(Blocks.BONE_BLOCK) && level.getBlockState(south).is(Blocks.BONE_BLOCK)) {
            return new AltarMatch(center, bottom, north, south);
        }

        return null;
    }

    private record AltarMatch(BlockPos center, BlockPos bottom, BlockPos arm1, BlockPos arm2) {
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.obesecat.skeleton_crown.caption"));
        tooltipComponents.add(Component.translatable("item.obesecat.skeleton_crown.ritual"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
