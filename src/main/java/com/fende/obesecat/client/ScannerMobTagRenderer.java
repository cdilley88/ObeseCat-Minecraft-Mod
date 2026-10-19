package com.fende.obesecat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.joml.Matrix4f;

public final class ScannerMobTagRenderer {
    private ScannerMobTagRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        if (!ScannerOverlay.isActive()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (entity == player || !entity.isAlive() || entity.isInvisibleTo(player)) {
            return;
        }

        double distSq = player.distanceToSqr(entity);
        if (distSq > 4096.0D) { // 64 blocks detection radius
            return;
        }

        int dist = (int) Math.sqrt(distSq);
        float hp = entity.getHealth();
        float maxHp = entity.getMaxHealth();
        int armor = entity.getArmorValue();
        String name = entity.getDisplayName().getString();
        boolean isHostile = entity instanceof Enemy;
        boolean isTargeted = (entity == ScannerOverlay.getCrosshairTarget());

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        Font font = mc.font;

        poseStack.pushPose();
        // Position tag slightly above entity collision box / head
        float yOffset = entity.getBbHeight() + 0.55F;
        poseStack.translate(0.0F, yOffset, 0.0F);
        // Rotate to face player camera orientation
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());

        // Dynamic distance scaling: keeps the tag crisp & readable at 50+ blocks!
        float scale = Math.max(0.025F, dist * 0.0028F);
        poseStack.scale(-scale, -scale, scale);

        Matrix4f matrix4f = poseStack.last().pose();

        // High-tech Cyan Mob Tag
        String prefix = isHostile ? "§c[!] " : "§b[✦] ";
        String line1 = prefix + "§f" + name + " §7[" + dist + "m]";

        // Line 2: ♥ 20/20  🛡 2
        String hpColor = hp > (maxHp * 0.5F) ? "§a" : (hp > (maxHp * 0.25F) ? "§e" : "§c");
        String line2 = "§c♥ " + hpColor + String.format("%.0f", hp) + "§7/§a" + String.format("%.0f", maxHp);
        if (armor > 0) {
            line2 += "  §b🛡 " + armor;
        }

        int w1 = font.width(line1);
        int w2 = font.width(line2);
        float x1 = -w1 / 2.0F;
        float x2 = -w2 / 2.0F;

        // Dark translucent cyan background (or red tint for hostiles)
        int bgColor;
        if (isTargeted) {
            bgColor = isHostile ? 0xCC450000 : 0xCC003844;
        } else {
            bgColor = isHostile ? 0x99250505 : 0x99001A24;
        }
        int packedLight = 0xF000F0; // Full bright phosphor glow so it's readable anywhere

        // If actively targeted by crosshair, draw "TARGET LOCKED" header
        if (isTargeted) {
            String lockTag = isHostile ? "§c▶ TARGET LOCKED ◀" : "§b▶ TARGET LOCKED ◀";
            int w0 = font.width(lockTag);
            float x0 = -w0 / 2.0F;
            font.drawInBatch(lockTag, x0, -22.0F, 0xFFFFFFFF, false, matrix4f, buffer, Font.DisplayMode.SEE_THROUGH, bgColor, packedLight);
            font.drawInBatch(lockTag, x0, -22.0F, 0xFFFFFFFF, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);
        }

        // Draw Line 1 (Name & Distance) with see-through and normal foreground passes
        font.drawInBatch(line1, x1, -11.0F, 0xFFFFFFFF, false, matrix4f, buffer, Font.DisplayMode.SEE_THROUGH, bgColor, packedLight);
        font.drawInBatch(line1, x1, -11.0F, 0xFFFFFFFF, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);

        // Draw Line 2 (Health & Armor)
        font.drawInBatch(line2, x2, 1.0F, 0xFF00E5FF, false, matrix4f, buffer, Font.DisplayMode.SEE_THROUGH, bgColor, packedLight);
        font.drawInBatch(line2, x2, 1.0F, 0xFF00E5FF, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.popPose();
    }
}
