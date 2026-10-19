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

public final class TacticalExplosionOverlay {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "tactical_nuclear_overlay");

    private static final int SHOCKWAVE_ARRIVAL_TICK = 4;
    private static final int FLASH_PEAK_TICKS = 8;
    private static final int THERMAL_GLOW_TICKS = 28;
    private static final int FALLOUT_TINT_TICKS = 65;

    private static final Random RANDOM = new Random();

    private static long startTick = -1L;
    private static BlockPos blastOrigin = BlockPos.ZERO;
    private static float blastIntensity = 1.0F;
    private static boolean soundPlayed = false;

    private TacticalExplosionOverlay() {
    }

    public static void trigger(BlockPos origin, int durationTicks, float intensity) {
        Minecraft minecraft = Minecraft.getInstance();
        startTick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        blastOrigin = origin;
        blastIntensity = Mth.clamp(intensity, 0.4F, 1.0F);
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

        // Shockwave audio arrival - crisp, thunderous tactical blast
        if (!soundPlayed && elapsed >= SHOCKWAVE_ARRIVAL_TICK) {
            playTacticalSounds(minecraft);
            soundPlayed = true;
        }

        if (elapsed > FALLOUT_TINT_TICKS) {
            startTick = -1L;
            return;
        }

        // Tactical camera shake (up to 160 blocks away)
        if (elapsed >= SHOCKWAVE_ARRIVAL_TICK && elapsed <= 45) {
            LocalPlayer player = minecraft.player;
            double distSq = player.distanceToSqr(blastOrigin.getX() + 0.5D, blastOrigin.getY() + 0.5D, blastOrigin.getZ() + 0.5D);
            double dist = Math.sqrt(distSq);

            if (dist < 160.0D) {
                float distFactor = (float) Math.max(0.0D, 1.0D - (dist / 160.0D));
                float shakeProgress = 1.0F - (float) (elapsed - SHOCKWAVE_ARRIVAL_TICK) / 41.0F;
                float shake = distFactor * shakeProgress * blastIntensity * 2.2F;

                if (shake > 0.03F) {
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

        if (elapsed < FLASH_PEAK_TICKS) {
            // Initial blinding tactical solar flash
            float progress = (float) elapsed / (float) FLASH_PEAK_TICKS;
            int alpha = (int) ((1.0F - progress * 0.20F) * blastIntensity * 255.0F);
            guiGraphics.fill(0, 0, width, height, (Mth.clamp(alpha, 0, 255) << 24) | 0xFFFCF4);
        } else if (elapsed < THERMAL_GLOW_TICKS) {
            // Transitions into warm incandescent golden-amber radiance
            float progress = (float) (elapsed - FLASH_PEAK_TICKS) / (float) (THERMAL_GLOW_TICKS - FLASH_PEAK_TICKS);
            int alpha = (int) (Mth.lerp(progress, 190.0F, 40.0F) * blastIntensity);
            guiGraphics.fill(0, 0, width, height, (Mth.clamp(alpha, 0, 255) << 24) | 0xFFD490);
        } else {
            // Soft tactical fallout ash tint clearing gently
            float progress = (float) (elapsed - THERMAL_GLOW_TICKS) / (float) (FALLOUT_TINT_TICKS - THERMAL_GLOW_TICKS);
            int alpha = (int) ((1.0F - progress) * blastIntensity * 40.0F);
            guiGraphics.fill(0, 0, width, height, (Mth.clamp(alpha, 0, 255) << 24) | 0x332822);
        }
    }

    private static void playTacticalSounds(Minecraft minecraft) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE.value(), 0.70F, 7.0F));
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WARDEN_SONIC_BOOM, 0.75F, 6.0F));
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.80F, 6.0F));
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.DRAGON_FIREBALL_EXPLODE, 0.65F, 5.0F));
    }
}
