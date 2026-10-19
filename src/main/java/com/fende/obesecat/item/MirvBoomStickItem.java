package com.fende.obesecat.item;

import com.fende.obesecat.world.TacticalNuclearExplosion;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Dev stick for MIRV tactical nuclear strikes.
 * Calls down 7 tactical sub-kiloton Davy Crockett detonations scattered in an area.
 */
public class MirvBoomStickItem extends Item {
    private static final double MAX_RAYCAST_RANGE = 512.0D;
    private static final int COOLDOWN_TICKS = 120; // 6 seconds

    public MirvBoomStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        BlockPos targetPos = resolveTargetPosition(level, player);

        if (level instanceof ServerLevel serverLevel) {
            TacticalNuclearExplosion.detonateMirv(serverLevel, targetPos);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private BlockPos resolveTargetPosition(Level level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 viewVec = player.getViewVector(1.0F);
        Vec3 traceEnd = eyePos.add(viewVec.scale(MAX_RAYCAST_RANGE));

        BlockHitResult hitResult = level.clip(new ClipContext(
                eyePos,
                traceEnd,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return hitResult.getBlockPos();
        }

        int targetX = (int) Math.floor(traceEnd.x);
        int targetZ = (int) Math.floor(traceEnd.z);
        int surfaceY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(targetX, 0, targetZ)).getY();
        return new BlockPos(targetX, Math.max(level.getMinBuildHeight() + 5, surfaceY), targetZ);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("[DEV STICK: MIRV CLUSTER]")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("Payload: 7x Tactical Nuclear Warheads")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("Pattern: Multi-Warhead Staggered Carpet Bombardment")
                .withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("\"Seven suns shall rise upon this valley.\"")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltipComponents.add(Component.literal("Right-click targeted block (up to 512m). Clear radius: > 60m.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
