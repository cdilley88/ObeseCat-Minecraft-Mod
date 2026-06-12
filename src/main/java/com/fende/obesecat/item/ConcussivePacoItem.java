package com.fende.obesecat.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ConcussivePacoItem extends PacoItem {
    private static final double KNOCKBACK_STRENGTH = 1.6D;

    public ConcussivePacoItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void applyBarkEffect(Player player) {
        LivingEntity target = findTarget(player);
        if (target == null) {
            return;
        }

        applyKnockback(player, target);
    }

    protected void applyKnockback(Player player, LivingEntity target) {
        double x = player.getX() - target.getX();
        double z = player.getZ() - target.getZ();
        target.knockback(KNOCKBACK_STRENGTH, x, z);
    }

    @Override
    protected boolean usesStinkMeter() {
        return true;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
