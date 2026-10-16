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

public final class BigGeminiFireballRenderer {
    private static final int SPHERE_RINGS = 20;
    private static final int SPHERE_SECTORS = 38;
    private static final Vector3f[][] UNIT_SPHERE = new Vector3f[SPHERE_RINGS + 1][SPHERE_SECTORS + 1];

    private static final int STEM_RINGS = 32;
    private static final int STEM_SECTORS = 32;

    private static final int MID_COLLAR_RINGS = 12;
    private static final int MID_COLLAR_SECTORS = 32;

    private static final int SUPER_CAP_RINGS = 24;
    private static final int SUPER_CAP_SECTORS = 40;

    private static final int CROWN_RINGS = 16;
    private static final int CROWN_SECTORS = 36;

    private static final int CEILING_RINGS = 14;
    private static final int CEILING_SECTORS = 36;

    private static final List<ActiveThermonuclearBlast> ACTIVE_BLASTS = new ArrayList<>();

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

    private BigGeminiFireballRenderer() {
    }

    public static void spawn(BlockPos origin, int durationTicks, float maxRadius) {
        Minecraft minecraft = Minecraft.getInstance();
        long startTick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        ACTIVE_BLASTS.add(new ActiveThermonuclearBlast(
                Vec3.atCenterOf(origin),
                startTick,
                Math.max(520, durationTicks),
                Math.max(68.0F, maxRadius)
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
        // PASS 1: Volumetric Ash, Soot & Double-Deck Cloud (RenderType.debugQuads())
        // =========================================================================
        boolean renderedSmoke = false;
        VertexConsumer smokeConsumer = null;

        for (ActiveThermonuclearBlast blast : ACTIVE_BLASTS) {
            float elapsed = (float) (gameTime - blast.startTick) + partialTick;
            if (elapsed < 10.0F || elapsed >= blast.durationTicks) continue;

            if (!renderedSmoke) {
                smokeConsumer = bufferSource.getBuffer(RenderType.debugQuads());
                renderedSmoke = true;
            }

            poseStack.pushPose();
            poseStack.translate(blast.center.x - camPos.x, blast.center.y - camPos.y, blast.center.z - camPos.z);
            Matrix4f baseMatrix = poseStack.last().pose();

            // Thermonuclear climb: reaches 148 blocks high (into the mesosphere!)
            float climbProgress = 1.0F - (float) Math.exp(-(elapsed - 10.0F) / 32.0F);
            float stemHeight = Math.max(0.0F, climbProgress * 148.0F);

            // Super-Anvil blooms outward to 68m major radius (180+ blocks wide!)
            float bloomProgress = 1.0F - (float) Math.exp(-(elapsed - 16.0F) / 46.0F);
            float superMajorR = 6.0F + bloomProgress * 62.0F;
            float superMinorR = 5.0F + bloomProgress * 17.0F;
            float throatR = Math.max(8.0F, superMajorR - superMinorR * 0.45F);

            // 1. Towering Caldera Stem Column (Plunges 48 blocks down directly into the caldera bedrock)
            if (elapsed >= 12.0F) {
                renderAbyssalStemSmoke(smokeConsumer, baseMatrix, elapsed, stemHeight, throatR);
            }

            // 3. Mid-Altitude Secondary Deck Cauliflower Collar (At y ~ 70)
            if (elapsed >= 22.0F && stemHeight >= 45.0F) {
                renderMidDeckCollarSmoke(smokeConsumer, baseMatrix, elapsed, Math.min(stemHeight * 0.52F, 75.0F));
            }

            // 4. Stratospheric Super-Anvil Torus (180+ blocks wide)
            if (elapsed >= 18.0F) {
                renderSuperAnvilSmoke(smokeConsumer, baseMatrix, elapsed, stemHeight, superMajorR, superMinorR);
            }

            // 5. Mesospheric Crown Anvil Dome (Ice-cirrus ripples)
            if (elapsed >= 18.0F) {
                renderMesosphericCrown(smokeConsumer, baseMatrix, elapsed, stemHeight, superMajorR, superMinorR);
            }

            // 6. Underbelly Boiling Caldera Ceiling
            if (elapsed >= 18.0F) {
                renderSuperCeiling(smokeConsumer, baseMatrix, elapsed, stemHeight, throatR, superMinorR);
            }

            poseStack.popPose();
        }

        if (renderedSmoke) {
            bufferSource.endBatch(RenderType.debugQuads());
        }

        // =========================================================================
        // PASS 2: Thermonuclear Furnace, Embers & Aurora (RenderType.lightning())
        // =========================================================================
        boolean renderedGlow = false;
        VertexConsumer glowConsumer = null;

        for (ActiveThermonuclearBlast blast : ACTIVE_BLASTS) {
            float elapsed = (float) (gameTime - blast.startTick) + partialTick;
            if (elapsed < 0.0F || elapsed >= blast.durationTicks) continue;

            if (!renderedGlow) {
                glowConsumer = bufferSource.getBuffer(RenderType.lightning());
                renderedGlow = true;
            }

            poseStack.pushPose();
            poseStack.translate(blast.center.x - camPos.x, blast.center.y - camPos.y, blast.center.z - camPos.z);
            Matrix4f baseMatrix = poseStack.last().pose();

            float climbProgress = 1.0F - (float) Math.exp(-(elapsed - 10.0F) / 32.0F);
            float stemHeight = Math.max(0.0F, climbProgress * 148.0F);
            float bloomProgress = 1.0F - (float) Math.exp(-(elapsed - 16.0F) / 46.0F);
            float superMajorR = 6.0F + bloomProgress * 62.0F;
            float superMinorR = 5.0F + bloomProgress * 17.0F;
            float throatR = Math.max(8.0F, superMajorR - superMinorR * 0.45F);

            // 1. Saturn Condensation Ring (Mid-Altitude Shock Collar)
            renderSaturnRing(glowConsumer, baseMatrix, elapsed);

            // 2. High-Altitude Ozone Ionization Aurora (Violet / Cobalt Corona atop anvil)
            if (elapsed >= 20.0F) {
                renderIonizationAurora(glowConsumer, baseMatrix, elapsed, stemHeight, superMajorR);
            }

            // 3. Raging Thermonuclear Stem Furnace
            renderStemFurnace(glowConsumer, baseMatrix, elapsed, stemHeight, throatR);

            // 4. Super-Anvil & Ceiling Molten Veins
            renderSuperAnvilGlow(glowConsumer, baseMatrix, elapsed, stemHeight, superMajorR, superMinorR);
            renderSuperCeilingGlow(glowConsumer, baseMatrix, elapsed, stemHeight, throatR, superMinorR);

            // 5. Giant Thermonuclear Solar Sphere
            if (elapsed <= 170.0F) {
                float maxR = blast.maxRadius;
                float expansion = 1.0F - (float) Math.exp(-elapsed / 16.0F);
                float radius = maxR * expansion;
                float liftY = elapsed > 16.0F ? Math.min(65.0F, (elapsed - 16.0F) * 0.45F) : 0.0F;
                Matrix4f fireballMatrix = new Matrix4f(baseMatrix).translate(0.0F, liftY, 0.0F);

                renderThermonuclearPlasma(glowConsumer, fireballMatrix, radius, elapsed);
            }

            poseStack.popPose();
        }

        if (renderedGlow) {
            bufferSource.endBatch(RenderType.lightning());
        }

        // =========================================================================
        // PASS 3: Radiant High-Intensity Solar God-Rays (RenderType.dragonRays())
        // =========================================================================
        boolean renderedRays = false;
        VertexConsumer rayConsumer = null;

        for (ActiveThermonuclearBlast blast : ACTIVE_BLASTS) {
            float elapsed = (float) (gameTime - blast.startTick) + partialTick;
            if (elapsed < 4.0F || elapsed > 85.0F || elapsed >= blast.durationTicks) continue;

            float rayIntensity = 1.0F - (elapsed - 4.0F) / 81.0F;
            if (rayIntensity <= 0.02F) continue;

            if (!renderedRays) {
                rayConsumer = bufferSource.getBuffer(RenderType.dragonRays());
                renderedRays = true;
            }

            float maxR = blast.maxRadius;
            float expansion = 1.0F - (float) Math.exp(-elapsed / 16.0F);
            float radius = maxR * expansion;
            float liftY = elapsed > 16.0F ? Math.min(65.0F, (elapsed - 16.0F) * 0.45F) : 0.0F;

            poseStack.pushPose();
            poseStack.translate(blast.center.x - camPos.x, blast.center.y - camPos.y + liftY, blast.center.z - camPos.z);
            renderSolarRays(poseStack, rayConsumer, radius * 2.2F, rayIntensity, elapsed);
            poseStack.popPose();
        }

        if (renderedRays) {
            bufferSource.endBatch(RenderType.dragonRays());
        }
    }

    // =========================================================================
    // PASS 1 IMPLEMENTATIONS
    // =========================================================================



    private static void renderAbyssalStemSmoke(VertexConsumer consumer, Matrix4f matrix, float t, float stemHeight, float throatR) {
        if (stemHeight <= 2.0F) return;

        float alphaProg = (t < 35.0F) ? (t - 12.0F) / 23.0F : (t < 420.0F) ? 1.0F : 1.0F - (t - 420.0F) / 100.0F;
        int alpha = Mth.clamp((int) (alphaProg * 248.0F), 0, 248);
        if (alpha <= 0) return;

        // Plunges 48 blocks down directly into the excavated abyssal caldera floor
        float stemBottomY = -48.0F;

        for (int r = 0; r < STEM_RINGS; r++) {
            float f0 = (float) r / (float) STEM_RINGS;
            float f1 = (float) (r + 1) / (float) STEM_RINGS;
            float y0 = stemBottomY + f0 * (stemHeight - stemBottomY);
            float y1 = stemBottomY + f1 * (stemHeight - stemBottomY);

            // Wide caldera floor root flare -> pinched convective column -> flared anvil throat
            float rootFlare0 = (float) Math.pow(Math.max(0.0F, 1.0F - f0 / 0.22F), 1.6D) * 24.0F;
            float rootFlare1 = (float) Math.pow(Math.max(0.0F, 1.0F - f1 / 0.22F), 1.6D) * 24.0F;

            float rBase0 = 24.0F * (1.0F - 0.58F * (float) Math.sin(f0 * Math.PI)) + throatR * (f0 * f0) + rootFlare0;
            float rBase1 = 24.0F * (1.0F - 0.58F * (float) Math.sin(f1 * Math.PI)) + throatR * (f1 * f1) + rootFlare1;

            float updraftPhase0 = f0 * 18.0F - t * 0.50F;
            float updraftPhase1 = f1 * 18.0F - t * 0.50F;

            for (int s = 0; s < STEM_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / STEM_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / STEM_SECTORS;

                double swirl00 = th0 + t * 0.045D + f0 * 2.8D;
                double swirl01 = th1 + t * 0.045D + f0 * 2.8D;
                double swirl10 = th0 + t * 0.045D + f1 * 2.8D;
                double swirl11 = th1 + t * 0.045D + f1 * 2.8D;

                float b00 = 0.18F * (float) Math.sin(4.0D * swirl00 + updraftPhase0);
                float b01 = 0.18F * (float) Math.sin(4.0D * swirl01 + updraftPhase0);
                float b10 = 0.18F * (float) Math.sin(4.0D * swirl10 + updraftPhase1);
                float b11 = 0.18F * (float) Math.sin(4.0D * swirl11 + updraftPhase1);

                float x00 = (rBase0 * (1.0F + b00)) * (float) Math.cos(th0), z00 = (rBase0 * (1.0F + b00)) * (float) Math.sin(th0);
                float x01 = (rBase0 * (1.0F + b01)) * (float) Math.cos(th1), z01 = (rBase0 * (1.0F + b01)) * (float) Math.sin(th1);
                float x10 = (rBase1 * (1.0F + b10)) * (float) Math.cos(th0), z10 = (rBase1 * (1.0F + b10)) * (float) Math.sin(th0);
                float x11 = (rBase1 * (1.0F + b11)) * (float) Math.cos(th1), z11 = (rBase1 * (1.0F + b11)) * (float) Math.sin(th1);

                float tone = 0.5F + 0.5F * (float) Math.sin(5.0D * swirl00 + updraftPhase0 * 0.8D);
                float sun = 0.70F + 0.30F * (float) Math.cos(th0);

                int red = (int) (Mth.lerp(tone, 48.0F, 150.0F) * sun);
                int green = (int) (Mth.lerp(tone, 44.0F, 154.0F) * sun);
                int blue = (int) (Mth.lerp(tone, 50.0F, 164.0F) * sun);

                // Smoldering red-hot inner core
                if (b00 > 0.08F && t < 380.0F) {
                    float smolder = (t < 160.0F) ? 1.0F : 1.0F - (t - 160.0F) / 220.0F;
                    red += (int) (b00 * 95.0F * smolder);
                    green += (int) (b00 * 30.0F * smolder);
                }

                consumer.addVertex(matrix, x00, y0, z00).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x10, y1, z10).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x11, y1, z11).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x01, y0, z01).setColor(red, green, blue, alpha);
            }
        }
    }

    private static void renderMidDeckCollarSmoke(VertexConsumer consumer, Matrix4f matrix, float t, float collarY) {
        float alphaProg = (t < 35.0F) ? (t - 22.0F) / 13.0F : (t < 400.0F) ? 1.0F : 1.0F - (t - 400.0F) / 80.0F;
        int alpha = Mth.clamp((int) (alphaProg * 240.0F), 0, 240);
        if (alpha <= 0) return;

        float majorR = 28.0F + Math.min(18.0F, (t - 22.0F) * 0.20F);
        float minorR = 11.0F;
        float roll = t * 0.06F;

        for (int r = 0; r < MID_COLLAR_RINGS; r++) {
            double phi0 = (r * Math.PI * 2.0D) / MID_COLLAR_RINGS + roll;
            double phi1 = ((r + 1) * Math.PI * 2.0D) / MID_COLLAR_RINGS + roll;

            for (int s = 0; s < MID_COLLAR_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / MID_COLLAR_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / MID_COLLAR_SECTORS;

                float r00 = minorR * (1.0F + 0.15F * (float) Math.sin(5.0 * th0 + phi0 * 2.0));
                float r01 = minorR * (1.0F + 0.15F * (float) Math.sin(5.0 * th1 + phi0 * 2.0));
                float r10 = minorR * (1.0F + 0.15F * (float) Math.sin(5.0 * th0 + phi1 * 2.0));
                float r11 = minorR * (1.0F + 0.15F * (float) Math.sin(5.0 * th1 + phi1 * 2.0));

                float p00_R = majorR + r00 * (float) Math.cos(phi0);
                float y00 = collarY + r00 * (float) Math.sin(phi0);
                float x00 = p00_R * (float) Math.cos(th0), z00 = p00_R * (float) Math.sin(th0);

                float p01_R = majorR + r01 * (float) Math.cos(phi0);
                float y01 = collarY + r01 * (float) Math.sin(phi0);
                float x01 = p01_R * (float) Math.cos(th1), z01 = p01_R * (float) Math.sin(th1);

                float p10_R = majorR + r10 * (float) Math.cos(phi1);
                float y10 = collarY + r10 * (float) Math.sin(phi1);
                float x10 = p10_R * (float) Math.cos(th0), z10 = p10_R * (float) Math.sin(th0);

                float p11_R = majorR + r11 * (float) Math.cos(phi1);
                float y11 = collarY + r11 * (float) Math.sin(phi1);
                float x11 = p11_R * (float) Math.cos(th1), z11 = p11_R * (float) Math.sin(th1);

                int red = 80, green = 76, blue = 82;
                consumer.addVertex(matrix, x00, y00, z00).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x10, y10, z10).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x11, y11, z11).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x01, y01, z01).setColor(red, green, blue, alpha);
            }
        }
    }

    private static void renderSuperAnvilSmoke(VertexConsumer consumer, Matrix4f matrix, float t, float capY, float majorR, float minorR) {
        float alphaProg = (t < 35.0F) ? (t - 16.0F) / 19.0F : (t < 420.0F) ? 1.0F : 1.0F - (t - 420.0F) / 100.0F;
        int alpha = Mth.clamp((int) (alphaProg * 248.0F), 0, 248);
        if (alpha <= 0) return;

        float rollOffset = t * 0.065F;
        float swirlOffset = t * 0.018F;

        for (int r = 0; r < SUPER_CAP_RINGS; r++) {
            double phi0 = (r * Math.PI * 2.0D) / SUPER_CAP_RINGS + rollOffset;
            double phi1 = ((r + 1) * Math.PI * 2.0D) / SUPER_CAP_RINGS + rollOffset;
            float ny0 = (float) Math.sin(phi0);

            for (int s = 0; s < SUPER_CAP_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / SUPER_CAP_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / SUPER_CAP_SECTORS;

                double sw0 = th0 + swirlOffset;
                double sw1 = th1 + swirlOffset;

                float b00 = (float) (0.22D * Math.sin(5.0D * sw0 + phi0 * 2.2D - t * 0.05D) + 0.10D * Math.cos(7.0D * sw0 - phi0 * 1.5D));
                float b01 = (float) (0.22D * Math.sin(5.0D * sw1 + phi0 * 2.2D - t * 0.05D) + 0.10D * Math.cos(7.0D * sw1 - phi0 * 1.5D));
                float b10 = (float) (0.22D * Math.sin(5.0D * sw0 + phi1 * 2.2D - t * 0.05D) + 0.10D * Math.cos(7.0D * sw0 - phi1 * 1.5D));
                float b11 = (float) (0.22D * Math.sin(5.0D * sw1 + phi1 * 2.2D - t * 0.05D) + 0.10D * Math.cos(7.0D * sw1 - phi1 * 1.5D));

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
                    red = (int) Mth.lerp(lightFactor, 72.0F, Mth.lerp(toneNoise, 160.0F, 195.0F));
                    green = (int) Mth.lerp(lightFactor, 70.0F, Mth.lerp(toneNoise, 165.0F, 198.0F));
                    blue = (int) Mth.lerp(lightFactor, 76.0F, Mth.lerp(toneNoise, 175.0F, 210.0F));
                } else {
                    float smolder = (t < 160.0F) ? 1.0F : Math.max(0.0F, 1.0F - (t - 160.0F) / 240.0F);
                    float smolderHeat = 0.5F + 0.5F * (float) Math.sin(7.0D * sw0 + phi0 * 2.5D - t * 0.08D);
                    red = (int) (55.0F + toneNoise * 18.0F + (-ny0) * smolderHeat * 110.0F * smolder);
                    green = (int) (48.0F + toneNoise * 14.0F + (-ny0) * smolderHeat * 28.0F * smolder);
                    blue = (int) (52.0F + toneNoise * 16.0F);
                }

                consumer.addVertex(matrix, x00, y00, z00).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x10, y10, z10).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x11, y11, z11).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x01, y01, z01).setColor(red, green, blue, alpha);
            }
        }
    }

    private static void renderMesosphericCrown(VertexConsumer consumer, Matrix4f matrix, float t, float capY, float majorR, float minorR) {
        float alphaProg = (t < 35.0F) ? (t - 16.0F) / 19.0F : (t < 420.0F) ? 1.0F : 1.0F - (t - 420.0F) / 100.0F;
        int alpha = Mth.clamp((int) (alphaProg * 250.0F), 0, 250);
        if (alpha <= 0) return;

        float domeTopY = capY + minorR * 0.88F;

        for (int r = 0; r < CROWN_RINGS; r++) {
            float frac0 = (float) r / (float) CROWN_RINGS;
            float frac1 = (float) (r + 1) / (float) CROWN_RINGS;

            float r0_base = frac0 * (majorR + 3.0F);
            float r1_base = frac1 * (majorR + 3.0F);

            // Flattened stratospheric anvil top
            float y0_base = domeTopY - (frac0 * frac0) * 9.5F;
            float y1_base = domeTopY - (frac1 * frac1) * 9.5F;

            for (int s = 0; s < CROWN_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / CROWN_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / CROWN_SECTORS;

                float b00 = (float) (0.12D * Math.sin(6.0D * th0 + t * 0.04D) * Math.cos(4.0D * th0 - t * 0.03D));
                float b01 = (float) (0.12D * Math.sin(6.0D * th1 + t * 0.04D) * Math.cos(4.0D * th1 - t * 0.03D));
                float b10 = (float) (0.12D * Math.sin(6.0D * th0 + t * 0.04D) * Math.cos(4.0D * th0 - t * 0.03D));
                float b11 = (float) (0.12D * Math.sin(6.0D * th1 + t * 0.04D) * Math.cos(4.0D * th1 - t * 0.03D));

                float x00 = (r0_base * (1.0F + b00)) * (float) Math.cos(th0), z00 = (r0_base * (1.0F + b00)) * (float) Math.sin(th0);
                float x01 = (r0_base * (1.0F + b01)) * (float) Math.cos(th1), z01 = (r0_base * (1.0F + b01)) * (float) Math.sin(th1);
                float x10 = (r1_base * (1.0F + b10)) * (float) Math.cos(th0), z10 = (r1_base * (1.0F + b10)) * (float) Math.sin(th0);
                float x11 = (r1_base * (1.0F + b11)) * (float) Math.cos(th1), z11 = (r1_base * (1.0F + b11)) * (float) Math.sin(th1);

                // High-altitude cirrus ice-crystal silver white highlights
                float tone = 0.5F + 0.5F * (float) Math.sin(7.0D * th0 + frac0 * 8.0D - t * 0.06D);
                int red = (int) Mth.lerp(tone, 120.0F, 210.0F);
                int green = (int) Mth.lerp(tone, 118.0F, 214.0F);
                int blue = (int) Mth.lerp(tone, 125.0F, 225.0F);

                consumer.addVertex(matrix, x00, y0_base + 3.0F * b00, z00).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x10, y1_base + 3.0F * b10, z10).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x11, y1_base + 3.0F * b11, z11).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x01, y0_base + 3.0F * b01, z01).setColor(red, green, blue, alpha);
            }
        }
    }

    private static void renderSuperCeiling(VertexConsumer consumer, Matrix4f matrix, float t, float capY, float throatR, float minorR) {
        float alphaProg = (t < 35.0F) ? (t - 16.0F) / 19.0F : (t < 420.0F) ? 1.0F : 1.0F - (t - 420.0F) / 100.0F;
        int alpha = Mth.clamp((int) (alphaProg * 248.0F), 0, 248);
        if (alpha <= 0) return;

        float ceilingBaseY = capY - minorR * 0.35F;

        for (int r = 0; r < CEILING_RINGS; r++) {
            float frac0 = (float) r / (float) CEILING_RINGS;
            float frac1 = (float) (r + 1) / (float) CEILING_RINGS;

            float r0_base = frac0 * throatR;
            float r1_base = frac1 * throatR;

            float y0_base = ceilingBaseY + (frac0 * frac0) * 5.0F;
            float y1_base = ceilingBaseY + (frac1 * frac1) * 5.0F;

            for (int s = 0; s < CEILING_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / CEILING_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / CEILING_SECTORS;

                float b00 = (float) (0.15D * Math.sin(5.0D * th0 - t * 0.10D) * Math.cos(6.0D * th0 + t * 0.07D));
                float b01 = (float) (0.15D * Math.sin(5.0D * th1 - t * 0.10D) * Math.cos(6.0D * th1 + t * 0.07D));
                float b10 = (float) (0.15D * Math.sin(5.0D * th0 - t * 0.10D) * Math.cos(6.0D * th0 + t * 0.07D));
                float b11 = (float) (0.15D * Math.sin(5.0D * th1 - t * 0.10D) * Math.cos(6.0D * th1 + t * 0.07D));

                float x00 = (r0_base * (1.0F + b00)) * (float) Math.cos(th0), z00 = (r0_base * (1.0F + b00)) * (float) Math.sin(th0);
                float x01 = (r0_base * (1.0F + b01)) * (float) Math.cos(th1), z01 = (r0_base * (1.0F + b01)) * (float) Math.sin(th1);
                float x10 = (r1_base * (1.0F + b10)) * (float) Math.cos(th0), z10 = (r1_base * (1.0F + b10)) * (float) Math.sin(th0);
                float x11 = (r1_base * (1.0F + b11)) * (float) Math.cos(th1), z11 = (r1_base * (1.0F + b11)) * (float) Math.sin(th1);

                float smolder = (t < 160.0F) ? 1.0F : Math.max(0.0F, 1.0F - (t - 160.0F) / 240.0F);
                int red = (int) (58.0F + smolder * 90.0F * (1.0F - frac0 * 0.5F));
                int green = (int) (45.0F + smolder * 25.0F * (1.0F - frac0 * 0.5F));
                int blue = 46;

                consumer.addVertex(matrix, x00, y0_base, z00).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x01, y0_base, z01).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x11, y1_base, z11).setColor(red, green, blue, alpha);
                consumer.addVertex(matrix, x10, y1_base, z10).setColor(red, green, blue, alpha);
            }
        }
    }

    // =========================================================================
    // PASS 2 IMPLEMENTATIONS (FURNACE, AURORA & HIGH ENERGY GLOW)
    // =========================================================================

    private static void renderSaturnRing(VertexConsumer consumer, Matrix4f matrix, float t) {
        if (t < 18.0F || t > 75.0F) return;
        float ringR = (t - 18.0F) * 1.85F;
        float ringAlpha = (1.0F - (t - 18.0F) / 57.0F) * 155.0F;
        int alpha = Mth.clamp((int) ringAlpha, 0, 255);
        if (alpha <= 0) return;

        int sectors = 36;
        float y = 62.0F;
        float width = 6.5F;

        for (int s = 0; s < sectors; s++) {
            double th0 = (s * Math.PI * 2.0D) / sectors;
            double th1 = ((s + 1) * Math.PI * 2.0D) / sectors;

            float r0 = ringR - width;
            float r1 = ringR + width;

            consumer.addVertex(matrix, r0 * (float) Math.cos(th0), y, r0 * (float) Math.sin(th0)).setColor(180, 230, 255, 0);
            consumer.addVertex(matrix, ringR * (float) Math.cos(th0), y + 1.5F, ringR * (float) Math.sin(th0)).setColor(210, 240, 255, alpha);
            consumer.addVertex(matrix, ringR * (float) Math.cos(th1), y + 1.5F, ringR * (float) Math.sin(th1)).setColor(210, 240, 255, alpha);
            consumer.addVertex(matrix, r0 * (float) Math.cos(th1), y, r0 * (float) Math.sin(th1)).setColor(180, 230, 255, 0);

            consumer.addVertex(matrix, ringR * (float) Math.cos(th0), y + 1.5F, ringR * (float) Math.sin(th0)).setColor(210, 240, 255, alpha);
            consumer.addVertex(matrix, r1 * (float) Math.cos(th0), y, r1 * (float) Math.sin(th0)).setColor(180, 230, 255, 0);
            consumer.addVertex(matrix, r1 * (float) Math.cos(th1), y, r1 * (float) Math.sin(th1)).setColor(180, 230, 255, 0);
            consumer.addVertex(matrix, ringR * (float) Math.cos(th1), y + 1.5F, ringR * (float) Math.sin(th1)).setColor(210, 240, 255, alpha);
        }
    }

    private static void renderIonizationAurora(VertexConsumer consumer, Matrix4f matrix, float t, float capY, float majorR) {
        if (t < 20.0F || t > 280.0F) return;
        float auroraProg = (t < 80.0F) ? (t - 20.0F) / 60.0F : 1.0F - (t - 80.0F) / 200.0F;
        int alpha = Mth.clamp((int) (auroraProg * 145.0F), 0, 145);
        if (alpha <= 0) return;

        float auroraY = capY + 22.0F;
        int sectors = 32;
        float auroraR = majorR * 1.15F;

        for (int s = 0; s < sectors; s++) {
            double th0 = (s * Math.PI * 2.0D) / sectors;
            double th1 = ((s + 1) * Math.PI * 2.0D) / sectors;

            // Violet, Cobalt and Emerald shimmering corona
            float wave0 = (float) Math.sin(4.0 * th0 + t * 0.08);
            float wave1 = (float) Math.sin(4.0 * th1 + t * 0.08);

            float r0 = auroraR * (1.0F + 0.12F * wave0);
            float r1 = auroraR * (1.0F + 0.12F * wave1);

            int red = (int) (160 + 50 * wave0);
            int green = (int) (80 + 40 * wave0);
            int blue = 255;

            consumer.addVertex(matrix, r0 * 0.3F * (float) Math.cos(th0), auroraY, r0 * 0.3F * (float) Math.sin(th0)).setColor(red, green, blue, alpha);
            consumer.addVertex(matrix, r0 * (float) Math.cos(th0), auroraY + 8.0F * wave0, r0 * (float) Math.sin(th0)).setColor(red, green, blue, 0);
            consumer.addVertex(matrix, r1 * (float) Math.cos(th1), auroraY + 8.0F * wave1, r1 * (float) Math.sin(th1)).setColor(red, green, blue, 0);
            consumer.addVertex(matrix, r1 * 0.3F * (float) Math.cos(th1), auroraY, r1 * 0.3F * (float) Math.sin(th1)).setColor(red, green, blue, alpha);
        }
    }

    private static void renderStemFurnace(VertexConsumer consumer, Matrix4f matrix, float t, float stemHeight, float throatR) {
        if (t < 12.0F || t >= 400.0F || stemHeight <= 2.0F) return;

        float alphaProg = (t < 160.0F) ? 1.0F : 1.0F - (t - 160.0F) / 240.0F;
        int alpha = Mth.clamp((int) (alphaProg * 195.0F), 0, 195);
        if (alpha <= 0) return;

        float stemBottomY = -48.0F;

        for (int r = 0; r < STEM_RINGS; r++) {
            float f0 = (float) r / (float) STEM_RINGS;
            float f1 = (float) (r + 1) / (float) STEM_RINGS;
            if (f0 >= 0.94F) break;

            float y0 = stemBottomY + f0 * (stemHeight - stemBottomY);
            float y1 = stemBottomY + f1 * (stemHeight - stemBottomY);

            float rootFlare0 = (float) Math.pow(Math.max(0.0F, 1.0F - f0 / 0.22F), 1.6D) * 18.0F;
            float rootFlare1 = (float) Math.pow(Math.max(0.0F, 1.0F - f1 / 0.22F), 1.6D) * 18.0F;

            float rBase0 = ((24.0F * (1.0F - 0.58F * (float) Math.sin(f0 * Math.PI)) + throatR * (f0 * f0)) * 0.78F) + rootFlare0;
            float rBase1 = ((24.0F * (1.0F - 0.58F * (float) Math.sin(f1 * Math.PI)) + throatR * (f1 * f1)) * 0.78F) + rootFlare1;

            float updraftPhase0 = f0 * 18.0F - t * 0.50F;
            float updraftPhase1 = f1 * 18.0F - t * 0.50F;

            for (int s = 0; s < STEM_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / STEM_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / STEM_SECTORS;

                double swirl00 = th0 + t * 0.045D + f0 * 2.8D;
                double swirl01 = th1 + t * 0.045D + f0 * 2.8D;
                double swirl10 = th0 + t * 0.045D + f1 * 2.8D;
                double swirl11 = th1 + t * 0.045D + f1 * 2.8D;

                float x00 = (rBase0 * (1.0F + 0.15F * (float) Math.sin(4.0D * swirl00 + updraftPhase0))) * (float) Math.cos(th0);
                float z00 = (rBase0 * (1.0F + 0.15F * (float) Math.sin(4.0D * swirl00 + updraftPhase0))) * (float) Math.sin(th0);
                float x01 = (rBase0 * (1.0F + 0.15F * (float) Math.sin(4.0D * swirl01 + updraftPhase0))) * (float) Math.cos(th1);
                float z01 = (rBase0 * (1.0F + 0.15F * (float) Math.sin(4.0D * swirl01 + updraftPhase0))) * (float) Math.sin(th1);
                float x10 = (rBase1 * (1.0F + 0.15F * (float) Math.sin(4.0D * swirl10 + updraftPhase1))) * (float) Math.cos(th0);
                float z10 = (rBase1 * (1.0F + 0.15F * (float) Math.sin(4.0D * swirl10 + updraftPhase1))) * (float) Math.sin(th0);
                float x11 = (rBase1 * (1.0F + 0.15F * (float) Math.sin(4.0D * swirl11 + updraftPhase1))) * (float) Math.cos(th1);
                float z11 = (rBase1 * (1.0F + 0.15F * (float) Math.sin(4.0D * swirl11 + updraftPhase1))) * (float) Math.sin(th1);

                consumer.addVertex(matrix, x00, y0, z00).setColor(255, 80, 15, alpha);
                consumer.addVertex(matrix, x10, y1, z10).setColor(255, 80, 15, alpha);
                consumer.addVertex(matrix, x11, y1, z11).setColor(255, 80, 15, alpha);
                consumer.addVertex(matrix, x01, y0, z01).setColor(255, 80, 15, alpha);
            }
        }
    }

    private static void renderSuperAnvilGlow(VertexConsumer consumer, Matrix4f matrix, float t, float capY, float majorR, float minorR) {
        if (t < 18.0F || t >= 390.0F) return;

        float alphaProg = (t < 150.0F) ? 1.0F : 1.0F - (t - 150.0F) / 240.0F;
        int alpha = Mth.clamp((int) (alphaProg * 225.0F), 0, 225);
        if (alpha <= 0) return;

        float rollOffset = t * 0.065F;
        float swirlOffset = t * 0.018F;

        for (int r = 0; r < SUPER_CAP_RINGS; r++) {
            double phi0 = (r * Math.PI * 2.0D) / SUPER_CAP_RINGS + rollOffset;
            if (Math.sin(phi0) >= 0.12D) continue; // Underside only

            double phi1 = ((r + 1) * Math.PI * 2.0D) / SUPER_CAP_RINGS + rollOffset;

            for (int s = 0; s < SUPER_CAP_SECTORS; s++) {
                double th0 = (s * Math.PI * 2.0D) / SUPER_CAP_SECTORS;
                double th1 = ((s + 1) * Math.PI * 2.0D) / SUPER_CAP_SECTORS;

                double sw0 = th0 + swirlOffset;
                float vein = (float) Math.max(0.0, Math.sin(6.0D * sw0 + phi0 * 3.0D - t * 0.05D) * Math.cos(9.0D * sw0 - phi0 * 2.0D));

                float r00 = minorR * 0.94F;
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
                int green = (int) (65.0F + vein * 80.0F);
                int blue = 12;
                int vAlpha = (int) ((0.45F + 0.55F * vein) * alpha);

                consumer.addVertex(matrix, x00, y00, z00).setColor(red, green, blue, vAlpha);
                consumer.addVertex(matrix, x10, y10, z10).setColor(red, green, blue, vAlpha);
                consumer.addVertex(matrix, x11, y11, z11).setColor(red, green, blue, vAlpha);
                consumer.addVertex(matrix, x01, y01, z01).setColor(red, green, blue, vAlpha);
            }
        }
    }

    private static void renderSuperCeilingGlow(VertexConsumer consumer, Matrix4f matrix, float t, float capY, float throatR, float minorR) {
        if (t < 18.0F || t >= 390.0F) return;

        float alphaProg = (t < 150.0F) ? 1.0F : 1.0F - (t - 150.0F) / 240.0F;
        int alpha = Mth.clamp((int) (alphaProg * 210.0F), 0, 210);
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

                float vein = (float) Math.max(0.0, Math.sin(5.0D * th0 - t * 0.08D) * Math.cos(6.0D * th0 + t * 0.05D));

                float x00 = r0_base * (float) Math.cos(th0), z00 = r0_base * (float) Math.sin(th0);
                float x01 = r0_base * (float) Math.cos(th1), z01 = r0_base * (float) Math.sin(th1);
                float x10 = r1_base * (float) Math.cos(th0), z10 = r1_base * (float) Math.sin(th0);
                float x11 = r1_base * (float) Math.cos(th1), z11 = r1_base * (float) Math.sin(th1);

                int red = 255;
                int green = (int) (70.0F + vein * 60.0F);
                int blue = 12;
                int vAlpha = (int) ((0.4F + 0.6F * vein) * alpha);

                consumer.addVertex(matrix, x00, ceilingBaseY, z00).setColor(red, green, blue, vAlpha);
                consumer.addVertex(matrix, x01, ceilingBaseY, z01).setColor(red, green, blue, vAlpha);
                consumer.addVertex(matrix, x11, ceilingBaseY, z11).setColor(red, green, blue, vAlpha);
                consumer.addVertex(matrix, x10, ceilingBaseY, z10).setColor(red, green, blue, vAlpha);
            }
        }
    }

    private static void renderThermonuclearPlasma(VertexConsumer consumer, Matrix4f poseMatrix, float radius, float t) {
        int r, g, b, a;
        if (t < 14.0F) {
            r = 245; g = 250; b = 255; a = 255;
        } else if (t < 60.0F) {
            float prog = (t - 14.0F) / 46.0F;
            r = 255;
            g = (int) Mth.lerp(prog, 250.0F, 195.0F);
            b = (int) Mth.lerp(prog, 220.0F, 40.0F);
            a = 245;
        } else if (t < 120.0F) {
            float prog = (t - 60.0F) / 60.0F;
            r = 255;
            g = (int) Mth.lerp(prog, 195.0F, 110.0F);
            b = (int) Mth.lerp(prog, 40.0F, 10.0F);
            a = (int) Mth.lerp(prog, 245.0F, 160.0F);
        } else {
            float prog = (t - 120.0F) / 50.0F;
            r = (int) Mth.lerp(prog, 255.0F, 140.0F);
            g = (int) Mth.lerp(prog, 110.0F, 30.0F);
            b = 5;
            a = (int) Mth.lerp(prog, 160.0F, 0.0F);
        }

        if (a <= 0) return;

        for (int ri = 0; ri < SPHERE_RINGS; ri++) {
            for (int si = 0; si < SPHERE_SECTORS; si++) {
                emitDisplaced(consumer, poseMatrix, UNIT_SPHERE[ri][si], radius, t, 0.06F, r, g, b, a);
                emitDisplaced(consumer, poseMatrix, UNIT_SPHERE[ri + 1][si], radius, t, 0.06F, r, g, b, a);
                emitDisplaced(consumer, poseMatrix, UNIT_SPHERE[ri + 1][si + 1], radius, t, 0.06F, r, g, b, a);
                emitDisplaced(consumer, poseMatrix, UNIT_SPHERE[ri][si + 1], radius, t, 0.06F, r, g, b, a);
            }
        }
    }

    private static void renderSolarRays(PoseStack poseStack, VertexConsumer consumer, float rayLength, float intensity, float t) {
        if (intensity <= 0.02F) return;

        int alpha = Mth.clamp((int) (intensity * 230.0F), 0, 255);
        poseStack.pushPose();
        RandomSource random = RandomSource.create(443322L);
        Quaternionf rot = new Quaternionf();

        int rayCount = 28;
        for (int i = 0; i < rayCount; i++) {
            rot.rotationXYZ(
                    random.nextFloat() * ((float) Math.PI * 2.0F),
                    random.nextFloat() * ((float) Math.PI * 2.0F) + (t * 0.015F),
                    random.nextFloat() * ((float) Math.PI * 2.0F)
            );
            poseStack.mulPose(rot);
            Matrix4f rayMatrix = poseStack.last().pose();

            float length = rayLength * (0.85F + random.nextFloat() * 0.5F);
            float width = 4.5F + random.nextFloat() * 5.0F;

            consumer.addVertex(rayMatrix, 0.0F, 0.0F, 0.0F).setColor(255, 255, 255, alpha);
            consumer.addVertex(rayMatrix, -width, length, -width).setColor(255, 170, 40, 0);
            consumer.addVertex(rayMatrix, width, length, width).setColor(255, 130, 20, 0);

            consumer.addVertex(rayMatrix, 0.0F, 0.0F, 0.0F).setColor(255, 255, 255, alpha);
            consumer.addVertex(rayMatrix, width, length, -width).setColor(255, 170, 40, 0);
            consumer.addVertex(rayMatrix, -width, length, width).setColor(255, 130, 20, 0);
        }

        poseStack.popPose();
    }

    private static void emitDisplaced(VertexConsumer consumer, Matrix4f matrix, Vector3f unitNorm, float radius, float t, float turbAmp, int r, int g, int b, int a) {
        float harmonics = (float) (Math.sin(5.0 * unitNorm.y + t * 0.35) * Math.cos(6.0 * unitNorm.x - t * 0.28));
        float effectiveRadius = radius * (1.0F + turbAmp * harmonics);
        consumer.addVertex(matrix, unitNorm.x * effectiveRadius, unitNorm.y * effectiveRadius, unitNorm.z * effectiveRadius).setColor(r, g, b, a);
    }

    private static final class ActiveThermonuclearBlast {
        final Vec3 center;
        final long startTick;
        final int durationTicks;
        final float maxRadius;

        ActiveThermonuclearBlast(Vec3 center, long startTick, int durationTicks, float maxRadius) {
            this.center = center;
            this.startTick = startTick;
            this.durationTicks = durationTicks;
            this.maxRadius = maxRadius;
        }
    }
}
