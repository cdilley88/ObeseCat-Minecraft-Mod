package com.fende.obesecat.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class WeatherDominoItem extends DominoItem {
    private final boolean raining;
    private final boolean thundering;

    public WeatherDominoItem(Properties properties, boolean raining, boolean thundering) {
        super(properties);
        this.raining = raining;
        this.thundering = thundering;
    }

    @Override
    protected void applyBarkEffect(Player player) {
        if (!player.level().isClientSide() && player.level() instanceof ServerLevel serverLevel) {
            serverLevel.setWeatherParameters(0, 6000, this.raining, this.thundering);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
