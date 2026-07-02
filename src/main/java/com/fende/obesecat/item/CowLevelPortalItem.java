package com.fende.obesecat.item;

import com.fende.obesecat.world.CowLevelPortalManager;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class CowLevelPortalItem extends Item {
    private static final String CAPTION_KEY = "item.obesecat.cow_level_portal.caption";

    public CowLevelPortalItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable(CAPTION_KEY).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.dimension().equals(Level.OVERWORLD)) {
            if (player instanceof ServerPlayer serverPlayer) {
                CowLevelPortalManager.teleportToSecretCowLevel(serverPlayer);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (level.dimension().equals(CowLevelPortalManager.SECRET_COW_LEVEL)) {
            if (player instanceof ServerPlayer serverPlayer) {
                CowLevelPortalManager.returnFromSecretCowLevel(serverPlayer);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.pass(stack);
    }
}
