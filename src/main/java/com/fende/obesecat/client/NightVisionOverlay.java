package com.fende.obesecat.client;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class NightVisionOverlay {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "night_vision");
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

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        guiGraphics.fill(0, 0, width, height, 0x3A00FF44);
        guiGraphics.fill(0, 0, width, Math.max(1, height / 9), 0x22001100);
        guiGraphics.fill(0, height - Math.max(1, height / 9), width, height, 0x22001100);
        guiGraphics.fill(0, 0, Math.max(1, width / 12), height, 0x22001100);
        guiGraphics.fill(width - Math.max(1, width / 12), 0, width, height, 0x22001100);

        for (int y = 0; y < height; y += 4) {
            guiGraphics.fill(0, y, width, y + 1, 0x2400FF3A);
        }
        for (int y = 2; y < height; y += 8) {
            guiGraphics.fill(0, y, width, y + 1, 0x16001406);
        }
    }
}
