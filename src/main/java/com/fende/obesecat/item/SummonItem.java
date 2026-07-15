package com.fende.obesecat.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Shared targeting, cooldown, foil, and tooltip behavior for all magicite summons. */
public abstract class SummonItem extends Item {
    protected SummonItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }

        BlockHitResult target = findTarget(player);
        if (target == null) {
            return InteractionResultHolder.pass(stack);
        }

        if (level instanceof ServerLevel serverLevel) {
            beginSummon(serverLevel, player, target);
        }

        player.swing(hand, true);
        player.getCooldowns().addCooldown(this, cooldownTicks());
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        String key = captionKey();
        if (key != null) {
            tooltip.add(Component.translatable(key).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    @Nullable
    private BlockHitResult findTarget(Player player) {
        HitResult hit = player.pick(range(), 1.0F, false);
        return hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK ? blockHit : null;
    }

    protected abstract double range();
    protected abstract int cooldownTicks();
    protected abstract String captionKey();
    protected abstract void beginSummon(ServerLevel level, Player player, BlockHitResult target);
}
