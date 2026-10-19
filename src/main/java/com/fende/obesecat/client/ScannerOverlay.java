package com.fende.obesecat.client;

import com.fende.obesecat.ObeseCatMod;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class ScannerOverlay {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "scanner_hud");
    private static boolean active = false;

    // Tactical Cyan Color Scheme
    private static final int COLOR_CYAN_BRIGHT = 0xFF00E5FF;
    private static final int COLOR_CYAN_DIM    = 0xFF00B0FF;
    private static final int COLOR_CYAN_DARK   = 0xFF004D5A;
    private static final int BG_PANEL          = 0x88001824;
    private static final int BORDER_PANEL      = 0xAA00E5FF;

    private ScannerOverlay() {
    }

    public static void setActive(boolean enabled) {
        active = enabled;
    }

    public static boolean isActive() {
        return active;
    }

    private static LivingEntity crosshairTarget = null;

    public static LivingEntity getCrosshairTarget() {
        return crosshairTarget;
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!active) {
            crosshairTarget = null;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) {
            crosshairTarget = null;
            return;
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        // 1. Scan for nearby living entities within 64 blocks
        AABB scanArea = mc.player.getBoundingBox().inflate(64.0D);
        List<LivingEntity> nearby = mc.level.getEntitiesOfClass(LivingEntity.class, scanArea, e -> e != mc.player && e.isAlive());
        int hostiles = 0;
        int passives = 0;
        for (LivingEntity e : nearby) {
            if (e instanceof Enemy) {
                hostiles++;
            } else {
                passives++;
            }
        }

        // 2. Render Top-Left Cyan Scanner Radar Panel
        int px = 16;
        int py = (NightVisionOverlay.isActive() ? 26 : 12) + (TelemetryOverlay.isActive() ? 54 : 0);
        int panelW = 164;
        int panelH = 24;

        guiGraphics.fill(px - 4, py - 4, px + panelW, py + panelH, BG_PANEL);
        guiGraphics.fill(px - 4, py - 4, px + panelW, py - 3, BORDER_PANEL);
        guiGraphics.fill(px - 4, py + panelH - 1, px + panelW, py + panelH, BORDER_PANEL);
        guiGraphics.fill(px - 4, py - 4, px - 3, py + panelH, BORDER_PANEL);

        guiGraphics.drawString(mc.font, "§b[✦] SCANNER // 360° LIFE-FORM RADAR", px, py, COLOR_CYAN_BRIGHT, false);
        String countStr = String.format("HOSTILES: §c%d  §7|  PASSIVES: §a%d  §7(64m)", hostiles, passives);
        guiGraphics.drawString(mc.font, countStr, px, py + 11, COLOR_CYAN_DIM, false);

        // 3. Find target in crosshair up to 64 blocks
        crosshairTarget = findCrosshairTarget(mc, 64.0D);

        // 4. Center Aiming Reticle & Target Lock-On Indicator
        if (crosshairTarget != null) {
            renderTargetLock(guiGraphics, mc, crosshairTarget, width / 2, height / 2);
        }
    }

    public static LivingEntity findCrosshairTarget(Minecraft mc, double maxDistance) {
        if (mc.player == null || mc.level == null) return null;

        net.minecraft.world.phys.Vec3 eyePos = mc.player.getEyePosition(1.0F);
        net.minecraft.world.phys.Vec3 viewVec = mc.player.getViewVector(1.0F);
        net.minecraft.world.phys.Vec3 reachVec = eyePos.add(viewVec.scale(maxDistance));

        AABB searchBox = mc.player.getBoundingBox().expandTowards(viewVec.scale(maxDistance)).inflate(4.0D);

        LivingEntity bestEntity = null;
        double bestDistSq = maxDistance * maxDistance;

        for (LivingEntity entity : mc.level.getEntitiesOfClass(LivingEntity.class, searchBox, e -> e != mc.player && e.isAlive() && !e.isInvisibleTo(mc.player))) {
            AABB entityBb = entity.getBoundingBox().inflate(0.5D);
            // 1. Direct ray hit
            java.util.Optional<net.minecraft.world.phys.Vec3> hit = entityBb.clip(eyePos, reachVec);
            if (hit.isPresent()) {
                double distSq = eyePos.distanceToSqr(hit.get());
                if (distSq < bestDistSq) {
                    bestDistSq = distSq;
                    bestEntity = entity;
                }
            } else {
                // 2. Near crosshair cone targeting (within crosshair focus tolerance)
                net.minecraft.world.phys.Vec3 toEntity = entity.getEyePosition().subtract(eyePos);
                double dist = toEntity.length();
                if (dist <= maxDistance && dist > 1.0D) {
                    net.minecraft.world.phys.Vec3 proj = eyePos.add(viewVec.scale(toEntity.dot(viewVec)));
                    double distFromRay = entity.getEyePosition().distanceTo(proj);
                    double tolerance = Math.max(1.2D, dist * 0.06D);
                    if (distFromRay < tolerance) {
                        double distSq = dist * dist;
                        if (distSq < bestDistSq) {
                            bestDistSq = distSq;
                            bestEntity = entity;
                        }
                    }
                }
            }
        }
        return bestEntity;
    }

    private static void renderTargetLock(GuiGraphics guiGraphics, Minecraft mc, LivingEntity target, int cx, int cy) {
        int dist = (int) mc.player.distanceTo(target);
        float hp = target.getHealth();
        float maxHp = target.getMaxHealth();
        int armor = target.getArmorValue();
        String name = target.getDisplayName().getString();
        boolean isHostile = target instanceof Enemy;

        // Animated cyan/red targeting box around crosshair
        int boxColor = isHostile ? 0xFFFF3333 : COLOR_CYAN_BRIGHT;
        int s = 16;
        // Top-left
        guiGraphics.fill(cx - s, cy - s, cx - s + 6, cy - s + 1, boxColor);
        guiGraphics.fill(cx - s, cy - s, cx - s + 1, cy - s + 6, boxColor);
        // Top-right
        guiGraphics.fill(cx + s - 6, cy - s, cx + s, cy - s + 1, boxColor);
        guiGraphics.fill(cx + s - 1, cy - s, cx + s, cy - s + 6, boxColor);
        // Bottom-left
        guiGraphics.fill(cx - s, cy + s - 1, cx - s + 6, cy + s, boxColor);
        guiGraphics.fill(cx - s, cy + s - 6, cx - s + 1, cy + s, boxColor);
        // Bottom-right
        guiGraphics.fill(cx + s - 6, cy + s - 1, cx + s, cy + s, boxColor);
        guiGraphics.fill(cx + s - 1, cy + s - 6, cx + s, cy + s, boxColor);

        // Center crosshair lock pip
        guiGraphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, boxColor);

        // Target Info Pill below crosshair
        String hpBar = buildHealthBar(hp, maxHp);
        String lockHeader = String.format("§b▶ LOCKED: §f%s §7[%dm] ◀", name, dist);
        String lockStats = String.format("%s §f%.0f§7/§a%.0f", hpBar, hp, maxHp);
        if (armor > 0) {
            lockStats += "  §b🛡 " + armor;
        }

        int tw1 = mc.font.width(lockHeader);
        int tw2 = mc.font.width(lockStats);
        int maxTw = Math.max(tw1, tw2);
        int lx = cx - maxTw / 2;
        int ly = cy + 22;

        guiGraphics.fill(lx - 6, ly - 3, lx + maxTw + 6, ly + 21, BG_PANEL);
        guiGraphics.fill(lx - 6, ly - 3, lx + maxTw + 6, ly - 2, boxColor);
        guiGraphics.fill(lx - 6, ly + 20, lx + maxTw + 6, ly + 21, boxColor);
        guiGraphics.fill(lx - 6, ly - 3, lx - 5, ly + 21, boxColor);
        guiGraphics.fill(lx + maxTw + 5, ly - 3, lx + maxTw + 6, ly + 21, boxColor);

        guiGraphics.drawString(mc.font, lockHeader, cx - tw1 / 2, ly, 0xFFFFFFFF, true);
        guiGraphics.drawString(mc.font, lockStats, cx - tw2 / 2, ly + 10, 0xFFFFFFFF, true);
    }

    private static String buildHealthBar(float hp, float maxHp) {
        int totalSegments = 10;
        int filled = Math.max(0, Math.min(totalSegments, Math.round((hp / maxHp) * totalSegments)));
        String color = (hp > maxHp * 0.5F) ? "§a" : ((hp > maxHp * 0.25F) ? "§e" : "§c");
        StringBuilder sb = new StringBuilder(color);
        for (int i = 0; i < totalSegments; i++) {
            if (i < filled) {
                sb.append("█");
            } else {
                sb.append("§8░");
            }
        }
        return sb.toString();
    }
}
