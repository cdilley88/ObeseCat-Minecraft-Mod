package com.fende.obesecat.item;

import com.fende.obesecat.world.AtomicFireSphere;
import com.fende.obesecat.world.NuclearCatExplosion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class JRobertPacoheimerItem extends PacoItem {
    private static final double EXPLOSION_RANGE = 256.0D;
    private static final int BARK_TO_EXPLOSION_DELAY_TICKS = 20;

    public JRobertPacoheimerItem(Properties properties) {
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
                NuclearCatExplosion.LITHIUM_CRATER_RADIUS,
                NuclearCatExplosion.LITHIUM_CRATER_MAX_DEPTH,
                BARK_TO_EXPLOSION_DELAY_TICKS
        );
        AtomicFireSphere.createDelayed(serverLevel, blockHit.getBlockPos(), BARK_TO_EXPLOSION_DELAY_TICKS + NuclearCatExplosion.FLASH_TO_CRATER_DELAY_TICKS + 2);
    }

    @Override
    protected int getBarkCooldownTicks() {
        return AtomicFireSphere.LIFETIME_TICKS;
    }

    @Override
    protected int getStinkCooldownTicks() {
        return AtomicFireSphere.LIFETIME_TICKS;
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
