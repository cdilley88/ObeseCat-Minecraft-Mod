package com.fende.obesecat.item;

import com.fende.obesecat.world.PraxisSummonManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public final class PraxisItem extends SummonItem {
    public PraxisItem(Properties properties) {
        super(properties);
    }

    @Override protected double range() { return PraxisSummonManager.RANGE; }
    @Override protected int cooldownTicks() { return PraxisSummonManager.COOLDOWN_TICKS; }
    @Override protected String captionKey() { return "item.obesecat.praxis.caption"; }

    @Override
    protected void beginSummon(ServerLevel level, Player player, BlockHitResult target) {
        PraxisSummonManager.schedule(level, player, target.getBlockPos());
    }
}
