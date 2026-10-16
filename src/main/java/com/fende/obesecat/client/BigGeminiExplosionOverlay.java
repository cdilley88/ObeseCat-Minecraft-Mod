package com.fende.obesecat.client;

import com.fende.obesecat.ObeseCatMod;
import java.util.Random;
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

public final class BigGeminiExplosionOverlay {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "big_gemini_nuclear_overlay");

    private static final int SHOCKWAVE_ARRIVAL_TICK = 4;
    private static final int FLASH_PEAK_TICKS = 24;
    private static final int THERMAL_GLOW_TICKS = 100;
    private static final int FALLOUT_TINT_TICKS = 320;

    private static final Random RANDOM = new Random();

    private static long startTick = -1L;
    private static BlockPos blastOrigin = BlockPos.ZERO;
    private static float blastIntensity = 1.0F;
    private static boolean soundPlayed = false;

    private BigGeminiExplosionOverlay() {
    }

    public static void trigger(BlockPos origin, int durationTicks, float intensity) {
        Minecraft minecraft = Minecraft.getInstance();
        startTick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        blastOrigin = origin;
        blastIntensity = Mth.clamp(intensity, 0.5F, 1.0F);
        soundPlayed = false;
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

        // Shockwave audio arrival - ONE smooth, thunderous roar
        if (!soundPlayed && elapsed >= SHOCKWAVE_ARRIVAL_TICK) {
            playThermonuclearSounds(minecraft);
            soundPlayed = true;
        }

        if (elapsed > FALLOUT_TINT_TICKS) {
            startTick = -1L;
            return;
        }

        // Heavy seismic rumble and violent camera shake (up to 512 blocks away)
        if (elapsed >= SHOCKWAVE_ARRIVAL_TICK && elapsed <= 90) {
            LocalPlayer player = minecraft.player;
            double distSq = player.distanceToSqr(blastOrigin.getX() + 0.5D, blastOrigin.getY() + 0.5D, blastOrigin.getZ() + 0.5D);
            double dist = Math.sqrt(distSq);

            if (dist < 512.0D) {
                float distFactor = (float) Math.max(0.0D, 1.0D - (dist / 512.0D));
                float shakeProgress = 1.0F - (float) (elapsed - SHOCKWAVE_ARRIVAL_TICK) / 86.0F;
                float shake = distFactor * shakeProgress * blastIntensity * 3.5F;

                if (shake > 0.04F) {
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

        // One single smooth photometric blast profile (no dip, no double pulse)
        if (elapsed < FLASH_PEAK_TICKS) {
            // Initial blinding solar white flash
            float progress = (float) elapsed / (float) FLASH_PEAK_TICKS;
            int alpha = (int) ((1.0F - progress * 0.15F) * blastIntensity * 255.0F);
            guiGraphics.fill(0, 0, width, height, (Mth.clamp(alpha, 0, 255) << 24) | 0xFFFCF4);
        } else if (elapsed < THERMAL_GLOW_TICKS) {
            // Smoothly transitions into warm incandescent golden-amber radiance
            float progress = (float) (elapsed - FLASH_PEAK_TICKS) / (float) (THERMAL_GLOW_TICKS - FLASH_PEAK_TICKS);
            int alpha = (int) (Mth.lerp(progress, 215.0F, 65.0F) * blastIntensity);
            guiGraphics.fill(0, 0, width, height, (Mth.clamp(alpha, 0, 255) << 24) | 0xFFD490);
        } else {
            // Soft atmospheric fallout ash tint gently clearing to normal view
            float progress = (float) (elapsed - THERMAL_GLOW_TICKS) / (float) (FALLOUT_TINT_TICKS - THERMAL_GLOW_TICKS);
            int alpha = (int) ((1.0F - progress) * blastIntensity * 65.0F);
            guiGraphics.fill(0, 0, width, height, (Mth.clamp(alpha, 0, 255) << 24) | 0x332822);
        }
    }

    private static void playThermonuclearSounds(Minecraft minecraft) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE.value(), 0.38F, 10.0F));
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WARDEN_SONIC_BOOM, 0.45F, 9.0F));
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.45F, 9.0F));
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.DRAGON_FIREBALL_EXPLODE, 0.32F, 8.0F));
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.END_PORTAL_SPAWN, 0.35F, 7.0F));
    }
}
