package com.fende.obesecat.client;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public final class IonStormOverlay {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "ion_storm");
    private static long expiresAt = -1L;
    private IonStormOverlay() {}

    public static void setActive(boolean active, int remainingTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!active) { expiresAt = -1L; return; }
        long now = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        expiresAt = now + Math.max(2, remainingTicks);
    }

    public static void suppressSkyFlash(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && expiresAt >= minecraft.level.getGameTime()) {
            minecraft.level.setSkyFlashTime(0);
        }
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        long now = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        if (expiresAt < now) return;
        int width = graphics.guiWidth(), height = graphics.guiHeight();
        float pulse = 0.5F + 0.5F * (float)Math.sin(now * 0.38D);
        int washAlpha = 30 + (int)(pulse * 8.0F);
        graphics.fill(0, 0, width, height, washAlpha << 24 | 0x5C0808);
        int edgeAlpha = 46 + (int)(pulse * 10.0F);
        int edge = Math.max(12, width / 18);
        graphics.fill(0, 0, width, Math.max(8, height / 14), edgeAlpha << 24 | 0x2B0000);
        graphics.fill(0, height - Math.max(8, height / 12), width, height, edgeAlpha << 24 | 0x2B0000);
        graphics.fill(0, 0, edge, height, edgeAlpha << 24 | 0x2B0000);
        graphics.fill(width - edge, 0, width, height, edgeAlpha << 24 | 0x2B0000);
        int seed = (int)(now * 1103515245L + 12345L);
        for (int i = 0; i < 3; i++) {
            seed = seed * 1664525 + 1013904223;
            int y = Math.floorMod(seed, Math.max(1, height));
            seed = seed * 1664525 + 1013904223;
            int x = Math.floorMod(seed, Math.max(1, width));
            int length = 18 + Math.floorMod(seed >>> 8, Math.max(19, width / 4));
            int alpha = 10 + Math.floorMod(seed >>> 16, 16);
            graphics.fill(x, y, Math.min(width, x + length), Math.min(height, y + 1), alpha << 24 | 0xC95745);
        }
    }
}
