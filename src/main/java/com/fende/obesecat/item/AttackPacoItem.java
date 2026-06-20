package com.fende.obesecat.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class AttackPacoItem extends ConcussivePacoItem {
    private static final double ATTACK_RANGE = 20.0D;
    protected static final float ATTACK_DAMAGE = 2.0F;

    public AttackPacoItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void applyBarkEffect(Player player) {
        LivingEntity target = findTarget(player);
        if (target == null) {
            return;
        }

        applyKnockback(player, target);
        target.hurt(player.damageSources().playerAttack(player), getAttackDamage());
    }

    protected float getAttackDamage() {
        return ATTACK_DAMAGE;
    }

    @Override
    protected double getRange() {
        return ATTACK_RANGE;
    }

    @Override
    protected String getCaptionKey() {
        return "item.obesecat.attack_paco.caption";
    }
}
