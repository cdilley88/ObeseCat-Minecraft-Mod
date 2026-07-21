package com.fende.obesecat.item;

import com.fende.obesecat.world.AequitasSummonManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public final class AequitasItem extends SummonItem {
    public AequitasItem(Properties properties) {
        super(properties);
    }

    @Override protected double range() { return AequitasSummonManager.RANGE; }
    @Override protected int cooldownTicks() { return AequitasSummonManager.COOLDOWN_TICKS; }
    @Override protected String captionKey() { return "item.obesecat.aequitas.caption"; }

    @Override
    protected void beginSummon(ServerLevel level, Player player, BlockHitResult target) {
        AequitasSummonManager.schedule(level, player, target.getBlockPos());
    }
}
