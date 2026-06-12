package com.fende.obesecat.client;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

public final class NuclearFlashOverlay {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "nuclear_flash");
    private static final int SILENCE_TICKS = 8;
    private static final int FULL_WHITE_TICKS = 55;
    private static final int TOTAL_TICKS = 130;

    private static long startTick = -1L;
    private static float intensity = 0.0F;
    private static boolean soundsPlayed = false;

    private NuclearFlashOverlay() {
    }

    public static void trigger(float flashIntensity) {
        Minecraft minecraft = Minecraft.getInstance();
        startTick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        intensity = Mth.clamp(flashIntensity, 0.35F, 1.0F);
        soundsPlayed = false;
        minecraft.getSoundManager().pause();
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (startTick < 0L) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        long elapsed = minecraft.level == null ? TOTAL_TICKS : minecraft.level.getGameTime() - startTick;
        if (!soundsPlayed && elapsed >= SILENCE_TICKS) {
            playDelayedBlastSounds(minecraft);
            soundsPlayed = true;
        }

        if (elapsed >= TOTAL_TICKS) {
            if (!soundsPlayed) {
                minecraft.getSoundManager().resume();
            }
            startTick = -1L;
            return;
        }

        float alpha = intensity;
        if (elapsed > FULL_WHITE_TICKS) {
            float fadeProgress = (float) (elapsed - FULL_WHITE_TICKS) / (float) (TOTAL_TICKS - FULL_WHITE_TICKS);
            alpha *= 1.0F - Mth.clamp(fadeProgress, 0.0F, 1.0F);
        }

        int alphaByte = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        int color = alphaByte << 24 | 0xFFFFFF;
        guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), color);
    }

    private static void playDelayedBlastSounds(Minecraft minecraft) {
        minecraft.getSoundManager().resume();
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE.value(), 0.72F, 2.0F));
        minecraft.getSoundManager().playDelayed(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE.value(), 0.88F, 2.0F), 4);
        minecraft.getSoundManager().playDelayed(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE.value(), 1.05F, 2.0F), 9);
    }
}
