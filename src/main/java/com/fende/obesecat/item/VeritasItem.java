package com.fende.obesecat.item;

import com.fende.obesecat.world.VeritasSummonManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public final class VeritasItem extends SummonItem {
    public VeritasItem(Properties properties) { super(properties); }

    @Override protected double range() { return VeritasSummonManager.RANGE; }
    @Override protected int cooldownTicks() { return VeritasSummonManager.COOLDOWN_TICKS; }
    @Override protected String captionKey() { return "item.obesecat.veritas.caption"; }

    @Override
    protected void beginSummon(ServerLevel level, Player player, BlockHitResult target) {
        VeritasSummonManager.schedule(level, player, target.getBlockPos());
    }
}
