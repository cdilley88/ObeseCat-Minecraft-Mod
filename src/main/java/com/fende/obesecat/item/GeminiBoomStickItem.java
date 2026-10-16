package com.fende.obesecat.item;

import com.fende.obesecat.world.GeminiThermonuclearExplosion;
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

public class GeminiBoomStickItem extends Item {
    private static final double MAX_RAYCAST_RANGE = 384.0D;
    private static final int COOLDOWN_TICKS = 120; // 6 seconds

    public GeminiBoomStickItem(Properties properties) {
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
            GeminiThermonuclearExplosion.detonate(serverLevel, targetPos, player);
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

        // If player aimed into the horizon/sky, strike the terrain surface 192 blocks out in that direction
        Vec3 distantTarget = eyePos.add(viewVec.scale(192.0D));
        int targetX = (int) Math.floor(distantTarget.x);
        int targetZ = (int) Math.floor(distantTarget.z);
        int surfaceY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(targetX, (int) eyePos.y, targetZ)).getY();
        return new BlockPos(targetX, surfaceY, targetZ);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.obesecat.gemini_boom_stick.caption").withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("item.obesecat.gemini_boom_stick.desc").withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.translatable("item.obesecat.gemini_boom_stick.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
