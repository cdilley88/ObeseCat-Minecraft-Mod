package com.fende.obesecat.item;

import com.fende.obesecat.world.LightningStabManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class LightningStabSwordItem extends SkillSwordItem {
    public LightningStabSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    protected int cooldownTicks() {
        return LightningStabManager.COOLDOWN_TICKS;
    }

    @Override
    protected boolean isFoilByDefault() {
        return true;
    }

    @Override
    protected String captionKey() {
        return "item.obesecat.lightning_stab.caption";
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

        LightningStabManager.schedule(level, blockHit.getBlockPos());
        return true;
    }

    private static BlockHitResult findBlockTarget(Player player) {
        HitResult hitResult = player.pick(LightningStabManager.RANGE, 1.0F, false);
        if (hitResult.getType() != HitResult.Type.BLOCK || !(hitResult instanceof BlockHitResult blockHit)) {
            return null;
        }
        return blockHit;
    }
}
