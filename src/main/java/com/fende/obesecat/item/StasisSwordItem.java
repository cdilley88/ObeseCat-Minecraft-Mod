package com.fende.obesecat.item;

import com.fende.obesecat.world.StasisSwordManager;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class StasisSwordItem extends Item {
    private static final int COOLDOWN_TICKS = 40;

    public StasisSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }

        HitResult hitResult = player.pick(StasisSwordManager.RANGE, 1.0F, false);
        if (hitResult.getType() != HitResult.Type.BLOCK || !(hitResult instanceof BlockHitResult blockHit)) {
            return InteractionResultHolder.pass(stack);
        }

        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            StasisSwordManager.schedule(serverLevel, blockHit.getBlockPos());
        }

        player.swing(usedHand, true);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity livingEntity) {
        return 0;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, net.minecraft.core.BlockPos pos, LivingEntity miningEntity) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.neoforged.neoforge.common.ItemAbility itemAbility) {
        return net.neoforged.neoforge.common.ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(itemAbility);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.obesecat.stasis_sword.caption").withStyle(ChatFormatting.YELLOW));
    }
}
