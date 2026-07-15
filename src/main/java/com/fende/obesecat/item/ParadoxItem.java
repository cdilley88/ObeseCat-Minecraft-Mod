package com.fende.obesecat.item;

import com.fende.obesecat.world.ParadoxSummonManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

public final class ParadoxItem extends SummonItem {
    public ParadoxItem(Properties properties) {
        super(properties);
    }

    @Override protected double range() { return ParadoxSummonManager.RANGE; }
    @Override protected int cooldownTicks() { return ParadoxSummonManager.COOLDOWN_TICKS; }
    @Override protected String captionKey() { return "item.obesecat.paradox.caption"; }

    @Override
    protected void beginSummon(ServerLevel level, Player player, BlockHitResult target) {
        ParadoxSummonManager.schedule(level, player, target.getBlockPos());
    }
}
