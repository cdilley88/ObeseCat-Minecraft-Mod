package com.fende.obesecat.item;

import com.fende.obesecat.world.PacoBarkBurst;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class AssaultPacoItem extends AttackPacoItem {
    public AssaultPacoItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void applyBarkEffect(Player player) {
        super.applyBarkEffect(player);
        if (player.level() instanceof ServerLevel level) {
            PacoBarkBurst.schedule(level, player.getX(), player.getY(), player.getZ(), 3);
            PacoBarkBurst.schedule(level, player.getX(), player.getY(), player.getZ(), 6);
        }
    }

    @Override
    protected float getAttackDamage() {
        return ATTACK_DAMAGE * 2.0F;
    }

    @Override
    protected String getCaptionKey() {
        return "item.obesecat.assault_paco.caption";
    }
}
