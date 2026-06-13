package com.fende.obesecat.item;

import com.fende.obesecat.world.AtomicFireSphere;
import com.fende.obesecat.world.NuclearCatExplosion;
import java.util.List;
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

public class BigFireBoomStickItem extends Item {
    private static final double RANGE = 256.0D;
    private static final int COOLDOWN_TICKS = AtomicFireSphere.LIFETIME_TICKS;

    public BigFireBoomStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        HitResult hitResult = player.pick(RANGE, 1.0F, false);
        if (hitResult.getType() != HitResult.Type.BLOCK || !(hitResult instanceof BlockHitResult blockHit)) {
            return InteractionResultHolder.pass(stack);
        }

        if (level instanceof ServerLevel serverLevel) {
            NuclearCatExplosion.flashThenDetonate(
                    serverLevel,
                    blockHit.getBlockPos(),
                    NuclearCatExplosion.LITHIUM_CRATER_RADIUS * 2,
                    NuclearCatExplosion.LITHIUM_CRATER_MAX_DEPTH * 2
            );
            AtomicFireSphere.createDelayed(
                    serverLevel,
                    blockHit.getBlockPos(),
                    NuclearCatExplosion.FLASH_TO_CRATER_DELAY_TICKS + 2,
                    AtomicFireSphere.BIG_RADIUS
            );
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.obesecat.big_fire_boom_stick.caption").withStyle(ChatFormatting.YELLOW));
    }
}
