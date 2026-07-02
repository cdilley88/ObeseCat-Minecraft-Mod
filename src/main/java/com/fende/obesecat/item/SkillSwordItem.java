package com.fende.obesecat.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public class SkillSwordItem extends Item {
    public SkillSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!(level instanceof ServerLevel serverLevel) || !cast(serverLevel, player, usedHand, stack)) {
            return InteractionResultHolder.pass(stack);
        }

        player.swing(usedHand, true);
        int cooldownTicks = cooldownTicks();
        if (cooldownTicks > 0) {
            player.getCooldowns().addCooldown(this, cooldownTicks);
        }
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
        return isFoilByDefault();
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(itemAbility);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        String captionKey = captionKey();
        if (captionKey != null) {
            tooltipComponents.add(Component.translatable(captionKey).withStyle(ChatFormatting.YELLOW));
        }
    }

    protected int cooldownTicks() {
        return 0;
    }

    protected boolean isFoilByDefault() {
        return false;
    }

    @Nullable
    protected String captionKey() {
        return null;
    }

    protected boolean cast(ServerLevel level, Player player, InteractionHand usedHand, ItemStack stack) {
        return false;
    }
}
