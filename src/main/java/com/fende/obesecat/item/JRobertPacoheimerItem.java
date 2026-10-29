package com.fende.obesecat.item;

import com.fende.obesecat.world.FatManPlutoniumExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class JRobertPacoheimerItem extends PacoItem {
    private static final double EXPLOSION_RANGE = 512.0D;
    private static final int BARK_TO_EXPLOSION_DELAY_TICKS = 20; // 1 second dramatic suspense
    private static final int COOLDOWN_TICKS = 100; // 5 seconds

    public JRobertPacoheimerItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void applyBarkEffect(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos targetPos = resolveTargetPosition(player, EXPLOSION_RANGE);
        if (targetPos == null) {
            return;
        }

        // Standard Fat Man plutonium implosion effect minus the central Trinitite deposit, with dramatic suspense delay
        FatManPlutoniumExplosion.detonateDelayed(serverLevel, targetPos, BARK_TO_EXPLOSION_DELAY_TICKS, false);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
    }

    @Override
    protected double getRange() {
        return EXPLOSION_RANGE;
    }

    @Override
    protected int getBarkCooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    protected int getStinkCooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    protected String getCaptionKey() {
        return "item.obesecat.j_robert_pacoheimer.caption";
    }

    @Override
    public boolean isFoil(net.minecraft.world.item.ItemStack stack) {
        return true;
    }
}
