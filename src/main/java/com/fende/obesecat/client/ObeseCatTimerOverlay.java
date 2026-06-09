package com.fende.obesecat.client;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.entity.ObeseCat;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public final class ObeseCatTimerOverlay {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "fat_man_countdown");

    private ObeseCatTimerOverlay() {
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui) {
            return;
        }

        ObeseCat tickingCat = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof ObeseCat obeseCat && obeseCat.getExplosionCountdownTicks() > 0) {
                double distance = minecraft.player.distanceToSqr(obeseCat);
                if (distance < nearestDistance && distance <= 4096.0D) {
                    tickingCat = obeseCat;
                    nearestDistance = distance;
                }
            }
        }

        if (tickingCat == null) {
            return;
        }

        String text = "Fat Man critical: " + tickingCat.getExplosionCountdownSeconds();
        int x = (guiGraphics.guiWidth() - minecraft.font.width(text)) / 2;
        int y = guiGraphics.guiHeight() / 4;
        guiGraphics.drawString(minecraft.font, text, x, y, 0xFFFF5533, true);
    }
}
