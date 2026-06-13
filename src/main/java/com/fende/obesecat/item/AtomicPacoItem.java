package com.fende.obesecat.item;

import com.fende.obesecat.world.NuclearCatExplosion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class AtomicPacoItem extends PacoItem {
    private static final double EXPLOSION_RANGE = 256.0D;
    private static final int BARK_TO_EXPLOSION_DELAY_TICKS = 20;
    private static final int COOLDOWN_TICKS = 120;

    public AtomicPacoItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void applyBarkEffect(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        HitResult hitResult = player.pick(EXPLOSION_RANGE, 1.0F, false);
        if (hitResult.getType() != HitResult.Type.BLOCK || !(hitResult instanceof BlockHitResult blockHit)) {
            return;
        }

        NuclearCatExplosion.flashThenDetonateDelayed(
                serverLevel,
                blockHit.getBlockPos(),
                NuclearCatExplosion.PLUTONIUM_CRATER_RADIUS,
                NuclearCatExplosion.PLUTONIUM_CRATER_MAX_DEPTH,
                BARK_TO_EXPLOSION_DELAY_TICKS
        );
    }

    @Override
    protected int getBarkCooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    public boolean isFoil(net.minecraft.world.item.ItemStack stack) {
        return true;
    }
}
