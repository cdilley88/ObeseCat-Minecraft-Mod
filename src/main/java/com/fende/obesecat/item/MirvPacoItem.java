package com.fende.obesecat.item;

import com.fende.obesecat.world.TacticalNuclearExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MirvPacoItem extends PacoItem {
    private static final double EXPLOSION_RANGE = 512.0D;
    private static final int BARK_TO_EXPLOSION_DELAY_TICKS = 20; // 1 second dramatic suspense
    private static final int COOLDOWN_TICKS = 120; // 6 seconds

    public MirvPacoItem(Properties properties) {
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

        TacticalNuclearExplosion.detonateMirv(serverLevel, targetPos, BARK_TO_EXPLOSION_DELAY_TICKS);
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
    protected String getCaptionKey() {
        return "item.obesecat.mirv_paco.caption";
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
