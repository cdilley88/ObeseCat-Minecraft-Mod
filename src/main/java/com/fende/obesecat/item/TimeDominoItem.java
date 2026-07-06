package com.fende.obesecat.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TimeDominoItem extends DominoItem {
    private final long targetTime;

    public TimeDominoItem(Properties properties, long targetTime) {
        super(properties);
        this.targetTime = targetTime;
    }

    @Override
    protected void applyBarkEffect(Player player) {
        if (!player.level().isClientSide() && player.level() instanceof ServerLevel serverLevel) {
            serverLevel.setDayTime(this.targetTime);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
