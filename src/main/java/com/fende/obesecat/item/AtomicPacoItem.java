package com.fende.obesecat.item;

import com.fende.obesecat.world.TacticalNuclearExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
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
        BlockPos targetPos = null;
        if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHit) {
            targetPos = blockHit.getBlockPos();
        } else {
            LivingEntity target = findTarget(player);
            if (target != null) {
                targetPos = target.blockPosition();
            } else if (hitResult.getType() != HitResult.Type.MISS) {
                targetPos = BlockPos.containing(hitResult.getLocation());
            }
        }

        if (targetPos == null) {
            return;
        }

        TacticalNuclearExplosion.detonateDelayed(serverLevel, targetPos, BARK_TO_EXPLOSION_DELAY_TICKS);
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
        return "item.obesecat.atomic_paco.caption";
    }

    @Override
    public boolean isFoil(net.minecraft.world.item.ItemStack stack) {
        return true;
    }
}
