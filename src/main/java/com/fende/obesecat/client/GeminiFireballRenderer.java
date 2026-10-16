package com.fende.obesecat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class GeminiFireballRenderer {
    private static final int SPHERE_RINGS = 18;
    private static final int SPHERE_SECTORS = 36;
    private static final Vector3f[][] UNIT_SPHERE = new Vector3f[SPHERE_RINGS + 1][SPHERE_SECTORS + 1];

    private static final int STEM_RINGS = 28;
    private static final int STEM_SECTORS = 28;

    private static final int CAP_RINGS = 22;
    private static final int CAP_SECTORS = 36;

    private static final int CROWN_RINGS = 14;
    private static final int CROWN_SECTORS = 32;

    private static final int CEILING_RINGS = 12;
    private static final int CEILING_SECTORS = 32;

    private static final List<ActiveFireball> ACTIVE_FIREBALLS = new ArrayList<>();

    static {
        // Pre-compute unit sphere vertices
        for (int r = 0; r <= SPHERE_RINGS; r++) {
            double v = (double) r / (double) SPHERE_RINGS;
            double phi = v * Math.PI;
            double sinPhi = Math.sin(phi);
            double cosPhi = Math.cos(phi);

            for (int s = 0; s <= SPHERE_SECTORS; s++) {
                double u = (double) s / (double) SPHERE_SECTORS;
                double theta = u * (Math.PI * 2.0D);
                double sinTheta = Math.sin(theta);
                double cosTheta = Math.cos(theta);

                UNIT_SPHERE[r][s] = new Vector3f(
                        (float) (sinPhi * cosTheta),
                        (float) cosPhi,
                        (float) (sinPhi * sinTheta)
                );
            }
        }
    }

    private GeminiFireballRenderer() {
    }

    public static void spawn(BlockPos origin, int durationTicks, float maxRadius) {
        Minecraft minecraft = Minecraft.getInstance();
        long startTick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        ACTIVE_FIREBALLS.add(new ActiveFireball(
                Vec3.atCenterOf(origin),
                startTick,
                Math.max(500, durationTicks),
                Math.max(20.0F, maxRadius)
        ));
    }

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE_FIREBALLS.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        long gameTime = minecraft.level.getGameTime();
        DeltaTracker deltaTracker = event.getPartialTick();
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);

        // Clean up expired fireballs
        ACTIVE_FIREBALLS.removeIf(fb -> {
            float elapsed = (float) (gameTime - fb.startTick) + partialTick;
            return elapsed >= fb.durationTicks;
        });

        if (ACTIVE_FIREBALLS.isEmpty()) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        // =========================================================================
        // PASS 1: Volumetric Atmospheric Smoke & Dust (RenderType.debugQuads())
        // Rendered strictly in a single dedicated batch without interleaving!
        // =========================================================================
        boolean renderedSmoke = false;
        VertexConsumer smokeConsumer = null;

        for (ActiveFireball fireball : ACTIVE_FIREBALLS) {
            float elapsed = (float) (gameTime - fireball.startTick) + partialTick;
            if (elapsed < 10.0F || elapsed >= fireball.durationTicks) {
                continue;
            }

            if (!renderedSmoke) {
                smokeConsumer = bufferSource.getBuffer(RenderType.debugQuads());
                renderedSmoke = true;
            }

            poseStack.pushPose();
            poseStack.translate(fireball.center.x - camPos.x, fireball.center.y - camPos.y, fireball.center.z - camPos.z);
            Matrix4f baseMatrix = poseStack.last().pose();

            // Dynamic connected growth: stem climbs upward, and cap sits directly at its head!
            float climbProgress = 1.0F - (float) Math.exp(-(elapsed - 10.0F) / 28.0F);
            float stemHeight = Math.max(0.0F, climbProgress * 102.0F);

            // Cap blossoms outward from a small compact bud into a colossal stratospheric anvil
            float bloomProgress = 1.0F - (float) Math.exp(-(elapsed - 15.0F) / 42.0F);
            float majorR = 4.0F + bloomProgress * 52.0F; // Expands from 4m up to 56m
            float minorR = 4.5F + bloomProgress * 15.0F; // Donut tube expands from 4.5m up to 19.5m
            float throatR = Math.max(6.0F, majorR - minorR * 0.45F);

            // 1. Base Surge (Ground-Level Dust Wave Skirt)
            if (elapsed <= 90.0F) {
                renderBaseSurge(smokeConsumer, baseMatrix, elapsed);
            }

            // 2. Towering Convective Stem Column (Violent Updraft & Twisting Vorticity)
            if (elapsed >= 12.0F) {
                renderMushroomStemSmoke(smokeConsumer, baseMatrix, elapsed, stemHeight, throatR);
            }

            // 3. Stratospheric Toroidal Mushroom Cap (Rolling Vortex Donut directly at top of stem)
            if (elapsed >= 16.0F) {
                renderMushroomCapSmoke(smokeConsumer, baseMatrix, elapsed, stemHeight, majorR, minorR);
            }

            // 4. Central Cauliflower Anvil Crown Dome (Fills top and center, eliminates hollow donut hole)
            if (elapsed >= 16.0F) {
                renderMushroomCrownDome(smokeConsumer, baseMatrix, elapsed, stemHeight, majorR, minorR);
            }

            // 5. Central Underbelly Ceiling (Fills inner underside viewed from directly below)
            if (elapsed >= 16.0F) {
                renderMushroomCeiling(smokeConsumer, baseMatrix, elapsed, stemHeight, throatR, minorR);
            }

            poseStack.popPose();
        }

        if (renderedSmoke) {
            bufferSource.endBatch(RenderType.debugQuads());
        }

        // =========================================================================
        // PASS 2: Incandescent Fire, Plasma & Embers (RenderType.lightning())
        // Full additive glow, rendered strictly in a single dedicated batch!
        // =========================================================================
        boolean renderedGlow = false;
        VertexConsumer glowConsumer = null;

        for (ActiveFireball fireball : ACTIVE_FIREBALLS) {
            float elapsed = (float) (gameTime - fireball.startTick) + partialTick;
            if (elapsed < 0.0F || elapsed >= fireball.durationTicks) {
                continue;
            }

            if (!renderedGlow) {
                glowConsumer = bufferSource.getBuffer(RenderType.lightning());
                renderedGlow = true;
            }

            poseStack.pushPose();
            poseStack.translate(fireball.center.x - camPos.x, fireball.center.y - camPos.y, fireball.center.z - camPos.z);
            Matrix4f baseMatrix = poseStack.last().pose();

            float climbProgress = 1.0F - (float) Math.exp(-(elapsed - 10.0F) / 28.0F);
            float stemHeight = Math.max(0.0F, climbProgress * 102.0F);
            float bloomProgress = 1.0F - (float) Math.exp(-(elapsed - 15.0F) / 42.0F);
            float majorR = 4.0F + bloomProgress * 52.0F;
            float minorR = 4.5F + bloomProgress * 15.0F;
            float throatR = Math.max(6.0F, majorR - minorR * 0.45F);

            // 1. Wilson Shock Condensation Disc
            renderWilsonDisc(glowConsumer, baseMatrix, elapsed);

            // 2. Convective Stem Glowing Molten Chimney (Lingers alongside the cloud)
            renderMushroomStemEmbers(glowConsumer, baseMatrix, elapsed, stemHeight, throatR);

            // 3. Toroidal Mushroom Cap & Ceiling Fiery Underbelly Smolder (Lingering glowing red veins)
            renderMushroomCapGlow(glowConsumer, baseMatrix, elapsed, stemHeight, majorR, minorR);
            renderMushroomCeilingGlow(glowConsumer, baseMatrix, elapsed, stemHeight, throatR, minorR);

            // 4. Expanding Incandescent Fireball & Solar Core
            if (elapsed <= 150.0F) {
                float maxR = fireball.maxRadius;
                float expansion = 1.0F - (float) Math.exp(-elapsed / 15.0F);
                float radius = maxR * expansion;

                float liftY = elapsed > 18.0F ? Math.min(50.0F, (elapsed - 18.0F) * 0.40F) : 0.0F;
                Matrix4f fireballMatrix = new Matrix4f(baseMatrix).translate(0.0F, liftY, 0.0F);

                // Wilson Supersonic Shockwave Condensation Bubble
                if (elapsed >= 3.0F && elapsed <= 42.0F) {
                    float shockProgress = (elapsed - 3.0F) / 39.0F;
                    float shockRadius = radius * (1.32F + shockProgress * 0.15F);
                    float shockAlpha = (1.0F - shockProgress) * 0.45F;
                    renderShockDome(glowConsumer, fireballMatrix, shockRadius, shockAlpha);
                }

                // Inner White-Hot Plasma Core (Incandescent Sun)
                renderPlasmaCore(glowConsumer, fireballMatrix, radius * 0.82F, elapsed);

                // Outer Turbulent Boiling Plasma Shell
                renderBoilingPlasmaShell(glowConsumer, fireballMatrix, radius, elapsed);
            }

            poseStack.popPose();
        }

        if (renderedGlow) {
            bufferSource.endBatch(RenderType.lightning());
        }

        // =========================================================================
        // PASS 3: Radiant Solar Beams / God-Rays (RenderType.dragonRays())
        // Multi-angle piercing rays, rendered strictly in a dedicated batch!
        // =========================================================================
        boolean renderedRays = false;
        VertexConsumer rayConsumer = null;

        for (ActiveFireball fireball : ACTIVE_FIREBALLS) {
            float elapsed = (float) (gameTime - fireball.startTick) + partialTick;
            if (elapsed < 6.0F || elapsed > 65.0F || elapsed >= fireball.durationTicks) {
                continue;
            }

            float rayIntensity = 1.0F - (elapsed - 6.0F) / 59.0F;
            if (rayIntensity <= 0.02F) {
                continue;
            }

            if (!renderedRays) {
                rayConsumer = bufferSource.getBuffer(RenderType.dragonRays());
                renderedRays = true;
            }

            float maxR = fireball.maxRadius;
            float expansion = 1.0F - (float) Math.exp(-elapsed / 15.0F);
            float radius = maxR * expansion;
            float liftY = elapsed > 18.0F ? Math.min(50.0F, (elapsed - 18.0F) * 0.40F) : 0.0F;

            poseStack.pushPose();
            poseStack.translate(fireball.center.x - camPos.x, fireball.center.y - camPos.y + liftY, fireball.center.z - camPos.z);

            renderSolarRays(poseStack, rayConsumer, radius * 1.6F, rayIntensity, elapsed);

            poseStack.popPose();
        }

        if (renderedRays) {
            bufferSource.endBatch(RenderType.dragonRays());
        }
    }

    // =========================================================================
    // VOLUMETRIC SMOKE & DUST RENDERERS (RenderType.debugQuads())
    // =========================================================================

    private static void renderBaseSurge(VertexConsumer consumer, Matrix4f matrix, float t) {
        float surgeR = Math.min(68.0F, (t - 10.0F) * 1.40F);
        float surgeAlphaProg = t < 45.0F ? (t - 10.0F) / 35.0F : 1.0F - (t - 45.0F) / 45.0F;
        int alpha = Mth.clamp((int) (surgeAlphaProg * 195.0F), 0, 195);
        if (alpha <= 0) return;

        int sectors = 28;
        for (int s = 0; s < sectors; s++) {
            double th0 = (s * Math.PI * 2.0D) / sectors;
            double th1 = ((s + 1) * Math.PI * 2.0D) / sectors;

            float innerR = 4.0F;
            float x0_in = innerR * (float) Math.cos(th0), z0_in = innerR * (float) Math.sin(th0);
            float x1_in = innerR * (float) Math.cos(th1), z1_in = innerR * (float) Math.sin(th1);

            float x0_out = surgeR * (float) Math.cos(th0), z0_out = surgeR * (float) Math.sin(th0);
            float x1_out = surgeR * (float) Math.cos(th1), z1_out = surgeR * (float) Math.sin(th1);

            float y_in = 6.0F;
            float y_out = 1.0F;

            consumer.addVertex(matrix, x0_in, y_in, z0_in).setColor(72, 65, 60, alpha);
            consumer.addVertex(matrix, x0_out, y_out, z0_out).setColor(95, 88, 82, alpha / 2);
            consumer.addVertex(matrix, x1_out, y_out, z1_out).setColor(95, 88, 82, alpha / 2);
            consumer.addVertex(matrix, x1_in, y_in, z1_in).setColor(72, 65, 60, alpha);
        }
    }

    private static void renderMushroomStemSmoke(VertexConsumer consumer, Matrix4f matrix, float t, float stemHeight, float throatR) {
        if (stemHeight <= 2.0F) return;

        // Lingers across main blast, then gently fades out over 140 ticks
        float alphaProg;
        if (t < 35.0F) {
            alphaProg = (t - 12.0F) / 23.0F;
        } else if (t < 360.0F) {
            alphaProg = 1.0F;
        } else {
            alphaProg = 1.0F - (t - 360.0F) / 140.0F;
        }
        int alpha = Mth.clamp((int) (alphaProg * 245.0F), 0, 245);
        if (alpha <= 0) return;

        float stemBottomY = -14.0F;

        for (int r = 0; r < STEM_RINGS; r++) {
            float f0 = (float) r / (float) STEM_RINGS;
            float f1 = (float) (r + 1) / (float) STEM_RINGS;
            float y0 = stemBottomY + f0 * (stemHeight - stemBottomY);
            float y1 = stemBottomY + f1 * (stemHeight - stemBottomY);

            // Fuses seamlessly into the crater floor at bottom and throat of the cap at top
            float rBase0 = 18.0F * (1.0F - 0.52F * (float) Math.sin(f0 * Math.PI)) + throatR * (f0 * f0);
            float rBase1 = 18.0F * (1.0F - 0.52F * (float) Math.sin(f1 * Math.PI)) + throatR * (f1 * f1);

            float updraftPhase0 = f0 * 16.0F - t * 0.45F;
            float updraftPhase1 = f1 * 16.0F - t * 0.45F;

            for (int s = 0; s < STEM_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / STEM_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / STEM_SECTORS;

                double swirl00 = th0 + t * 0.04D + f0 * 2.4D;
                double swirl01 = th1 + t * 0.04D + f0 * 2.4D;
                double swirl10 = th0 + t * 0.04D + f1 * 2.4D;
                double swirl11 = th1 + t * 0.04D + f1 * 2.4D;

                float b00 = 0.16F * (float) Math.sin(4.0D * swirl00 + updraftPhase0) + 0.08F * (float) Math.cos(7.0D * swirl00 - updraftPhase0 * 1.3D);
                float b01 = 0.16F * (float) Math.sin(4.0D * swirl01 + updraftPhase0) + 0.08F * (float) Math.cos(7.0D * swirl01 - updraftPhase0 * 1.3D);
                float b10 = 0.16F * (float) Math.sin(4.0D * swirl10 + updraftPhase1) + 0.08F * (float) Math.cos(7.0D * swirl10 - updraftPhase1 * 1.3D);
                float b11 = 0.16F * (float) Math.sin(4.0D * swirl11 + updraftPhase1) + 0.08F * (float) Math.cos(7.0D * swirl11 - updraftPhase1 * 1.3D);

                float rad00 = rBase0 * (1.0F + b00);
                float rad01 = rBase0 * (1.0F + b01);
                float rad10 = rBase1 * (1.0F + b10);
                float rad11 = rBase1 * (1.0F + b11);

                float x00 = rad00 * (float) Math.cos(th0), z00 = rad00 * (float) Math.sin(th0);
                float x01 = rad01 * (float) Math.cos(th1), z01 = rad01 * (float) Math.sin(th1);
                float x10 = rad10 * (float) Math.cos(th0), z10 = rad10 * (float) Math.sin(th0);
                float x11 = rad11 * (float) Math.cos(th1), z11 = rad11 * (float) Math.sin(th1);

                // Multi-tonal ash gray with smoldering red/amber streaks in inner folds
                float billowTone = 0.5F + 0.5F * (float) Math.sin(5.0D * swirl00 + updraftPhase0 * 0.8D);
                float sunLighting = 0.70F + 0.30F * (float) Math.cos(th0);

                int red = (int) (Mth.lerp(billowTone, 46.0F, 142.0F) * sunLighting);
                int green = (int) (Mth.lerp(billowTone, 42.0F, 145.0F) * sunLighting);
                int blue = (int) (Mth.lerp(billowTone, 48.0F, 155.0F) * sunLighting);

                // Inner core smoldering red heat tint
                if (b00 > 0.10F && t < 360.0F) {
                    float smolder = (t < 140.0F) ? 1.0F : 1.0F - (t - 140.0F) / 220.0F;
                    red += (int) (b00 * 85.0F * smolder);
                    green += (int) (b00 * 25.0F * smolder);
                }

                consumer.addVertex(matrix, x00, y0, z00).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x10, y1, z10).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x11, y1, z11).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x01, y0, z01).setColor(red, green, blue, alpha);
            }
        }
    }

    private static void renderMushroomCapSmoke(VertexConsumer consumer, Matrix4f matrix, float t, float capY, float majorR, float minorR) {
        float alphaProg;
        if (t < 35.0F) {
            alphaProg = (t - 15.0F) / 20.0F;
        } else if (t < 360.0F) {
            alphaProg = 1.0F;
        } else {
            alphaProg = 1.0F - (t - 360.0F) / 140.0F;
        }
        int alpha = Mth.clamp((int) (alphaProg * 248.0F), 0, 248);
        if (alpha <= 0) return;

        float rollOffset = t * 0.07F;
        float swirlOffset = t * 0.02F;

        for (int r = 0; r < CAP_RINGS; r++) {
            double phi0 = (r * Math.PI * 2.0D) / CAP_RINGS + rollOffset;
            double phi1 = ((r + 1) * Math.PI * 2.0D) / CAP_RINGS + rollOffset;

            float ny0 = (float) Math.sin(phi0);

            for (int s = 0; s < CAP_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / CAP_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / CAP_SECTORS;

                double sw0 = th0 + swirlOffset;
                double sw1 = th1 + swirlOffset;

                float b00 = (float) (0.20D * Math.sin(5.0D * sw0 + phi0 * 2.2D - t * 0.06D) + 0.10D * Math.cos(7.0D * sw0 - phi0 * 1.5D));
                float b01 = (float) (0.20D * Math.sin(5.0D * sw1 + phi0 * 2.2D - t * 0.06D) + 0.10D * Math.cos(7.0D * sw1 - phi0 * 1.5D));
                float b10 = (float) (0.20D * Math.sin(5.0D * sw0 + phi1 * 2.2D - t * 0.06D) + 0.10D * Math.cos(7.0D * sw0 - phi1 * 1.5D));
                float b11 = (float) (0.20D * Math.sin(5.0D * sw1 + phi1 * 2.2D - t * 0.06D) + 0.10D * Math.cos(7.0D * sw1 - phi1 * 1.5D));

                float r00 = minorR * (1.0F + b00);
                float r01 = minorR * (1.0F + b01);
                float r10 = minorR * (1.0F + b10);
                float r11 = minorR * (1.0F + b11);

                float p00_R = majorR + r00 * (float) Math.cos(phi0);
                float y00 = capY + r00 * (float) Math.sin(phi0);
                float x00 = p00_R * (float) Math.cos(th0), z00 = p00_R * (float) Math.sin(th0);

                float p01_R = majorR + r01 * (float) Math.cos(phi0);
                float y01 = capY + r01 * (float) Math.sin(phi0);
                float x01 = p01_R * (float) Math.cos(th1), z01 = p01_R * (float) Math.sin(th1);

                float p10_R = majorR + r10 * (float) Math.cos(phi1);
                float y10 = capY + r10 * (float) Math.sin(phi1);
                float x10 = p10_R * (float) Math.cos(th0), z10 = p10_R * (float) Math.sin(th0);

                float p11_R = majorR + r11 * (float) Math.cos(phi1);
                float y11 = capY + r11 * (float) Math.sin(phi1);
                float x11 = p11_R * (float) Math.cos(th1), z11 = p11_R * (float) Math.sin(th1);

                float sunDot = ny0 * 0.75F + 0.25F * (float) (Math.cos(phi0) * Math.cos(th0));
                float lightFactor = Mth.clamp(0.40F + 0.60F * sunDot, 0.0F, 1.0F);
                float toneNoise = 0.5F + 0.5F * (float) Math.sin(6.0D * sw0 + phi0 * 3.0D);

                int red, green, blue;

                if (ny0 >= 0.0F) {
                    // Sunlit Ash Gray Crown & Outer Ribs
                    red = (int) Mth.lerp(lightFactor, 68.0F, Mth.lerp(toneNoise, 145.0F, 175.0F));
                    green = (int) Mth.lerp(lightFactor, 65.0F, Mth.lerp(toneNoise, 148.0F, 178.0F));
                    blue = (int) Mth.lerp(lightFactor, 72.0F, Mth.lerp(toneNoise, 155.0F, 186.0F));
                } else {
                    // Smoldering Charcoal Underside with rich glowing red bands
                    float smolderHeat = 0.5F + 0.5F * (float) Math.sin(7.0D * sw0 + phi0 * 2.5D - t * 0.08D);
                    float smolderDuration = (t < 140.0F) ? 1.0F : Math.max(0.0F, 1.0F - (t - 140.0F) / 220.0F);

                    red = (int) (52.0F + toneNoise * 16.0F + (-ny0) * smolderHeat * 95.0F * smolderDuration);
                    green = (int) (48.0F + toneNoise * 14.0F + (-ny0) * smolderHeat * 24.0F * smolderDuration);
                    blue = (int) (50.0F + toneNoise * 14.0F);
                }

                consumer.addVertex(matrix, x00, y00, z00).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x10, y10, z10).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x11, y11, z11).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x01, y01, z01).setColor(red, green, blue, alpha);
            }
        }
    }

    private static void renderMushroomCrownDome(VertexConsumer consumer, Matrix4f matrix, float t, float capY, float majorR, float minorR) {
        float alphaProg = (t < 35.0F) ? (t - 15.0F) / 20.0F : (t < 360.0F) ? 1.0F : 1.0F - (t - 360.0F) / 140.0F;
        int alpha = Mth.clamp((int) (alphaProg * 248.0F), 0, 248);
        if (alpha <= 0) return;

        float domeTopY = capY + minorR * 0.85F;

        for (int r = 0; r < CROWN_RINGS; r++) {
            float frac0 = (float) r / (float) CROWN_RINGS;
            float frac1 = (float) (r + 1) / (float) CROWN_RINGS;

            float r0_base = frac0 * (majorR + 2.0F);
            float r1_base = frac1 * (majorR + 2.0F);

            float y0_base = domeTopY - (frac0 * frac0) * 8.5F;
            float y1_base = domeTopY - (frac1 * frac1) * 8.5F;

            for (int s = 0; s < CROWN_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / CROWN_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / CROWN_SECTORS;

                float b00 = (float) (0.12D * Math.sin(6.0D * th0 + t * 0.05D) * Math.cos(4.0D * th0 - t * 0.04D) + 0.06D * Math.sin(10.0D * th0 + frac0 * 6.0D));
                float b01 = (float) (0.12D * Math.sin(6.0D * th1 + t * 0.05D) * Math.cos(4.0D * th1 - t * 0.04D) + 0.06D * Math.sin(10.0D * th1 + frac0 * 6.0D));
                float b10 = (float) (0.12D * Math.sin(6.0D * th0 + t * 0.05D) * Math.cos(4.0D * th0 - t * 0.04D) + 0.06D * Math.sin(10.0D * th0 + frac1 * 6.0D));
                float b11 = (float) (0.12D * Math.sin(6.0D * th1 + t * 0.05D) * Math.cos(4.0D * th1 - t * 0.04D) + 0.06D * Math.sin(10.0D * th1 + frac1 * 6.0D));

                float rad00 = r0_base * (1.0F + b00);
                float rad01 = r0_base * (1.0F + b01);
                float rad10 = r1_base * (1.0F + b10);
                float rad11 = r1_base * (1.0F + b11);

                float y00 = y0_base + 3.0F * b00;
                float y01 = y0_base + 3.0F * b01;
                float y10 = y1_base + 3.0F * b10;
                float y11 = y1_base + 3.0F * b11;

                float x00 = rad00 * (float) Math.cos(th0), z00 = rad00 * (float) Math.sin(th0);
                float x01 = rad01 * (float) Math.cos(th1), z01 = rad01 * (float) Math.sin(th1);
                float x10 = rad10 * (float) Math.cos(th0), z10 = rad10 * (float) Math.sin(th0);
                float x11 = rad11 * (float) Math.cos(th1), z11 = rad11 * (float) Math.sin(th1);

                float sunFactor = 0.65F + 0.35F * (1.0F - frac0 * 0.4F);
                float tone = 0.5F + 0.5F * (float) Math.sin(7.0D * th0 + frac0 * 8.0D - t * 0.08D);

                int red = (int) (Mth.lerp(tone, 95.0F, 180.0F) * sunFactor);
                int green = (int) (Mth.lerp(tone, 92.0F, 183.0F) * sunFactor);
                int blue = (int) (Mth.lerp(tone, 98.0F, 192.0F) * sunFactor);

                consumer.addVertex(matrix, x00, y00, z00).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x10, y10, z10).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x11, y11, z11).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x01, y01, z01).setColor(red, green, blue, alpha);
            }
        }
    }

    private static void renderMushroomCeiling(VertexConsumer consumer, Matrix4f matrix, float t, float capY, float throatR, float minorR) {
        float alphaProg = (t < 35.0F) ? (t - 15.0F) / 20.0F : (t < 360.0F) ? 1.0F : 1.0F - (t - 360.0F) / 140.0F;
        int alpha = Mth.clamp((int) (alphaProg * 248.0F), 0, 248);
        if (alpha <= 0) return;

        float ceilingBaseY = capY - minorR * 0.35F;

        for (int r = 0; r < CEILING_RINGS; r++) {
            float frac0 = (float) r / (float) CEILING_RINGS;
            float frac1 = (float) (r + 1) / (float) CEILING_RINGS;

            float r0_base = frac0 * throatR;
            float r1_base = frac1 * throatR;

            float y0_base = ceilingBaseY + (frac0 * frac0) * 4.0F;
            float y1_base = ceilingBaseY + (frac1 * frac1) * 4.0F;

            for (int s = 0; s < CEILING_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / CEILING_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / CEILING_SECTORS;

                float b00 = (float) (0.15D * Math.sin(5.0D * th0 - t * 0.10D) * Math.cos(6.0D * th0 + t * 0.07D));
                float b01 = (float) (0.15D * Math.sin(5.0D * th1 - t * 0.10D) * Math.cos(6.0D * th1 + t * 0.07D));
                float b10 = (float) (0.15D * Math.sin(5.0D * th0 - t * 0.10D) * Math.cos(6.0D * th0 + t * 0.07D));
                float b11 = (float) (0.15D * Math.sin(5.0D * th1 - t * 0.10D) * Math.cos(6.0D * th1 + t * 0.07D));

                float rad00 = r0_base * (1.0F + b00);
                float rad01 = r0_base * (1.0F + b01);
                float rad10 = r1_base * (1.0F + b10);
                float rad11 = r1_base * (1.0F + b11);

                float y00 = y0_base + 2.5F * b00;
                float y01 = y0_base + 2.5F * b01;
                float y10 = y1_base + 2.5F * b10;
                float y11 = y1_base + 2.5F * b11;

                float x00 = rad00 * (float) Math.cos(th0), z00 = rad00 * (float) Math.sin(th0);
                float x01 = rad01 * (float) Math.cos(th1), z01 = rad01 * (float) Math.sin(th1);
                float x10 = rad10 * (float) Math.cos(th0), z10 = rad10 * (float) Math.sin(th0);
                float x11 = rad11 * (float) Math.cos(th1), z11 = rad11 * (float) Math.sin(th1);

                // Deep charcoal volcanic ceiling with smoldering red/amber embers
                float billowTone = 0.5F + 0.5F * (float) Math.sin(6.0D * th0 + frac0 * 7.0D - t * 0.08D);
                float smolderDuration = (t < 140.0F) ? 1.0F : Math.max(0.0F, 1.0F - (t - 140.0F) / 220.0F);

                int red = (int) (55.0F + billowTone * 20.0F + smolderDuration * 80.0F * (1.0F - frac0 * 0.5F));
                int green = (int) (44.0F + billowTone * 18.0F + smolderDuration * 22.0F * (1.0F - frac0 * 0.5F));
                int blue = (int) (46.0F + billowTone * 20.0F);

                consumer.addVertex(matrix, x00, y00, z00).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x01, y01, z01).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x11, y11, z11).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x10, y10, z10).setColor(red, green, blue, alpha);
            }
        }
    }

    // =========================================================================
    // INCANDESCENT FIRE, PLASMA & GLOW RENDERERS (RenderType.lightning())
    // =========================================================================

    private static void renderWilsonDisc(VertexConsumer consumer, Matrix4f matrix, float t) {
        if (t < 16.0F || t > 55.0F) return;
        float discR = (t - 16.0F) * 1.55F;
        float discAlphaProg = 1.0F - (t - 16.0F) / 39.0F;
        int alpha = Mth.clamp((int) (discAlphaProg * 140.0F), 0, 140);
        if (alpha <= 0) return;

        int sectors = 32;
        float y = 38.0F;
        float width = 4.5F;

        for (int s = 0; s < sectors; s++) {
            double th0 = (s * Math.PI * 2.0D) / sectors;
            double th1 = ((s + 1) * Math.PI * 2.0D) / sectors;

            float r0 = discR - width;
            float r1 = discR + width;

            consumer.addVertex(matrix, r0 * (float) Math.cos(th0), y, r0 * (float) Math.sin(th0)).setColor(200, 240, 255, 0);
            consumer.addVertex(matrix, discR * (float) Math.cos(th0), y + 1.2F, discR * (float) Math.sin(th0)).setColor(220, 245, 255, alpha);
            consumer.addVertex(matrix, discR * (float) Math.cos(th1), y + 1.2F, discR * (float) Math.sin(th1)).setColor(220, 245, 255, alpha);
            consumer.addVertex(matrix, r0 * (float) Math.cos(th1), y, r0 * (float) Math.sin(th1)).setColor(200, 240, 255, 0);

            consumer.addVertex(matrix, discR * (float) Math.cos(th0), y + 1.2F, discR * (float) Math.sin(th0)).setColor(220, 245, 255, alpha);
            consumer.addVertex(matrix, r1 * (float) Math.cos(th0), y, r1 * (float) Math.sin(th0)).setColor(200, 240, 255, 0);
            consumer.addVertex(matrix, r1 * (float) Math.cos(th1), y, r1 * (float) Math.sin(th1)).setColor(200, 240, 255, 0);
            consumer.addVertex(matrix, discR * (float) Math.cos(th1), y + 1.2F, discR * (float) Math.sin(th1)).setColor(220, 245, 255, alpha);
        }
    }

    private static void renderMushroomStemEmbers(VertexConsumer consumer, Matrix4f matrix, float t, float stemHeight, float throatR) {
        if (t < 12.0F || t >= 360.0F || stemHeight <= 2.0F) return;

        // Long-lasting glowing red chimney that smolders throughout the mushroom cloud's life
        float emberAlphaProg = (t < 140.0F) ? 1.0F : 1.0F - (t - 140.0F) / 220.0F;
        int emberAlpha = Mth.clamp((int) (emberAlphaProg * 185.0F), 0, 185);
        if (emberAlpha <= 0) return;

        float stemBottomY = -14.0F;

        for (int r = 0; r < STEM_RINGS; r++) {
            float f0 = (float) r / (float) STEM_RINGS;
            float f1 = (float) (r + 1) / (float) STEM_RINGS;
            if (f0 >= 0.95F) break;

            float y0 = stemBottomY + f0 * (stemHeight - stemBottomY);
            float y1 = stemBottomY + f1 * (stemHeight - stemBottomY);

            float rBase0 = (18.0F * (1.0F - 0.52F * (float) Math.sin(f0 * Math.PI)) + throatR * (f0 * f0)) * 0.78F;
            float rBase1 = (18.0F * (1.0F - 0.52F * (float) Math.sin(f1 * Math.PI)) + throatR * (f1 * f1)) * 0.78F;

            float updraftPhase0 = f0 * 16.0F - t * 0.45F;
            float updraftPhase1 = f1 * 16.0F - t * 0.45F;

            for (int s = 0; s < STEM_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / STEM_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / STEM_SECTORS;

                double swirl00 = th0 + t * 0.04D + f0 * 2.4D;
                double swirl01 = th1 + t * 0.04D + f0 * 2.4D;
                double swirl10 = th0 + t * 0.04D + f1 * 2.4D;
                double swirl11 = th1 + t * 0.04D + f1 * 2.4D;

                float rad00 = rBase0 * (1.0F + 0.14F * (float) Math.sin(4.0D * swirl00 + updraftPhase0));
                float rad01 = rBase0 * (1.0F + 0.14F * (float) Math.sin(4.0D * swirl01 + updraftPhase0));
                float rad10 = rBase1 * (1.0F + 0.14F * (float) Math.sin(4.0D * swirl10 + updraftPhase1));
                float rad11 = rBase1 * (1.0F + 0.14F * (float) Math.sin(4.0D * swirl11 + updraftPhase1));

                float x00 = rad00 * (float) Math.cos(th0), z00 = rad00 * (float) Math.sin(th0);
                float x01 = rad01 * (float) Math.cos(th1), z01 = rad01 * (float) Math.sin(th1);
                float x10 = rad10 * (float) Math.cos(th0), z10 = rad10 * (float) Math.sin(th0);
                float x11 = rad11 * (float) Math.cos(th1), z11 = rad11 * (float) Math.sin(th1);

                // Intense smoldering molten red-orange core
                consumer.addVertex(matrix, x00, y0, z00).setColor(245, 75, 14, emberAlpha);
                consumer.addVertex(matrix, x10, y1, z10).setColor(245, 75, 14, emberAlpha);
                consumer.addVertex(matrix, x11, y1, z11).setColor(245, 75, 14, emberAlpha);
                consumer.addVertex(matrix, x01, y0, z01).setColor(245, 75, 14, emberAlpha);
            }
        }
    }

    private static void renderMushroomCapGlow(VertexConsumer consumer, Matrix4f matrix, float t, float capY, float majorR, float minorR) {
        if (t < 16.0F || t >= 360.0F) return;

        // Smoldering red bands that persist throughout the lingering phase
        float alphaProg = (t < 140.0F) ? 1.0F : 1.0F - (t - 140.0F) / 220.0F;
        int underbellyEmberAlpha = Mth.clamp((int) (alphaProg * 215.0F), 0, 215);
        if (underbellyEmberAlpha <= 0) return;

        float rollOffset = t * 0.07F;
        float swirlOffset = t * 0.02F;

        for (int r = 0; r < CAP_RINGS; r++) {
            double phi0 = (r * Math.PI * 2.0D) / CAP_RINGS + rollOffset;
            if (Math.sin(phi0) >= 0.10D) {
                continue; // Full underside and throat
            }

            double phi1 = ((r + 1) * Math.PI * 2.0D) / CAP_RINGS + rollOffset;

            for (int s = 0; s < CAP_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / CAP_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / CAP_SECTORS;

                double sw0 = th0 + swirlOffset;

                // Glowing magma veins and smoldering bands
                float vein1 = (float) Math.sin(6.0D * sw0 + phi0 * 3.0D - t * 0.05D);
                float vein2 = (float) Math.cos(9.0D * sw0 - phi0 * 2.0D + t * 0.04D);
                float veinIntensity = Math.max(0.0F, (vein1 + vein2) * 0.5F);

                float r00 = minorR * 0.94F;
                float r01 = minorR * 0.94F;
                float r10 = minorR * 0.94F;
                float r11 = minorR * 0.94F;

                float p00_R = majorR + r00 * (float) Math.cos(phi0);
                float y00 = capY + r00 * (float) Math.sin(phi0);
                float x00 = p00_R * (float) Math.cos(th0), z00 = p00_R * (float) Math.sin(th0);

                float p01_R = majorR + r01 * (float) Math.cos(phi0);
                float y01 = capY + r01 * (float) Math.sin(phi0);
                float x01 = p01_R * (float) Math.cos(th1), z01 = p01_R * (float) Math.sin(th1);

                float p10_R = majorR + r10 * (float) Math.cos(phi1);
                float y10 = capY + r10 * (float) Math.sin(phi1);
                float x10 = p10_R * (float) Math.cos(th0), z10 = p10_R * (float) Math.sin(th0);

                float p11_R = majorR + r11 * (float) Math.cos(phi1);
                float y11 = capY + r11 * (float) Math.sin(phi1);
                float x11 = p11_R * (float) Math.cos(th1), z11 = p11_R * (float) Math.sin(th1);

                int red = 255;
                int green = (int) (55.0F + veinIntensity * 55.0F);
                int blue = 10;
                int currentAlpha = (int) ((0.45F + 0.55F * veinIntensity) * underbellyEmberAlpha);

                consumer.addVertex(matrix, x00, y00, z00).setColor(red, green, blue, currentAlpha);
                consumer.addVertex(matrix, x10, y10, z10).setColor(red, green, blue, currentAlpha);
                consumer.addVertex(matrix, x11, y11, z11).setColor(red, green, blue, currentAlpha);
                consumer.addVertex(matrix, x01, y01, z01).setColor(red, green, blue, currentAlpha);
            }
        }
    }

    private static void renderMushroomCeilingGlow(VertexConsumer consumer, Matrix4f matrix, float t, float capY, float throatR, float minorR) {
        if (t < 16.0F || t >= 360.0F) return;

        float alphaProg = (t < 140.0F) ? 1.0F : 1.0F - (t - 140.0F) / 220.0F;
        int alpha = Mth.clamp((int) (alphaProg * 195.0F), 0, 195);
        if (alpha <= 0) return;

        float ceilingBaseY = capY - minorR * 0.38F;

        for (int r = 0; r < CEILING_RINGS; r += 2) {
            float frac0 = (float) r / (float) CEILING_RINGS;
            float frac1 = (float) (r + 1) / (float) CEILING_RINGS;

            float r0_base = frac0 * throatR;
            float r1_base = frac1 * throatR;

            for (int s = 0; s < CEILING_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / CEILING_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / CEILING_SECTORS;

                float vein = (float) (Math.sin(5.0D * th0 - t * 0.08D) * Math.cos(6.0D * th0 + t * 0.05D));
                float veinBoost = Math.max(0.0F, vein);

                float x00 = r0_base * (float) Math.cos(th0), z00 = r0_base * (float) Math.sin(th0);
                float x01 = r0_base * (float) Math.cos(th1), z01 = r0_base * (float) Math.sin(th1);
                float x10 = r1_base * (float) Math.cos(th0), z10 = r1_base * (float) Math.sin(th0);
                float x11 = r1_base * (float) Math.cos(th1), z11 = r1_base * (float) Math.sin(th1);

                int red = 250;
                int green = (int) (60.0F + veinBoost * 45.0F);
                int blue = 10;
                int vAlpha = (int) ((0.4F + 0.6F * veinBoost) * alpha);

                consumer.addVertex(matrix, x00, ceilingBaseY, z00).setColor(red, green, blue, vAlpha);
                consumer.addVertex(matrix, x01, ceilingBaseY, z01).setColor(red, green, blue, vAlpha);
                consumer.addVertex(matrix, x11, ceilingBaseY, z11).setColor(red, green, blue, vAlpha);
                consumer.addVertex(matrix, x10, ceilingBaseY, z10).setColor(red, green, blue, vAlpha);
            }
        }
    }

    private static void renderPlasmaCore(
            VertexConsumer consumer,
            Matrix4f poseMatrix,
            float radius,
            float t
    ) {
        int r, g, b, a;
        if (t < 16.0F) {
            r = 255; g = 255; b = 255; a = 255;
        } else if (t < 55.0F) {
            float prog = (t - 16.0F) / 39.0F;
            r = 255;
            g = (int) Mth.lerp(prog, 255.0F, 190.0F);
            b = (int) Mth.lerp(prog, 220.0F, 50.0F);
            a = 245;
        } else if (t < 110.0F) {
            float prog = (t - 55.0F) / 55.0F;
            r = 255;
            g = (int) Mth.lerp(prog, 190.0F, 110.0F);
            b = (int) Mth.lerp(prog, 50.0F, 15.0F);
            a = (int) Mth.lerp(prog, 240.0F, 170.0F);
        } else {
            float prog = (t - 110.0F) / 40.0F;
            r = (int) Mth.lerp(prog, 255.0F, 160.0F);
            g = (int) Mth.lerp(prog, 110.0F, 40.0F);
            b = 10;
            a = (int) Mth.lerp(prog, 170.0F, 0.0F);
        }

        if (a <= 0) return;

        for (int ri = 0; ri < SPHERE_RINGS; ri++) {
            for (int si = 0; si < SPHERE_SECTORS; si++) {
                emitDisplacedVertex(consumer, poseMatrix, UNIT_SPHERE[ri][si], radius, t, 0.04F, r, g, b, a);
                emitDisplacedVertex(consumer, poseMatrix, UNIT_SPHERE[ri + 1][si], radius, t, 0.04F, r, g, b, a);
                emitDisplacedVertex(consumer, poseMatrix, UNIT_SPHERE[ri + 1][si + 1], radius, t, 0.04F, r, g, b, a);
                emitDisplacedVertex(consumer, poseMatrix, UNIT_SPHERE[ri][si + 1], radius, t, 0.04F, r, g, b, a);
            }
        }
    }

    private static void renderBoilingPlasmaShell(
            VertexConsumer consumer,
            Matrix4f poseMatrix,
            float radius,
            float t
    ) {
        int r, g, b, a;
        if (t < 18.0F) {
            r = 255; g = 235; b = 160; a = 230;
        } else if (t < 65.0F) {
            float prog = (t - 18.0F) / 47.0F;
            r = 255;
            g = (int) Mth.lerp(prog, 210.0F, 120.0F);
            b = (int) Mth.lerp(prog, 80.0F, 10.0F);
            a = (int) Mth.lerp(prog, 230.0F, 190.0F);
        } else if (t < 125.0F) {
            float prog = (t - 65.0F) / 60.0F;
            r = (int) Mth.lerp(prog, 255.0F, 190.0F);
            g = (int) Mth.lerp(prog, 120.0F, 40.0F);
            b = 10;
            a = (int) Mth.lerp(prog, 190.0F, 110.0F);
        } else {
            float prog = (t - 125.0F) / 25.0F;
            r = (int) Mth.lerp(prog, 190.0F, 90.0F);
            g = 25;
            b = 5;
            a = (int) Mth.lerp(prog, 110.0F, 0.0F);
        }

        if (a <= 0) return;

        for (int ri = 0; ri < SPHERE_RINGS; ri++) {
            for (int si = 0; si < SPHERE_SECTORS; si++) {
                emitDisplacedVertex(consumer, poseMatrix, UNIT_SPHERE[ri][si], radius, t, 0.08F, r, g, b, a);
                emitDisplacedVertex(consumer, poseMatrix, UNIT_SPHERE[ri + 1][si], radius, t, 0.08F, r, g, b, a);
                emitDisplacedVertex(consumer, poseMatrix, UNIT_SPHERE[ri + 1][si + 1], radius, t, 0.08F, r, g, b, a);
                emitDisplacedVertex(consumer, poseMatrix, UNIT_SPHERE[ri][si + 1], radius, t, 0.08F, r, g, b, a);
            }
        }
    }

    private static void renderShockDome(
            VertexConsumer consumer,
            Matrix4f poseMatrix,
            float radius,
            float alpha
    ) {
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        if (a <= 0) return;

        int r = 180, g = 240, b = 255;
        int halfRings = SPHERE_RINGS / 2 + 1;
        for (int ri = 0; ri < halfRings; ri++) {
            for (int si = 0; si < SPHERE_SECTORS; si++) {
                emitVertex(consumer, poseMatrix, UNIT_SPHERE[ri][si], radius, r, g, b, a);
                emitVertex(consumer, poseMatrix, UNIT_SPHERE[ri + 1][si], radius, r, g, b, a);
                emitVertex(consumer, poseMatrix, UNIT_SPHERE[ri + 1][si + 1], radius, r, g, b, a);
                emitVertex(consumer, poseMatrix, UNIT_SPHERE[ri][si + 1], radius, r, g, b, a);
            }
        }
    }

    // =========================================================================
    // SOLAR GOD-RAYS (RenderType.dragonRays())
    // =========================================================================

    private static void renderSolarRays(
            PoseStack poseStack,
            VertexConsumer consumer,
            float rayLength,
            float intensity,
            float t
    ) {
        if (intensity <= 0.02F) return;

        int alpha = Mth.clamp((int) (intensity * 210.0F), 0, 255);

        poseStack.pushPose();
        RandomSource random = RandomSource.create(98765L);
        Quaternionf rot = new Quaternionf();

        int rayCount = 20;
        for (int i = 0; i < rayCount; i++) {
            rot.rotationXYZ(
                    random.nextFloat() * ((float) Math.PI * 2.0F),
                    random.nextFloat() * ((float) Math.PI * 2.0F) + (t * 0.02F),
                    random.nextFloat() * ((float) Math.PI * 2.0F)
            );
            poseStack.mulPose(rot);
            Matrix4f rayMatrix = poseStack.last().pose();

            float length = rayLength * (0.8F + random.nextFloat() * 0.5F);
            float width = 3.5F + random.nextFloat() * 4.0F;

            consumer.addVertex(rayMatrix, 0.0F, 0.0F, 0.0F).setColor(255, 255, 255, alpha);
            consumer.addVertex(rayMatrix, -width, length, -width).setColor(255, 180, 50, 0);
            consumer.addVertex(rayMatrix, width, length, width).setColor(255, 140, 30, 0);

            consumer.addVertex(rayMatrix, 0.0F, 0.0F, 0.0F).setColor(255, 255, 255, alpha);
            consumer.addVertex(rayMatrix, width, length, -width).setColor(255, 180, 50, 0);
            consumer.addVertex(rayMatrix, -width, length, width).setColor(255, 140, 30, 0);
        }

        poseStack.popPose();
    }

    // =========================================================================
    // HELPER VERTEX EMITTERS
    // =========================================================================

    private static void emitDisplacedVertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            Vector3f unitNorm,
            float radius,
            float t,
            float turbAmp,
            int r,
            int g,
            int b,
            int a
    ) {
        float harmonics = (float) (Math.sin(5.0 * unitNorm.y + t * 0.35) * Math.cos(6.0 * unitNorm.x - t * 0.28)
                + 0.5 * Math.sin(8.0 * unitNorm.z + 7.0 * unitNorm.y + t * 0.45));
        float effectiveRadius = radius * (1.0F + turbAmp * harmonics);

        float x = unitNorm.x * effectiveRadius;
        float y = unitNorm.y * effectiveRadius;
        float z = unitNorm.z * effectiveRadius;

        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
    }

    private static void emitVertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            Vector3f unitNorm,
            float radius,
            int r,
            int g,
            int b,
            int a
    ) {
        consumer.addVertex(matrix, unitNorm.x * radius, unitNorm.y * radius, unitNorm.z * radius)
                .setColor(r, g, b, a);
    }

    private static final class ActiveFireball {
        final Vec3 center;
        final long startTick;
        final int durationTicks;
        final float maxRadius;

        ActiveFireball(Vec3 center, long startTick, int durationTicks, float maxRadius) {
            this.center = center;
            this.startTick = startTick;
            this.durationTicks = durationTicks;
            this.maxRadius = maxRadius;
        }
    }
}
