package com.fende.obesecat.item;

import com.fende.obesecat.entity.TheCarver;
import com.fende.obesecat.registry.ModEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CursedCleaverItem extends SwordItem {
    public CursedCleaverItem(Properties properties) {
        super(Tiers.DIAMOND, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (!(state.getBlock() instanceof AnvilBlock)) {
            return InteractionResult.PASS;
        }

        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (!belowState.is(Blocks.SCULK_CATALYST)) {
            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("message.obesecat.carver_ritual_catalyst_needed"),
                        true
                );
                player.playSound(SoundEvents.SCULK_SHRIEKER_SHRIEK, 0.8F, 0.5F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (player.experienceLevel < 30 && !player.getAbilities().instabuild) {
            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("message.obesecat.carver_ritual_xp_needed"),
                        true
                );
                player.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 0.5F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            // Drain XP and consume cleaver
            if (!player.getAbilities().instabuild) {
                player.experienceLevel = 0;
                player.experienceProgress = 0.0F;
                player.totalExperience = 0;
                context.getItemInHand().shrink(1);
            }

            // Destroy the anvil
            level.destroyBlock(pos, false);

            // Violent explosion
            serverLevel.explode(
                    null,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    3.5F,
                    Level.ExplosionInteraction.MOB
            );

            // Sculk spreading bloom
            spreadSculkAround(serverLevel, belowPos);

            // Particles
            serverLevel.sendParticles(
                    ParticleTypes.SCULK_SOUL,
                    pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D,
                    70, 1.6D, 1.0D, 1.6D, 0.08D
            );
            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    2, 0.1D, 0.1D, 0.1D, 0.0D
            );

            // Audio cues
            serverLevel.playSound(null, pos, SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 2.5F, 0.72F);
            serverLevel.playSound(null, pos, SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.5F, 0.68F);
            serverLevel.playSound(null, pos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 2.0F, 0.5F);

            // Spawn The Carver
            TheCarver carver = ModEntities.THE_CARVER.get().create(serverLevel);
            if (carver != null) {
                carver.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, player.getYRot() + 180.0F, 0.0F);
                carver.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
                serverLevel.addFreshEntity(carver);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void spreadSculkAround(ServerLevel level, BlockPos catalystPos) {
        int radius = 3;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) continue;
                for (int dy = -1; dy <= 2; dy++) {
                    BlockPos target = catalystPos.offset(dx, dy, dz);
                    BlockState current = level.getBlockState(target);
                    if (current.is(Blocks.SCULK_CATALYST) || current.is(Blocks.AIR)) continue;
                    
                    if (current.isSolidRender(level, target) && level.getBlockState(target.above()).isAir()) {
                        if (level.random.nextFloat() < 0.65F) {
                            level.setBlock(target, Blocks.SCULK.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.obesecat.cursed_cleaver.caption"));
        tooltipComponents.add(Component.translatable("item.obesecat.cursed_cleaver.ritual"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
