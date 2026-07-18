package com.fende.obesecat.item;

import com.fende.obesecat.world.NightSwordManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class NightSwordItem extends SkillSwordItem {
    public NightSwordItem(Properties properties) { super(properties); }

    @Override protected int cooldownTicks() { return NightSwordManager.COOLDOWN_TICKS; }
    @Override protected boolean isFoilByDefault() { return true; }
    @Override protected String skillClassKey() { return "item.obesecat.skill_class.dark_sword"; }
    @Override protected String captionKey() { return "item.obesecat.night_sword.caption"; }
    @Override protected String effectKey() { return "item.obesecat.night_sword.effect"; }

    @Override
    protected boolean canCastClient(Player player, InteractionHand hand, ItemStack stack) {
        return findEntityTarget(player) != null || findBlockTarget(player) != null;
    }

    @Override
    protected boolean cast(ServerLevel level, Player player, InteractionHand hand, ItemStack stack) {
        LivingEntity entityTarget = findEntityTarget(player);
        if (entityTarget != null) {
            NightSwordManager.schedule(level, player, entityTarget);
            return true;
        }

        BlockHitResult blockTarget = findBlockTarget(player);
        if (blockTarget != null) {
            NightSwordManager.schedule(level, player, Vec3.atCenterOf(blockTarget.getBlockPos()));
            return true;
        }
        return false;
    }

    private static LivingEntity findEntityTarget(Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getViewVector(1.0F).scale(NightSwordManager.RANGE));
        HitResult blockHit = player.pick(NightSwordManager.RANGE, 1.0F, false);
        double maxDistance = blockHit.getType() == HitResult.Type.MISS
                ? NightSwordManager.RANGE * NightSwordManager.RANGE
                : blockHit.getLocation().distanceToSqr(start);
        AABB searchBox = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player, start, end, searchBox,
                entity -> entity instanceof LivingEntity && entity != player && entity.isAlive()
                        && entity.isPickable() && !entity.isSpectator(),
                maxDistance);
        return entityHit != null && entityHit.getEntity() instanceof LivingEntity living ? living : null;
    }

    private static BlockHitResult findBlockTarget(Player player) {
        HitResult hit = player.pick(NightSwordManager.RANGE, 1.0F, false);
        return hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit
                ? blockHit : null;
    }
}
