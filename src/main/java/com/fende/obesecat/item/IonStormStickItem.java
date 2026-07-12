package com.fende.obesecat.item;

import com.fende.obesecat.world.IonStormManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IonStormStickItem extends Item {
    public IonStormStickItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            IonStormManager.start(serverLevel, player.blockPosition());
            player.getCooldowns().addCooldown(this, IonStormManager.WARNING_DELAY_TICKS + IonStormManager.DURATION_TICKS);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
