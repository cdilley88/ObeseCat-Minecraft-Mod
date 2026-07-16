package com.fende.obesecat.item;

import com.fende.obesecat.world.HolyExplosionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class HolyExplosionSwordItem extends SkillSwordItem {
    public HolyExplosionSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    protected int cooldownTicks() {
        return HolyExplosionManager.COOLDOWN_TICKS;
    }

    @Override
    protected boolean isFoilByDefault() {
        return true;
    }

    @Override
    protected String skillClassKey() {
        return "item.obesecat.skill_class.holy_sword";
    }

    @Override
    protected String captionKey() {
        return "item.obesecat.holy_explosion.caption";
    }

    @Override
    protected boolean canCastClient(Player player, InteractionHand usedHand, ItemStack stack) {
        return findBlockTarget(player) != null;
    }

    @Override
    protected boolean cast(ServerLevel level, Player player, InteractionHand usedHand, ItemStack stack) {
        BlockHitResult blockHit = findBlockTarget(player);
        if (blockHit == null) {
            return false;
        }

        HolyExplosionManager.schedule(level, blockHit.getBlockPos());
        return true;
    }

    private static BlockHitResult findBlockTarget(Player player) {
        HitResult hitResult = player.pick(HolyExplosionManager.RANGE, 1.0F, false);
        if (hitResult.getType() != HitResult.Type.BLOCK || !(hitResult instanceof BlockHitResult blockHit)) {
            return null;
        }
        return blockHit;
    }
}

