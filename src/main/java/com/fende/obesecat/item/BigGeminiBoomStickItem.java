package com.fende.obesecat.item;

import com.fende.obesecat.world.BigGeminiThermonuclearExplosion;
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

public class BigGeminiBoomStickItem extends Item {
    private static final double MAX_RAYCAST_RANGE = 512.0D;
    private static final int COOLDOWN_TICKS = 180; // 9 seconds

    public BigGeminiBoomStickItem(Properties properties) {
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
            BigGeminiThermonuclearExplosion.detonate(serverLevel, targetPos, player);
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
        tooltipComponents.add(Component.literal("[CLASS: THERMONUCLEAR EXPERIMENTAL]")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("Yield: 10.4 Megatons (Ivy Mike Class)")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("\"Trinity was just the spark. This is the sun.\"")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
        tooltipComponents.add(Component.literal("Right-click block within 512m to initiate thermonuclear ignition.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
