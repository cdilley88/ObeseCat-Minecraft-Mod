package com.fende.obesecat.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EnigmaEmberItem extends CaptionedItem {
    private static final double RANGE = 40.0D;
    private static final int COOLDOWN_TICKS = 2;

    public EnigmaEmberItem(Properties properties) {
        super(properties, "item.obesecat.enigma_ember.caption");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (!player.getAbilities().instabuild && player.getFoodData().getFoodLevel() <= 0) {
            return InteractionResultHolder.fail(stack);
        }

        HitResult hit = player.pick(RANGE, 1.0F, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.fail(stack);
        }

        BlockPos destinationBlock = blockHit.getBlockPos().relative(blockHit.getDirection());
        if (!canStandAt(level, destinationBlock)) {
            destinationBlock = blockHit.getBlockPos().above();
        }
        if (!canStandAt(level, destinationBlock)) {
            return InteractionResultHolder.fail(stack);
        }

        WormholeEmberItem.sendDepartureEffects(serverPlayer);
        Vec3 destination = Vec3.atBottomCenterOf(destinationBlock);
        serverPlayer.teleportTo(serverPlayer.serverLevel(), destination.x, destination.y, destination.z, serverPlayer.getYRot(), serverPlayer.getXRot());
        if (!player.getAbilities().instabuild) {
            player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - 1));
        }
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        WormholeEmberItem.sendArrivalEffects(serverPlayer);

        return InteractionResultHolder.success(stack);
    }

    private static boolean canStandAt(Level level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
    }
}
