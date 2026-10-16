package com.fende.obesecat.client;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;

public final class NightVisionOverlay {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "night_vision");
    private static final ResourceLocation VISOR_OVERLAY = ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "textures/gui/night_vision_goggles.png");
    private static boolean active = false;

    private NightVisionOverlay() {
    }

    public static void setActive(boolean enabled) {
        active = enabled;
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!active) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui) {
            return;
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        // 1. High-resolution panoramic visor mask (wide viewing port, hotbar open at bottom)
        guiGraphics.blit(VISOR_OVERLAY, 0, 0, 0.0F, 0.0F, width, height, width, height);

        // 2. Subtle CRT / Phosphor screen tint & fine horizontal scanlines
        guiGraphics.fill(0, 0, width, height, 0x0E00FF40);
        for (int y = 0; y < height; y += 4) {
            guiGraphics.fill(0, y, width, y + 1, 0x0700FF33);
        }

        // 3. Center Aiming Reticle (Subtle corner brackets around crosshair)
        renderCenterReticle(guiGraphics, width / 2, height / 2);

        // 4. Top-Left HUD Telemetry & Power Gauge (Stacked to keep top-right minimap clear!)
        int startX = 24;
        int startY = 16;
        guiGraphics.drawString(minecraft.font, "§a[✦] DORITO-NVG // GEN-3 VISOR", startX, startY, 0xFF00FF55, false);
        guiGraphics.drawString(minecraft.font, "§2IR FILTER: 850nm AUTO-GATE", startX, startY + 10, 0xFF00AA44, false);

        int foodLevel = minecraft.player.getFoodData().getFoodLevel();
        int pwrPercent = (foodLevel * 100) / 20;
        int pwrColor = foodLevel > 6 ? 0xFF00FF55 : (foodLevel > 2 ? 0xFFFFB000 : 0xFFFF3333);
        String bar = buildPwrBar(foodLevel);
        String pwrText = "PWR: " + pwrPercent + "% [" + bar + "]";
        if (foodLevel <= 4) {
            pwrText += " §c⚠ LOW BATTERY";
        }
        guiGraphics.drawString(minecraft.font, pwrText, startX, startY + 20, pwrColor, false);

        // 5. Bottom-Left LUX Sensor
        renderLightTelemetry(guiGraphics, minecraft, width, height);
    }

    private static void renderCenterReticle(GuiGraphics guiGraphics, int cx, int cy) {
        int reticleColor = 0x5500FF55;
        guiGraphics.fill(cx - 14, cy, cx - 6, cy + 1, reticleColor);
        guiGraphics.fill(cx + 6, cy, cx + 14, cy + 1, reticleColor);
        guiGraphics.fill(cx, cy - 14, cx + 1, cy - 6, reticleColor);
        guiGraphics.fill(cx, cy + 6, cx + 1, cy + 14, reticleColor);
        guiGraphics.fill(cx, cy, cx + 1, cy + 1, 0x8800FF55);
    }

    private static String buildPwrBar(int foodLevel) {
        StringBuilder sb = new StringBuilder();
        int filled = foodLevel / 2; // 0 to 10
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                sb.append("█");
            } else {
                sb.append("░");
            }
        }
        return sb.toString();
    }

    private static void renderLightTelemetry(GuiGraphics guiGraphics, Minecraft minecraft, int width, int height) {
        BlockPos pos = minecraft.player.blockPosition();
        int rawLight = minecraft.level.getMaxLocalRawBrightness(pos);
        int blockLight = minecraft.level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = minecraft.level.getBrightness(LightLayer.SKY, pos);

        // Bottom left LUX reading (sits neatly beside hotbar)
        String luxText = "LUX: " + rawLight + "/15 (BLK: " + blockLight + " | SKY: " + skyLight + ")";
        guiGraphics.drawString(minecraft.font, luxText, 24, height - 18, 0xFF00CC55, false);

        // Low Light Warning Banner (centered above hearts/food bar)
        if (rawLight <= 4) {
            boolean blink = (minecraft.level.getGameTime() / 10L) % 2L == 0L;
            if (blink) {
                String warn;
                if (rawLight == 0) {
                    warn = "⚠ CRITICAL: ZERO AMBIENT LIGHT // HOSTILE MOB SPAWN ACTIVE ⚠";
                } else {
                    warn = "⚠ LOW LIGHT WARNING: LUX " + rawLight + " // HOSTILE MOB THREAT ELEVATED ⚠";
                }

                int textWidth = minecraft.font.width(warn);
                int wx = (width - textWidth) / 2;
                int wy = height - 54;

                // Translucent warning banner with red border
                guiGraphics.fill(wx - 8, wy - 3, wx + textWidth + 8, wy + 11, 0x99280000);
                guiGraphics.fill(wx - 8, wy - 3, wx + textWidth + 8, wy - 2, 0xFFFF3333);
                guiGraphics.fill(wx - 8, wy + 10, wx + textWidth + 8, wy + 11, 0xFFFF3333);
                guiGraphics.drawString(minecraft.font, warn, wx, wy, 0xFFFF4444, true);
            }
        }
    }
}
