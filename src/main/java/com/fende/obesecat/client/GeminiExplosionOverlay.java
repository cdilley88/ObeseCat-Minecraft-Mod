package com.fende.obesecat.client;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Random;

public final class GeminiExplosionOverlay {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "gemini_nuclear_overlay");

    private static final int SILENCE_TICKS = 12;
    private static final int INITIAL_FLASH_TICKS = 24;
    private static final int THERMAL_PEAK_TICKS = 70;
    private static final int FALLOUT_TINT_TICKS = 220;

    private static final Random RANDOM = new Random();

    private static long startTick = -1L;
    private static BlockPos blastOrigin = BlockPos.ZERO;
    private static float blastIntensity = 1.0F;
    private static boolean soundsPlayed = false;

    private GeminiExplosionOverlay() {
    }

    public static void trigger(BlockPos origin, int durationTicks, float intensity) {
        Minecraft minecraft = Minecraft.getInstance();
        startTick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        blastOrigin = origin;
        blastIntensity = Mth.clamp(intensity, 0.4F, 1.0F);
        soundsPlayed = false;
        minecraft.getSoundManager().pause();
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (startTick < 0L) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        long elapsed = minecraft.level.getGameTime() - startTick;

        // Sound unpause & sonic shockwave at arrival tick
        if (!soundsPlayed && elapsed >= SILENCE_TICKS) {
            playLayeredBlastSounds(minecraft);
            soundsPlayed = true;
        }

        if (elapsed > FALLOUT_TINT_TICKS) {
            if (!soundsPlayed) {
                minecraft.getSoundManager().resume();
            }
            startTick = -1L;
            return;
        }

        // Camera tremor / screen shake during supersonic shockwave arrival (ticks 12 to 55)
        if (elapsed >= SILENCE_TICKS && elapsed <= 55) {
            LocalPlayer player = minecraft.player;
            double distSq = player.distanceToSqr(blastOrigin.getX() + 0.5D, blastOrigin.getY() + 0.5D, blastOrigin.getZ() + 0.5D);
            double dist = Math.sqrt(distSq);

            if (dist < 320.0D) {
                float distFactor = (float) Math.max(0.0D, 1.0D - (dist / 320.0D));
                float shakeProgress = 1.0F - (float) (elapsed - SILENCE_TICKS) / 43.0F;
                float shake = distFactor * shakeProgress * blastIntensity * 2.5F;

                if (shake > 0.05F) {
                    float pitchJitter = (RANDOM.nextFloat() - 0.5F) * shake;
                    float yawJitter = (RANDOM.nextFloat() - 0.5F) * shake;
                    player.setXRot(player.getXRot() + pitchJitter);
                    player.setYRot(player.getYRot() + yawJitter);
                }
            }
        }
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (startTick < 0L) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        long elapsed = minecraft.level == null ? 0L : minecraft.level.getGameTime() - startTick;
        if (elapsed < 0L || elapsed > FALLOUT_TINT_TICKS) {
            return;
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        if (elapsed < INITIAL_FLASH_TICKS) {
            // Stage 1: Intense Prompt Ionization & Solar White Flash
            float flashProg = (float) elapsed / (float) INITIAL_FLASH_TICKS;
            // Starts ultra-bright cyan, holds brilliant white
            float alpha = blastIntensity * (1.0F - flashProg * 0.15F);
            int alphaByte = Mth.clamp((int) (alpha * 255.0F), 0, 255);

            // Subtle cyan tint on the first few ticks, then incandescent white
            int rgb = elapsed < 4 ? 0xB5F0FF : 0xFFFFFF;
            guiGraphics.fill(0, 0, width, height, (alphaByte << 24) | rgb);

            // Secondary chromatic scanline bursts
            renderIonizationScanlines(guiGraphics, width, height, elapsed, alpha);
        } else if (elapsed < THERMAL_PEAK_TICKS) {
            // Stage 2: Thermonuclear Thermal Flare & Burning Radiance
            float progress = (float) (elapsed - INITIAL_FLASH_TICKS) / (float) (THERMAL_PEAK_TICKS - INITIAL_FLASH_TICKS);
            float alpha = blastIntensity * (1.0F - progress);

            // Deep incandescent solar orange / fiery plasma wash
            int washAlpha = Mth.clamp((int) (alpha * 160.0F), 0, 160);
            guiGraphics.fill(0, 0, width, height, (washAlpha << 24) | 0xFF7A18);

            // Glowing hot vignette borders
            renderThermalVignette(guiGraphics, width, height, alpha);
        } else {
            // Stage 3: Lingering Atmospheric Fallout / Radiation Haze
            float progress = (float) (elapsed - THERMAL_PEAK_TICKS) / (float) (FALLOUT_TINT_TICKS - THERMAL_PEAK_TICKS);
            float alpha = blastIntensity * (1.0F - progress);

            int hazeAlpha = Mth.clamp((int) (alpha * 36.0F), 0, 36);
            guiGraphics.fill(0, 0, width, height, (hazeAlpha << 24) | 0x3D1A10);

            // Subtle dark smoky borders
            int borderAlpha = Mth.clamp((int) (alpha * 48.0F), 0, 48);
            guiGraphics.fill(0, 0, width, height / 14, (borderAlpha << 24) | 0x140C0A);
            guiGraphics.fill(0, height - height / 14, width, height, (borderAlpha << 24) | 0x140C0A);
            guiGraphics.fill(0, 0, width / 18, height, (borderAlpha << 24) | 0x140C0A);
            guiGraphics.fill(width - width / 18, 0, width, height, (borderAlpha << 24) | 0x140C0A);
        }
    }

    private static void renderThermalVignette(GuiGraphics guiGraphics, int width, int height, float alpha) {
        int fieryAlpha = Mth.clamp((int) (alpha * 85.0F), 0, 85);
        int emberAlpha = Mth.clamp((int) (alpha * 65.0F), 0, 65);

        // Top and bottom glowing heatwaves
        guiGraphics.fill(0, 0, width, height / 8, (fieryAlpha << 24) | 0xFF3500);
        guiGraphics.fill(0, height - height / 6, width, height, (fieryAlpha << 24) | 0xFF4500);
        guiGraphics.fill(0, height - height / 12, width, height, (emberAlpha << 24) | 0xFFBF15);

        // Left and right edges
        guiGraphics.fill(0, 0, width / 10, height, (fieryAlpha << 24) | 0xDD2800);
        guiGraphics.fill(width - width / 10, 0, width, height, (fieryAlpha << 24) | 0xDD2800);
    }

    private static void renderIonizationScanlines(GuiGraphics guiGraphics, int width, int height, long elapsed, float alpha) {
        int scanAlpha = Mth.clamp((int) (alpha * 40.0F), 0, 40);
        int seed = (int) (elapsed * 1234567L + 98765L);
        for (int i = 0; i < 4; i++) {
            seed = seed * 1103515245 + 12345;
            int y = Math.floorMod(seed, Math.max(1, height));
            seed = seed * 1103515245 + 12345;
            int h = 1 + Math.floorMod(seed >>> 16, 3);
            guiGraphics.fill(0, y, width, Math.min(height, y + h), (scanAlpha << 24) | 0x55E5FF);
        }
    }

    private static void playLayeredBlastSounds(Minecraft minecraft) {
        minecraft.getSoundManager().resume();

        // 1. Immediate deep sub-bass atmospheric rip (Warden sonic boom + low pitch generic explode)
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE.value(), 0.42F, 3.5F));
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WARDEN_SONIC_BOOM, 0.65F, 2.5F));
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.75F, 2.0F));

        // 2. Delayed secondary blast wave echo (4 ticks)
        minecraft.getSoundManager().playDelayed(SimpleSoundInstance.forUI(SoundEvents.DRAGON_FIREBALL_EXPLODE, 0.55F, 2.5F), 4);
        minecraft.getSoundManager().playDelayed(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE.value(), 0.68F, 2.2F), 6);

        // 3. Shockwave ground upheaval rumble (10 ticks)
        minecraft.getSoundManager().playDelayed(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE.value(), 0.92F, 2.0F), 10);
        minecraft.getSoundManager().playDelayed(SimpleSoundInstance.forUI(SoundEvents.LIGHTNING_BOLT_IMPACT, 0.60F, 1.8F), 14);

        // 4. Distant stratospheric roar & atmospheric reverberation (22 ticks)
        minecraft.getSoundManager().playDelayed(SimpleSoundInstance.forUI(SoundEvents.WITHER_DEATH, 0.38F, 1.5F), 22);
    }
}
