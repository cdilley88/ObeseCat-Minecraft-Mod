package com.fende.obesecat.item;

import com.fende.obesecat.entity.TargetDummy;
import com.fende.obesecat.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class TargetDummyItem extends Item {
    public TargetDummyItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        TargetDummy dummy = ModEntities.TARGET_DUMMY.get().create(level);
        if (dummy == null) return InteractionResult.FAIL;
        var pos = context.getClickedPos().relative(context.getClickedFace());
        double x = pos.getX() + 0.5D;
        double z = pos.getZ() + 0.5D;
        float yaw = 0.0F;
        if (context.getPlayer() != null) {
            double dx = context.getPlayer().getX() - x;
            double dz = context.getPlayer().getZ() - z;
            yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
        }
        dummy.moveTo(x, pos.getY(), z, yaw, 0.0F);
        dummy.setYBodyRot(yaw);
        dummy.setYHeadRot(yaw);
        if (!level.addFreshEntity(dummy)) return InteractionResult.FAIL;
        context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}

