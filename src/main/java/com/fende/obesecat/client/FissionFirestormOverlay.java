package com.fende.obesecat.client;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class FissionFirestormOverlay {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "fission_firestorm");
    private static final int WHITE_FLASH_WAIT_TICKS = 58;
    private static final int FADE_IN_TICKS = 22;
    private static final int FADE_OUT_TICKS = 90;

    private static long startTick = -1L;
    private static int holdTicks = 0;

    private FissionFirestormOverlay() {
    }

    public static void trigger(int fireballHoldTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        startTick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        holdTicks = Math.max(20, fireballHoldTicks);
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (startTick < 0L) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        long elapsed = minecraft.level == null ? WHITE_FLASH_WAIT_TICKS : minecraft.level.getGameTime() - startTick;
        if (elapsed < WHITE_FLASH_WAIT_TICKS) {
            return;
        }

        int effectTick = (int) (elapsed - WHITE_FLASH_WAIT_TICKS);
        int totalTicks = FADE_IN_TICKS + holdTicks + FADE_OUT_TICKS;
        if (effectTick >= totalTicks) {
            startTick = -1L;
            return;
        }

        float alpha;
        if (effectTick < FADE_IN_TICKS) {
            alpha = (float) effectTick / (float) FADE_IN_TICKS;
        } else if (effectTick < FADE_IN_TICKS + holdTicks) {
            alpha = 1.0F;
        } else {
            float fadeProgress = (float) (effectTick - FADE_IN_TICKS - holdTicks) / (float) FADE_OUT_TICKS;
            alpha = 1.0F - Mth.clamp(fadeProgress, 0.0F, 1.0F);
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        int redAlpha = Mth.clamp((int) (alpha * 96.0F), 0, 96);
        guiGraphics.fill(0, 0, width, height, redAlpha << 24 | 0xBB1200);

        renderBurningVignette(guiGraphics, width, height, alpha);
    }

    private static void renderBurningVignette(GuiGraphics guiGraphics, int width, int height, float alpha) {
        int burnAlpha = Mth.clamp((int) (alpha * 38.0F), 0, 38);
        int emberAlpha = Mth.clamp((int) (alpha * 28.0F), 0, 28);
        int smokeAlpha = Mth.clamp((int) (alpha * 34.0F), 0, 34);

        guiGraphics.fill(0, 0, width, height / 10, smokeAlpha << 24 | 0x170908);
        guiGraphics.fill(0, height - height / 4, width, height, burnAlpha << 24 | 0xFF4A00);
        guiGraphics.fill(0, height - height / 8, width, height, emberAlpha << 24 | 0xFFC233);
        guiGraphics.fill(0, 0, width / 10, height, burnAlpha << 24 | 0xCC2500);
        guiGraphics.fill(width - width / 10, 0, width, height, burnAlpha << 24 | 0xCC2500);
    }
}
