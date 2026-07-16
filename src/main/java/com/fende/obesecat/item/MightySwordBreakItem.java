package com.fende.obesecat.item;

import com.fende.obesecat.world.MightySwordBreakManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class MightySwordBreakItem extends SkillSwordItem {
    private final MightySwordBreakManager.Skill skill;
    private final String captionKey;

    public MightySwordBreakItem(Properties properties, MightySwordBreakManager.Skill skill, String captionKey) {
        super(properties);
        this.skill = skill;
        this.captionKey = captionKey;
    }

    @Override
    protected int cooldownTicks() {
        return MightySwordBreakManager.COOLDOWN_TICKS;
    }

    @Override
    protected boolean isFoilByDefault() {
        return true;
    }

    @Override
    protected String skillClassKey() {
        return "item.obesecat.skill_class.mighty_sword";
    }

    @Override
    protected String captionKey() {
        return captionKey;
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
        MightySwordBreakManager.schedule(level, blockHit.getBlockPos(), skill);
        return true;
    }

    private static BlockHitResult findBlockTarget(Player player) {
        HitResult hitResult = player.pick(MightySwordBreakManager.RANGE, 1.0F, false);
        return hitResult instanceof BlockHitResult blockHit && hitResult.getType() == HitResult.Type.BLOCK
                ? blockHit
                : null;
    }
}

