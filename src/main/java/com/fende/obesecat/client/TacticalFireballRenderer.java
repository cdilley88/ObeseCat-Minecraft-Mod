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

/**
 * 3D client-side volumetric renderer for tactical sub-kiloton (Davy Crockett) nuclear blasts.
 * Features a miniature, razor-sharp mushroom cloud with a rising convective stem,
 * rolling toroidal mushroom cap, ground-level base surge, incandescent furnace glow,
 * and prompt solar god-rays.
 */
public final class TacticalFireballRenderer {
    private static final int SPHERE_RINGS = 16;
    private static final int SPHERE_SECTORS = 32;
    private static final Vector3f[][] UNIT_SPHERE = new Vector3f[SPHERE_RINGS + 1][SPHERE_SECTORS + 1];

    private static final int STEM_RINGS = 20;
    private static final int STEM_SECTORS = 20;

    private static final int CAP_RINGS = 16;
    private static final int CAP_SECTORS = 28;

    private static final int CROWN_RINGS = 10;
    private static final int CROWN_SECTORS = 20;

    private static final int CEILING_RINGS = 8;
    private static final int CEILING_SECTORS = 20;

    private static final List<ActiveTacticalBlast> ACTIVE_BLASTS = new ArrayList<>();

    static {
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

    private TacticalFireballRenderer() {
    }

    public static void spawn(BlockPos origin, int durationTicks, float maxRadius) {
        Minecraft minecraft = Minecraft.getInstance();
        long startTick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        ACTIVE_BLASTS.add(new ActiveTacticalBlast(
                Vec3.atCenterOf(origin),
                startTick,
                Math.max(180, durationTicks),
                Math.max(6.0F, maxRadius)
        ));
    }

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE_BLASTS.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        long gameTime = minecraft.level.getGameTime();
        DeltaTracker deltaTracker = event.getPartialTick();
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);

        ACTIVE_BLASTS.removeIf(b -> {
            float elapsed = (float) (gameTime - b.startTick) + partialTick;
            return elapsed >= b.durationTicks;
        });

        if (ACTIVE_BLASTS.isEmpty()) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        // =========================================================================
        // PASS 1: Volumetric Atmospheric Smoke & Dust (RenderType.debugQuads())
        // =========================================================================
        boolean renderedSmoke = false;
        VertexConsumer smokeConsumer = null;

        for (ActiveTacticalBlast blast : ACTIVE_BLASTS) {
            float elapsed = (float) (gameTime - blast.startTick) + partialTick;
            if (elapsed < 4.0F || elapsed >= blast.durationTicks) continue;

            if (!renderedSmoke) {
                smokeConsumer = bufferSource.getBuffer(RenderType.debugQuads());
                renderedSmoke = true;
            }

            poseStack.pushPose();
            poseStack.translate(blast.center.x - camPos.x, blast.center.y - camPos.y, blast.center.z - camPos.z);
            Matrix4f baseMatrix = poseStack.last().pose();

            // Tactical climb: rises to ~26 blocks high
            float climbProgress = 1.0F - (float) Math.exp(-(elapsed - 6.0F) / 18.0F);
            float stemHeight = Math.max(0.0F, climbProgress * 26.0F);

            // Tactical mushroom anvil blooms to ~8.5m major radius, ~3.0m minor radius
            float bloomProgress = 1.0F - (float) Math.exp(-(elapsed - 8.0F) / 24.0F);
            float majorR = 2.0F + bloomProgress * 6.5F;
            float minorR = 1.4F + bloomProgress * 1.8F;
            float throatR = Math.max(2.2F, majorR - minorR * 0.45F);

            // 1. Base Surge (Ground-level dust skirt expanding outward)
            if (elapsed <= 70.0F) {
                renderBaseSurge(smokeConsumer, baseMatrix, elapsed);
            }

            // 2. Tactical Stem Column
            if (elapsed >= 6.0F) {
                renderTacticalStemSmoke(smokeConsumer, baseMatrix, elapsed, stemHeight, throatR);
            }

            // 3. Toroidal Mushroom Cap
            if (elapsed >= 8.0F) {
                renderTacticalCapSmoke(smokeConsumer, baseMatrix, elapsed, stemHeight, majorR, minorR);
            }

            // 4. Crown Dome (Cauliflower top)
            if (elapsed >= 8.0F) {
                renderTacticalCrownDome(smokeConsumer, baseMatrix, elapsed, stemHeight, majorR, minorR);
            }

            // 5. Underbelly Ceiling
            if (elapsed >= 8.0F) {
                renderTacticalCeiling(smokeConsumer, baseMatrix, elapsed, stemHeight, throatR, minorR);
            }

            poseStack.popPose();
        }

        if (renderedSmoke) {
            bufferSource.endBatch(RenderType.debugQuads());
        }

        // =========================================================================
        // PASS 2: Incandescent Fire, Plasma & Embers (RenderType.lightning())
        // =========================================================================
        boolean renderedGlow = false;
        VertexConsumer glowConsumer = null;

        for (ActiveTacticalBlast blast : ACTIVE_BLASTS) {
            float elapsed = (float) (gameTime - blast.startTick) + partialTick;
            if (elapsed < 0.0F || elapsed >= blast.durationTicks) continue;

            if (!renderedGlow) {
                glowConsumer = bufferSource.getBuffer(RenderType.lightning());
                renderedGlow = true;
            }

            poseStack.pushPose();
            poseStack.translate(blast.center.x - camPos.x, blast.center.y - camPos.y, blast.center.z - camPos.z);
            Matrix4f baseMatrix = poseStack.last().pose();

            float climbProgress = 1.0F - (float) Math.exp(-(elapsed - 6.0F) / 18.0F);
            float stemHeight = Math.max(0.0F, climbProgress * 26.0F);
            float bloomProgress = 1.0F - (float) Math.exp(-(elapsed - 8.0F) / 24.0F);
            float majorR = 2.0F + bloomProgress * 6.5F;
            float minorR = 1.4F + bloomProgress * 1.8F;
            float throatR = Math.max(2.2F, majorR - minorR * 0.45F);

            // 1. Tactical Condensation Ring
            renderCondensationRing(glowConsumer, baseMatrix, elapsed);

            // 2. Glowing Stem Furnace
            renderTacticalStemFurnace(glowConsumer, baseMatrix, elapsed, stemHeight, throatR);

            // 3. Cap Underside Veins Glow
            renderTacticalCapGlow(glowConsumer, baseMatrix, elapsed, stemHeight, majorR, minorR);

            // 4. Initial Solar Plasma Sphere
            if (elapsed <= 75.0F) {
                float maxR = blast.maxRadius;
                float expansion = 1.0F - (float) Math.exp(-elapsed / 10.0F);
                float radius = maxR * expansion;
                float liftY = elapsed > 8.0F ? Math.min(12.0F, (elapsed - 8.0F) * 0.35F) : 0.0F;
                Matrix4f fireballMatrix = new Matrix4f(baseMatrix).translate(0.0F, liftY, 0.0F);

                renderSolarPlasma(glowConsumer, fireballMatrix, radius, elapsed);
            }

            poseStack.popPose();
        }

        if (renderedGlow) {
            bufferSource.endBatch(RenderType.lightning());
        }

        // =========================================================================
        // PASS 3: Tactical Solar God-Rays (RenderType.dragonRays())
        // =========================================================================
        boolean renderedRays = false;
        VertexConsumer rayConsumer = null;

        for (ActiveTacticalBlast blast : ACTIVE_BLASTS) {
            float elapsed = (float) (gameTime - blast.startTick) + partialTick;
            if (elapsed < 2.0F || elapsed > 45.0F || elapsed >= blast.durationTicks) continue;

            float rayIntensity = 1.0F - (elapsed - 2.0F) / 43.0F;
            if (rayIntensity <= 0.02F) continue;

            if (!renderedRays) {
                rayConsumer = bufferSource.getBuffer(RenderType.dragonRays());
                renderedRays = true;
            }

            float maxR = blast.maxRadius;
            float expansion = 1.0F - (float) Math.exp(-elapsed / 10.0F);
            float radius = maxR * expansion;
            float liftY = elapsed > 8.0F ? Math.min(12.0F, (elapsed - 8.0F) * 0.35F) : 0.0F;

            poseStack.pushPose();
            poseStack.translate(blast.center.x - camPos.x, blast.center.y - camPos.y + liftY, blast.center.z - camPos.z);
            renderTacticalSolarRays(poseStack, rayConsumer, radius * 1.8F, rayIntensity, elapsed);
            poseStack.popPose();
        }

        if (renderedRays) {
            bufferSource.endBatch(RenderType.dragonRays());
        }
    }

    // =========================================================================
    // PASS 1: VOLUMETRIC SMOKE METHODS
    // =========================================================================

    private static void renderBaseSurge(VertexConsumer consumer, Matrix4f matrix, float t) {
        float progress = (t - 4.0F) / 66.0F;
        float surgeR = 2.5F + progress * 9.5F;
        float alphaProg = (t < 15.0F) ? (t - 4.0F) / 11.0F : (1.0F - progress);
        int alpha = Mth.clamp((int) (alphaProg * 215.0F), 0, 215);
        if (alpha <= 0) return;

        int sectors = 24;
        for (int s = 0; s < sectors; s++) {
            double th0 = (s * Math.PI * 2.0D) / sectors;
            double th1 = ((s + 1) * Math.PI * 2.0D) / sectors;

            float r0 = surgeR * 0.35F;
            float r1 = surgeR;
            float y0 = 0.2F;
            float y1 = 1.2F + 0.4F * (float) Math.sin(4.0D * th0 + t * 0.12D);

            float x00 = r0 * (float) Math.cos(th0), z00 = r0 * (float) Math.sin(th0);
            float x01 = r0 * (float) Math.cos(th1), z01 = r0 * (float) Math.sin(th1);
            float x10 = r1 * (float) Math.cos(th0), z10 = r1 * (float) Math.sin(th0);
            float x11 = r1 * (float) Math.cos(th1), z11 = r1 * (float) Math.sin(th1);

            int red = 82, green = 78, blue = 80;
            consumer.addVertex(matrix, x00, y0, z00).setColor(red, green, blue, alpha);
            consumer.addVertex(matrix, x10, y1, z10).setColor(red, green, blue, 0);
            consumer.addVertex(matrix, x11, y1, z11).setColor(red, green, blue, 0);
            consumer.addVertex(matrix, x01, y0, z01).setColor(red, green, blue, alpha);
        }
    }

    private static void renderTacticalStemSmoke(VertexConsumer consumer, Matrix4f matrix, float t, float stemHeight, float throatR) {
        if (stemHeight <= 1.0F) return;

        float alphaProg = (t < 20.0F) ? (t - 6.0F) / 14.0F : (t < 140.0F) ? 1.0F : 1.0F - (t - 140.0F) / 40.0F;
        int alpha = Mth.clamp((int) (alphaProg * 245.0F), 0, 245);
        if (alpha <= 0) return;

        float stemBottomY = -5.0F;

        for (int r = 0; r < STEM_RINGS; r++) {
            float f0 = (float) r / (float) STEM_RINGS;
            float f1 = (float) (r + 1) / (float) STEM_RINGS;
            float y0 = stemBottomY + f0 * (stemHeight - stemBottomY);
            float y1 = stemBottomY + f1 * (stemHeight - stemBottomY);

            // Ground root flare -> narrow convective column -> anvil throat
            float rootFlare0 = (float) Math.pow(Math.max(0.0F, 1.0F - f0 / 0.28F), 1.6D) * 3.8F;
            float rootFlare1 = (float) Math.pow(Math.max(0.0F, 1.0F - f1 / 0.28F), 1.6D) * 3.8F;

            float rBase0 = 3.6F * (1.0F - 0.52F * (float) Math.sin(f0 * Math.PI)) + throatR * (f0 * f0) + rootFlare0;
            float rBase1 = 3.6F * (1.0F - 0.52F * (float) Math.sin(f1 * Math.PI)) + throatR * (f1 * f1) + rootFlare1;

            float updraftPhase0 = f0 * 12.0F - t * 0.45F;
            float updraftPhase1 = f1 * 12.0F - t * 0.45F;

            for (int s = 0; s < STEM_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / STEM_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / STEM_SECTORS;

                double swirl00 = th0 + t * 0.05D + f0 * 2.5D;
                double swirl01 = th1 + t * 0.05D + f0 * 2.5D;
                double swirl10 = th0 + t * 0.05D + f1 * 2.5D;
                double swirl11 = th1 + t * 0.05D + f1 * 2.5D;

                float b00 = 0.16F * (float) Math.sin(3.0D * swirl00 + updraftPhase0);
                float b01 = 0.16F * (float) Math.sin(3.0D * swirl01 + updraftPhase0);
                float b10 = 0.16F * (float) Math.sin(3.0D * swirl10 + updraftPhase1);
                float b11 = 0.16F * (float) Math.sin(3.0D * swirl11 + updraftPhase1);

                float x00 = (rBase0 * (1.0F + b00)) * (float) Math.cos(th0), z00 = (rBase0 * (1.0F + b00)) * (float) Math.sin(th0);
                float x01 = (rBase0 * (1.0F + b01)) * (float) Math.cos(th1), z01 = (rBase0 * (1.0F + b01)) * (float) Math.sin(th1);
                float x10 = (rBase1 * (1.0F + b10)) * (float) Math.cos(th0), z10 = (rBase1 * (1.0F + b10)) * (float) Math.sin(th0);
                float x11 = (rBase1 * (1.0F + b11)) * (float) Math.cos(th1), z11 = (rBase1 * (1.0F + b11)) * (float) Math.sin(th1);

                float tone = 0.5F + 0.5F * (float) Math.sin(4.0D * swirl00 + updraftPhase0 * 0.8D);
                float sun = 0.70F + 0.30F * (float) Math.cos(th0);

                int red = (int) (Mth.lerp(tone, 52.0F, 140.0F) * sun);
                int green = (int) (Mth.lerp(tone, 48.0F, 142.0F) * sun);
                int blue = (int) (Mth.lerp(tone, 54.0F, 148.0F) * sun);

                // Smoldering red-hot core highlights
                if (b00 > 0.06F && t < 120.0F) {
                    float smolder = (t < 60.0F) ? 1.0F : 1.0F - (t - 60.0F) / 60.0F;
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

    private static void renderTacticalCapSmoke(VertexConsumer consumer, Matrix4f matrix, float t, float capY, float majorR, float minorR) {
        float alphaProg = (t < 20.0F) ? (t - 8.0F) / 12.0F : (t < 145.0F) ? 1.0F : 1.0F - (t - 145.0F) / 45.0F;
        int alpha = Mth.clamp((int) (alphaProg * 245.0F), 0, 245);
        if (alpha <= 0) return;

        float rollOffset = t * 0.08F;
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

                float b00 = (float) (0.20D * Math.sin(4.0D * sw0 + phi0 * 2.0D - t * 0.06D));
                float b01 = (float) (0.20D * Math.sin(4.0D * sw1 + phi0 * 2.0D - t * 0.06D));
                float b10 = (float) (0.20D * Math.sin(4.0D * sw0 + phi1 * 2.0D - t * 0.06D));
                float b11 = (float) (0.20D * Math.sin(4.0D * sw1 + phi1 * 2.0D - t * 0.06D));

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

                float sunDot = ny0 * 0.70F + 0.30F * (float) (Math.cos(phi0) * Math.cos(th0));
                float lightFactor = Mth.clamp(0.40F + 0.60F * sunDot, 0.0F, 1.0F);
                float toneNoise = 0.5F + 0.5F * (float) Math.sin(5.0D * sw0 + phi0 * 2.5D);

                int red, green, blue;
                if (ny0 >= 0.0F) {
                    red = (int) Mth.lerp(lightFactor, 68.0F, Mth.lerp(toneNoise, 150.0F, 185.0F));
                    green = (int) Mth.lerp(lightFactor, 66.0F, Mth.lerp(toneNoise, 152.0F, 188.0F));
                    blue = (int) Mth.lerp(lightFactor, 72.0F, Mth.lerp(toneNoise, 162.0F, 198.0F));
                } else {
                    float smolder = (t < 90.0F) ? 1.0F : Math.max(0.0F, 1.0F - (t - 90.0F) / 70.0F);
                    float smolderHeat = 0.5F + 0.5F * (float) Math.sin(5.0D * sw0 + phi0 * 2.0D - t * 0.1D);
                    red = (int) (52.0F + toneNoise * 16.0F + (-ny0) * smolderHeat * 95.0F * smolder);
                    green = (int) (46.0F + toneNoise * 12.0F + (-ny0) * smolderHeat * 24.0F * smolder);
                    blue = (int) (50.0F + toneNoise * 14.0F);
                }

                consumer.addVertex(matrix, x00, y00, z00).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x10, y10, z10).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x11, y11, z11).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x01, y01, z01).setColor(red, green, blue, alpha);
            }
        }
    }

    private static void renderTacticalCrownDome(VertexConsumer consumer, Matrix4f matrix, float t, float capY, float majorR, float minorR) {
        float alphaProg = (t < 20.0F) ? (t - 8.0F) / 12.0F : (t < 145.0F) ? 1.0F : 1.0F - (t - 145.0F) / 45.0F;
        int alpha = Mth.clamp((int) (alphaProg * 245.0F), 0, 245);
        if (alpha <= 0) return;

        float domeBaseY = capY + minorR * 0.25F;
        float domeR = majorR * 0.92F;
        float domeH = minorR * 1.35F;

        for (int r = 0; r < CROWN_RINGS; r++) {
            float frac0 = (float) r / (float) CROWN_RINGS;
            float frac1 = (float) (r + 1) / (float) CROWN_RINGS;

            float r0 = domeR * (float) Math.sin(frac0 * (Math.PI * 0.5D));
            float r1 = domeR * (float) Math.sin(frac1 * (Math.PI * 0.5D));

            float y0 = domeBaseY + domeH * (float) Math.cos(frac0 * (Math.PI * 0.5D));
            float y1 = domeBaseY + domeH * (float) Math.cos(frac1 * (Math.PI * 0.5D));

            for (int s = 0; s < CROWN_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / CROWN_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / CROWN_SECTORS;

                float b00 = (float) (0.12D * Math.sin(5.0D * th0 + t * 0.05D));
                float b01 = (float) (0.12D * Math.sin(5.0D * th1 + t * 0.05D));

                float x00 = (r0 * (1.0F + b00)) * (float) Math.cos(th0), z00 = (r0 * (1.0F + b00)) * (float) Math.sin(th0);
                float x01 = (r0 * (1.0F + b01)) * (float) Math.cos(th1), z01 = (r0 * (1.0F + b01)) * (float) Math.sin(th1);
                float x10 = (r1 * (1.0F + b00)) * (float) Math.cos(th0), z10 = (r1 * (1.0F + b00)) * (float) Math.sin(th0);
                float x11 = (r1 * (1.0F + b01)) * (float) Math.cos(th1), z11 = (r1 * (1.0F + b01)) * (float) Math.sin(th1);

                int red = 165, green = 168, blue = 178;
                consumer.addVertex(matrix, x00, y0, z00).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x10, y1, z10).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x11, y1, z11).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x01, y0, z01).setColor(red, green, blue, alpha);
            }
        }
    }

    private static void renderTacticalCeiling(VertexConsumer consumer, Matrix4f matrix, float t, float capY, float throatR, float minorR) {
        float alphaProg = (t < 20.0F) ? (t - 8.0F) / 12.0F : (t < 145.0F) ? 1.0F : 1.0F - (t - 145.0F) / 45.0F;
        int alpha = Mth.clamp((int) (alphaProg * 245.0F), 0, 245);
        if (alpha <= 0) return;

        float ceilingBaseY = capY - minorR * 0.35F;

        for (int r = 0; r < CEILING_RINGS; r++) {
            float frac0 = (float) r / (float) CEILING_RINGS;
            float frac1 = (float) (r + 1) / (float) CEILING_RINGS;

            float r0_base = frac0 * throatR;
            float r1_base = frac1 * throatR;

            float y0_base = ceilingBaseY + (frac0 * frac0) * 2.2F;
            float y1_base = ceilingBaseY + (frac1 * frac1) * 2.2F;

            for (int s = 0; s < CEILING_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / CEILING_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / CEILING_SECTORS;

                float b00 = (float) (0.12D * Math.sin(4.0D * th0 - t * 0.08D));
                float b01 = (float) (0.12D * Math.sin(4.0D * th1 - t * 0.08D));

                float x00 = (r0_base * (1.0F + b00)) * (float) Math.cos(th0), z00 = (r0_base * (1.0F + b00)) * (float) Math.sin(th0);
                float x01 = (r0_base * (1.0F + b01)) * (float) Math.cos(th1), z01 = (r0_base * (1.0F + b01)) * (float) Math.sin(th1);
                float x10 = (r1_base * (1.0F + b00)) * (float) Math.cos(th0), z10 = (r1_base * (1.0F + b00)) * (float) Math.sin(th0);
                float x11 = (r1_base * (1.0F + b01)) * (float) Math.cos(th1), z11 = (r1_base * (1.0F + b01)) * (float) Math.sin(th1);

                float smolder = (t < 80.0F) ? 1.0F : Math.max(0.0F, 1.0F - (t - 80.0F) / 80.0F);
                int red = (int) (55.0F + smolder * 85.0F * (1.0F - frac0 * 0.5F));
                int green = (int) (44.0F + smolder * 22.0F * (1.0F - frac0 * 0.5F));
                int blue = 45;

                consumer.addVertex(matrix, x00, y0_base, z00).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x01, y0_base, z01).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x11, y1_base, z11).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x10, y1_base, z10).setColor(red, green, blue, alpha);
            }
        }
    }

    // =========================================================================
    // PASS 2: INCANDESCENT GLOW METHODS
    // =========================================================================

    private static void renderCondensationRing(VertexConsumer consumer, Matrix4f matrix, float t) {
        if (t < 8.0F || t > 40.0F) return;
        float ringR = (t - 8.0F) * 0.95F;
        float ringAlpha = (1.0F - (t - 8.0F) / 32.0F) * 160.0F;
        int alpha = Mth.clamp((int) ringAlpha, 0, 255);
        if (alpha <= 0) return;

        int sectors = 24;
        float y = 14.0F;
        float width = 2.4F;

        for (int s = 0; s < sectors; s++) {
            double th0 = (s * Math.PI * 2.0D) / sectors;
            double th1 = ((s + 1) * Math.PI * 2.0D) / sectors;

            float r0 = ringR - width;
            float r1 = ringR + width;

            consumer.addVertex(matrix, r0 * (float) Math.cos(th0), y, r0 * (float) Math.sin(th0)).setColor(190, 235, 255, 0);
            consumer.addVertex(matrix, ringR * (float) Math.cos(th0), y + 0.6F, ringR * (float) Math.sin(th0)).setColor(220, 245, 255, alpha);
            consumer.addVertex(matrix, ringR * (float) Math.cos(th1), y + 0.6F, ringR * (float) Math.sin(th1)).setColor(220, 245, 255, alpha);
            consumer.addVertex(matrix, r0 * (float) Math.cos(th1), y, r0 * (float) Math.sin(th1)).setColor(190, 235, 255, 0);

            consumer.addVertex(matrix, ringR * (float) Math.cos(th0), y + 0.6F, ringR * (float) Math.sin(th0)).setColor(220, 245, 255, alpha);
            consumer.addVertex(matrix, r1 * (float) Math.cos(th0), y, r1 * (float) Math.sin(th0)).setColor(190, 235, 255, 0);
            consumer.addVertex(matrix, r1 * (float) Math.cos(th1), y, r1 * (float) Math.sin(th1)).setColor(190, 235, 255, 0);
            consumer.addVertex(matrix, ringR * (float) Math.cos(th1), y + 0.6F, ringR * (float) Math.sin(th1)).setColor(220, 245, 255, alpha);
        }
    }

    private static void renderTacticalStemFurnace(VertexConsumer consumer, Matrix4f matrix, float t, float stemHeight, float throatR) {
        if (t < 6.0F || t >= 130.0F || stemHeight <= 1.0F) return;

        float alphaProg = (t < 50.0F) ? 1.0F : 1.0F - (t - 50.0F) / 80.0F;
        int alpha = Mth.clamp((int) (alphaProg * 190.0F), 0, 190);
        if (alpha <= 0) return;

        float stemBottomY = -5.0F;

        for (int r = 0; r < STEM_RINGS; r++) {
            float f0 = (float) r / (float) STEM_RINGS;
            float f1 = (float) (r + 1) / (float) STEM_RINGS;
            if (f0 >= 0.90F) break;

            float y0 = stemBottomY + f0 * (stemHeight - stemBottomY);
            float y1 = stemBottomY + f1 * (stemHeight - stemBottomY);

            float rootFlare0 = (float) Math.pow(Math.max(0.0F, 1.0F - f0 / 0.28F), 1.6D) * 2.8F;
            float rootFlare1 = (float) Math.pow(Math.max(0.0F, 1.0F - f1 / 0.28F), 1.6D) * 2.8F;

            float rBase0 = ((3.6F * (1.0F - 0.52F * (float) Math.sin(f0 * Math.PI)) + throatR * (f0 * f0)) * 0.72F) + rootFlare0;
            float rBase1 = ((3.6F * (1.0F - 0.52F * (float) Math.sin(f1 * Math.PI)) + throatR * (f1 * f1)) * 0.72F) + rootFlare1;

            float updraftPhase0 = f0 * 12.0F - t * 0.45F;
            float updraftPhase1 = f1 * 12.0F - t * 0.45F;

            for (int s = 0; s < STEM_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / STEM_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / STEM_SECTORS;

                double swirl00 = th0 + t * 0.05D + f0 * 2.5D;
                double swirl01 = th1 + t * 0.05D + f0 * 2.5D;
                double swirl10 = th0 + t * 0.05D + f1 * 2.5D;
                double swirl11 = th1 + t * 0.05D + f1 * 2.5D;

                float x00 = (rBase0 * (1.0F + 0.12F * (float) Math.sin(3.0D * swirl00 + updraftPhase0))) * (float) Math.cos(th0);
                float z00 = (rBase0 * (1.0F + 0.12F * (float) Math.sin(3.0D * swirl00 + updraftPhase0))) * (float) Math.sin(th0);
                float x01 = (rBase0 * (1.0F + 0.12F * (float) Math.sin(3.0D * swirl01 + updraftPhase0))) * (float) Math.cos(th1);
                float z01 = (rBase0 * (1.0F + 0.12F * (float) Math.sin(3.0D * swirl01 + updraftPhase0))) * (float) Math.sin(th1);
                float x10 = (rBase1 * (1.0F + 0.12F * (float) Math.sin(3.0D * swirl10 + updraftPhase1))) * (float) Math.cos(th0);
                float z10 = (rBase1 * (1.0F + 0.12F * (float) Math.sin(3.0D * swirl10 + updraftPhase1))) * (float) Math.sin(th0);
                float x11 = (rBase1 * (1.0F + 0.12F * (float) Math.sin(3.0D * swirl11 + updraftPhase1))) * (float) Math.cos(th1);
                float z11 = (rBase1 * (1.0F + 0.12F * (float) Math.sin(3.0D * swirl11 + updraftPhase1))) * (float) Math.sin(th1);

                consumer.addVertex(matrix, x00, y0, z00).setColor(255, 95, 20, alpha);
                consumer.addVertex(matrix, x10, y1, z10).setColor(255, 95, 20, alpha);
                consumer.addVertex(matrix, x11, y1, z11).setColor(255, 95, 20, alpha);
                consumer.addVertex(matrix, x01, y0, z01).setColor(255, 95, 20, alpha);
            }
        }
    }

    private static void renderTacticalCapGlow(VertexConsumer consumer, Matrix4f matrix, float t, float capY, float majorR, float minorR) {
        if (t < 8.0F || t >= 120.0F) return;

        float alphaProg = (t < 50.0F) ? 1.0F : 1.0F - (t - 50.0F) / 70.0F;
        int alpha = Mth.clamp((int) (alphaProg * 210.0F), 0, 210);
        if (alpha <= 0) return;

        float rollOffset = t * 0.08F;
        float swirlOffset = t * 0.02F;

        for (int r = 0; r < CAP_RINGS; r++) {
            double phi0 = (r * Math.PI * 2.0D) / CAP_RINGS + rollOffset;
            if (Math.sin(phi0) >= 0.15D) continue; // Underside only

            double phi1 = ((r + 1) * Math.PI * 2.0D) / CAP_RINGS + rollOffset;

            for (int s = 0; s < CAP_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / CAP_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / CAP_SECTORS;

                double sw0 = th0 + swirlOffset;
                float vein = (float) Math.max(0.0, Math.sin(5.0D * sw0 + phi0 * 2.5D - t * 0.06D));

                float r00 = minorR * 0.92F;
                float p00_R = majorR + r00 * (float) Math.cos(phi0);
                float y00 = capY + r00 * (float) Math.sin(phi0);
                float x00 = p00_R * (float) Math.cos(th0), z00 = p00_R * (float) Math.sin(th0);

                float p01_R = majorR + r00 * (float) Math.cos(phi0);
                float y01 = capY + r00 * (float) Math.sin(phi0);
                float x01 = p01_R * (float) Math.cos(th1), z01 = p01_R * (float) Math.sin(th1);

                float p10_R = majorR + r00 * (float) Math.cos(phi1);
                float y10 = capY + r00 * (float) Math.sin(phi1);
                float x10 = p10_R * (float) Math.cos(th0), z10 = p10_R * (float) Math.sin(th0);

                float p11_R = majorR + r00 * (float) Math.cos(phi1);
                float y11 = capY + r00 * (float) Math.sin(phi1);
                float x11 = p11_R * (float) Math.cos(th1), z11 = p11_R * (float) Math.sin(th1);

                int red = 255;
                int green = (int) (75.0F + vein * 80.0F);
                int blue = 15;
                int vAlpha = (int) ((0.40F + 0.60F * vein) * alpha);

                consumer.addVertex(matrix, x00, y00, z00).setColor(red, green, blue, vAlpha);
                consumer.addVertex(matrix, x10, y10, z10).setColor(red, green, blue, vAlpha);
                consumer.addVertex(matrix, x11, y11, z11).setColor(red, green, blue, vAlpha);
                consumer.addVertex(matrix, x01, y01, z01).setColor(red, green, blue, vAlpha);
            }
        }
    }

    private static void renderSolarPlasma(VertexConsumer consumer, Matrix4f matrix, float radius, float t) {
        if (radius <= 0.1F) return;

        float alphaProg = (t < 25.0F) ? 1.0F : 1.0F - (t - 25.0F) / 50.0F;
        int alpha = Mth.clamp((int) (alphaProg * 255.0F), 0, 255);
        if (alpha <= 0) return;

        int red = 255;
        int green = (int) Mth.lerp(Math.min(1.0F, t / 40.0F), 245.0F, 110.0F);
        int blue = (int) Mth.lerp(Math.min(1.0F, t / 25.0F), 190.0F, 20.0F);

        for (int r = 0; r < SPHERE_RINGS; r++) {
            for (int s = 0; s < SPHERE_SECTORS; s++) {
                Vector3f v00 = UNIT_SPHERE[r][s];
                Vector3f v01 = UNIT_SPHERE[r][s + 1];
                Vector3f v10 = UNIT_SPHERE[r + 1][s];
                Vector3f v11 = UNIT_SPHERE[r + 1][s + 1];

                consumer.addVertex(matrix, v00.x() * radius, v00.y() * radius, v00.z() * radius).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, v10.x() * radius, v10.y() * radius, v10.z() * radius).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, v11.x() * radius, v11.y() * radius, v11.z() * radius).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, v01.x() * radius, v01.y() * radius, v01.z() * radius).setColor(red, green, blue, alpha);
            }
        }
    }

    // =========================================================================
    // PASS 3: DRAGON RAYS
    // =========================================================================

    private static void renderTacticalSolarRays(PoseStack poseStack, VertexConsumer consumer, float rayLength, float intensity, float t) {
        RandomSource random = RandomSource.create(4321L);
        int rayCount = 18;
        int alpha = Mth.clamp((int) (intensity * 175.0F), 0, 175);
        if (alpha <= 0) return;

        float halfWidth = rayLength * 0.12F;

        for (int i = 0; i < rayCount; i++) {
            poseStack.pushPose();

            float pitch = random.nextFloat() * 360.0F + t * 1.8F;
            float yaw = random.nextFloat() * 360.0F + t * 1.2F;
            float roll = random.nextFloat() * 360.0F;

            poseStack.mulPose(new Quaternionf().rotationYXZ(
                    (float) Math.toRadians(yaw),
                    (float) Math.toRadians(pitch),
                    (float) Math.toRadians(roll)
            ));

            Matrix4f rayMatrix = poseStack.last().pose();

            consumer.addVertex(rayMatrix, 0.0F, 0.0F, 0.0F).setColor(255, 255, 230, alpha);
            consumer.addVertex(rayMatrix, -halfWidth, rayLength, -halfWidth).setColor(255, 170, 30, 0);
            consumer.addVertex(rayMatrix, halfWidth, rayLength, -halfWidth).setColor(255, 170, 30, 0);
            consumer.addVertex(rayMatrix, 0.0F, 0.0F, 0.0F).setColor(255, 255, 230, alpha);

            consumer.addVertex(rayMatrix, 0.0F, 0.0F, 0.0F).setColor(255, 255, 230, alpha);
            consumer.addVertex(rayMatrix, halfWidth, rayLength, halfWidth).setColor(255, 170, 30, 0);
            consumer.addVertex(rayMatrix, -halfWidth, rayLength, halfWidth).setColor(255, 170, 30, 0);
            consumer.addVertex(rayMatrix, 0.0F, 0.0F, 0.0F).setColor(255, 255, 230, alpha);

            poseStack.popPose();
        }
    }

    private static final class ActiveTacticalBlast {
        final Vec3 center;
        final long startTick;
        final int durationTicks;
        final float maxRadius;

        ActiveTacticalBlast(Vec3 center, long startTick, int durationTicks, float maxRadius) {
            this.center = center;
            this.startTick = startTick;
            this.durationTicks = durationTicks;
            this.maxRadius = maxRadius;
        }
    }
}
