package com.fende.obesecat.client;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;

public final class TelemetryOverlay {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "telemetry_hud");
    private static boolean active = false;

    // Tactical Amber Color Scheme
    private static final int COLOR_AMBER_BRIGHT = 0xFFFFC107;
    private static final int COLOR_AMBER_DARK   = 0xFFFFA000;
    private static final int COLOR_CYAN_VALUE   = 0xFF64FFDA;
    private static final int COLOR_GREEN_SAFE   = 0xFF69F0AE;
    private static final int COLOR_RED_WARN     = 0xFFFF5252;
    private static final int BG_PANEL           = 0x881E1500;
    private static final int BORDER_PANEL       = 0xAAFFB300;

    private TelemetryOverlay() {
    }

    public static void setActive(boolean enabled) {
        active = enabled;
    }

    public static boolean isActive() {
        return active;
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!active) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) {
            return;
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        // 1. Tactical Screen Corner Brackets
        renderCornerBrackets(guiGraphics, width, height);

        // 2. Center Reticle Framing Brackets
        renderCenterReticleBrackets(guiGraphics, width / 2, height / 2);

        // 3. Top Center Compass Ribbon
        renderCompassRibbon(guiGraphics, mc, width);

        // 4. Top-Left Position & Chunk Telemetry (Cleanly offset below NVG if active)
        renderPositionTelemetry(guiGraphics, mc);

        // 5. Bottom-Left Unified Time, Biome, & Celestial Telemetry (Keeps top-right 100% clear for minimaps!)
        renderCelestialTelemetry(guiGraphics, mc, height);
    }

    private static void renderCornerBrackets(GuiGraphics guiGraphics, int width, int height) {
        int c = 0x66FFB300;
        int m = 14; // margin
        int l = 16; // bracket length

        // Top Left
        guiGraphics.fill(m, m, m + l, m + 1, c);
        guiGraphics.fill(m, m, m + 1, m + l, c);

        // Top Right
        guiGraphics.fill(width - m - l, m, width - m, m + 1, c);
        guiGraphics.fill(width - m - 1, m, width - m, m + l, c);

        // Bottom Left
        guiGraphics.fill(m, height - m - 1, m + l, height - m, c);
        guiGraphics.fill(m, height - m - l, m + 1, height - m, c);

        // Bottom Right
        guiGraphics.fill(width - m - l, height - m - 1, width - m, height - m, c);
        guiGraphics.fill(width - m - 1, height - m - l, width - m, height - m, c);
    }

    private static void renderCenterReticleBrackets(GuiGraphics guiGraphics, int cx, int cy) {
        int c = 0x55FFA000;
        // Subtle corner tick marks around crosshair
        guiGraphics.fill(cx - 18, cy - 8, cx - 14, cy - 7, c);
        guiGraphics.fill(cx - 18, cy - 8, cx - 17, cy - 4, c);

        guiGraphics.fill(cx + 14, cy - 8, cx + 18, cy - 7, c);
        guiGraphics.fill(cx + 17, cy - 8, cx + 18, cy - 4, c);

        guiGraphics.fill(cx - 18, cy + 7, cx - 14, cy + 8, c);
        guiGraphics.fill(cx - 18, cy + 4, cx - 17, cy + 8, c);

        guiGraphics.fill(cx + 14, cy + 7, cx + 18, cy + 8, c);
        guiGraphics.fill(cx + 17, cy + 4, cx + 18, cy + 8, c);
    }

    private static void renderCompassRibbon(GuiGraphics guiGraphics, Minecraft mc, int width) {
        int cx = width / 2;
        int y = 10;
        float yaw = Mth.wrapDegrees(mc.player.getYRot());
        if (yaw < 0) {
            yaw += 360.0F;
        }

        // Compass background tape
        int tapeWidth = 140;
        guiGraphics.fill(cx - tapeWidth / 2, y - 2, cx + tapeWidth / 2, y + 16, BG_PANEL);
        guiGraphics.fill(cx - tapeWidth / 2, y - 2, cx + tapeWidth / 2, y - 1, BORDER_PANEL);
        guiGraphics.fill(cx - tapeWidth / 2, y + 15, cx + tapeWidth / 2, y + 16, BORDER_PANEL);

        // Center cursor triangle
        guiGraphics.drawString(mc.font, "▼", cx - 3, y - 7, COLOR_AMBER_BRIGHT, false);

        // Render tick marks and headings across visible field of view (±35 degrees)
        for (int deg = -35; deg <= 35; deg++) {
            int heading = ((int) Math.round(yaw + deg)) % 360;
            if (heading < 0) heading += 360;

            if (heading % 15 == 0) {
                int px = cx + (int) (deg * 1.8F);
                if (px >= cx - tapeWidth / 2 + 6 && px <= cx + tapeWidth / 2 - 6) {
                    String label = getCardinalLabel(heading);
                    if (label != null) {
                        int lw = mc.font.width(label);
                        guiGraphics.drawString(mc.font, label, px - lw / 2, y + 2, COLOR_AMBER_BRIGHT, false);
                    } else {
                        guiGraphics.fill(px, y + 8, px + 1, y + 13, 0x99FFA000);
                    }
                }
            }
        }

        // Numerical bearing indicator
        String bearingText = String.format("%03d° // %s", (int) yaw, getDetailedDirection(yaw));
        int bw = mc.font.width(bearingText);
        guiGraphics.drawString(mc.font, bearingText, cx - bw / 2, y + 18, COLOR_CYAN_VALUE, true);
    }

    private static String getCardinalLabel(int deg) {
        return switch (deg) {
            case 0 -> "S";
            case 45 -> "SW";
            case 90 -> "W";
            case 135 -> "NW";
            case 180 -> "N";
            case 225 -> "NE";
            case 270 -> "E";
            case 315 -> "SE";
            default -> null;
        };
    }

    private static String getDetailedDirection(float yaw) {
        if (yaw >= 337.5 || yaw < 22.5) return "SOUTH (+Z)";
        if (yaw < 67.5) return "SOUTH-WEST";
        if (yaw < 112.5) return "WEST (-X)";
        if (yaw < 157.5) return "NORTH-WEST";
        if (yaw < 202.5) return "NORTH (-Z)";
        if (yaw < 247.5) return "NORTH-EAST";
        if (yaw < 292.5) return "EAST (+X)";
        return "SOUTH-EAST";
    }

    private static void renderPositionTelemetry(GuiGraphics guiGraphics, Minecraft mc) {
        BlockPos pos = mc.player.blockPosition();
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        ChunkPos chunkPos = new ChunkPos(pos);
        int inChunkX = pos.getX() & 15;
        int inChunkZ = pos.getZ() & 15;
        boolean onBorder = (inChunkX == 0 || inChunkX == 15 || inChunkZ == 0 || inChunkZ == 15);

        int px = 16;
        int py = NightVisionOverlay.isActive() ? 26 : 12;
        int panelW = 164;
        int panelH = 50;

        // Background Glass Panel
        guiGraphics.fill(px - 4, py - 4, px + panelW, py + panelH, BG_PANEL);
        guiGraphics.fill(px - 4, py - 4, px + panelW, py - 3, BORDER_PANEL);
        guiGraphics.fill(px - 4, py + panelH - 1, px + panelW, py + panelH, BORDER_PANEL);
        guiGraphics.fill(px - 4, py - 4, px - 3, py + panelH, BORDER_PANEL);

        // Header
        guiGraphics.drawString(mc.font, "§6[✦] TELEMETRY // GEOLOC", px, py, COLOR_AMBER_BRIGHT, false);

        // Coordinates
        String coordStr = String.format("XYZ: §f%.1f §7/ §f%.1f §7/ §f%.1f", x, y, z);
        guiGraphics.drawString(mc.font, coordStr, px, py + 11, COLOR_AMBER_DARK, false);

        // Block & Chunk
        String blockStr = String.format("BLK: §f[%d, %d, %d] §7• §e(%d, %d)", pos.getX(), pos.getY(), pos.getZ(), inChunkX, inChunkZ);
        guiGraphics.drawString(mc.font, blockStr, px, py + 21, COLOR_AMBER_DARK, false);

        // Chunk Border Status
        if (onBorder) {
            boolean blink = (mc.level.getGameTime() / 8L) % 2L == 0L;
            int warnColor = blink ? COLOR_AMBER_BRIGHT : COLOR_RED_WARN;
            guiGraphics.drawString(mc.font, String.format("CHUNK: §b[%d, %d] §e⚡ BORDER ⚡", chunkPos.x, chunkPos.z), px, py + 32, warnColor, false);
        } else {
            int distToEdge = Math.min(Math.min(inChunkX, 15 - inChunkX), Math.min(inChunkZ, 15 - inChunkZ));
            guiGraphics.drawString(mc.font, String.format("CHUNK: §b[%d, %d] §8(%dm edge)", chunkPos.x, chunkPos.z, distToEdge + 1), px, py + 32, 0xFF888888, false);
        }

        // Mini 16x16 Tactical Chunk Radar in the panel corner
        renderChunkRadar(guiGraphics, px + panelW - 22, py + 8, inChunkX, inChunkZ, onBorder, mc.level.getGameTime());
    }

    private static void renderChunkRadar(GuiGraphics guiGraphics, int rx, int ry, int cx, int cz, boolean onBorder, long gameTime) {
        int size = 18;
        // Chunk boundary box
        int boxBorder = onBorder ? (gameTime / 6L % 2L == 0 ? 0xFFFFD54F : 0xFFFF3333) : 0x88FFA000;
        guiGraphics.fill(rx - 1, ry - 1, rx + size + 1, ry, boxBorder);
        guiGraphics.fill(rx - 1, ry + size, rx + size + 1, ry + size + 1, boxBorder);
        guiGraphics.fill(rx - 1, ry - 1, rx, ry + size + 1, boxBorder);
        guiGraphics.fill(rx + size, ry - 1, rx + size + 1, ry + size + 1, boxBorder);
        guiGraphics.fill(rx, ry, rx + size, ry + size, 0x99100800);

        // Subtle sub-chunk crosshair center
        guiGraphics.fill(rx + 9, ry + 1, rx + 10, ry + size - 1, 0x33FFA000);
        guiGraphics.fill(rx + 1, ry + 9, rx + size - 1, ry + 10, 0x33FFA000);

        // Player Blip
        int dotX = rx + 1 + (int) (cx * (16.0F / 15.0F));
        int dotZ = ry + 1 + (int) (cz * (16.0F / 15.0F));
        int dotColor = (gameTime / 6L % 2L == 0) ? 0xFFFFFFFF : 0xFFFFC107;
        guiGraphics.fill(dotX - 1, dotZ - 1, dotX + 1, dotZ + 1, dotColor);
    }

    private static void renderCelestialTelemetry(GuiGraphics guiGraphics, Minecraft mc, int height) {
        BlockPos pos = mc.player.blockPosition();
        int rawLight = mc.level.getMaxLocalRawBrightness(pos);
        int blockLight = mc.level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = mc.level.getBrightness(LightLayer.SKY, pos);

        long dayTime = mc.level.getDayTime();
        long dayCount = dayTime / 24000L;
        long timeInDay = dayTime % 24000L;

        // Convert Minecraft ticks to 12-hour clock (0 ticks = 6:00 AM)
        int hours = (int) ((timeInDay / 1000 + 6) % 24);
        int minutes = (int) ((timeInDay % 1000) * 60 / 1000);
        String ampm = hours >= 12 ? "PM" : "AM";
        int displayHours = hours % 12;
        if (displayHours == 0) displayHours = 12;
        String timeStr = String.format("%02d:%02d %s", displayHours, minutes, ampm);

        String celestialPhase;
        if (timeInDay < 1000) celestialPhase = "§eDawn";
        else if (timeInDay < 12000) celestialPhase = "§6Day";
        else if (timeInDay < 13500) celestialPhase = "§cDusk";
        else celestialPhase = "§9Night";

        var biomeHolder = mc.level.getBiome(pos);
        String biomeName = biomeHolder.unwrapKey()
                .map(key -> key.location().getPath().replace('_', ' '))
                .orElse("Unknown");
        biomeName = capitalizeWords(biomeName);

        String dim = mc.level.dimension().location().getPath().replace('_', ' ');
        dim = capitalizeWords(dim);

        int elevation = pos.getY();
        int seaOffset = elevation - 63; // 63 is vanilla sea level

        String weather;
        if (mc.level.isThundering()) {
            weather = "§cThunderstorm";
        } else if (mc.level.isRaining()) {
            weather = "§9Rain";
        } else {
            weather = "§aClear";
        }

        int panelW = 206;
        int panelH = 46;
        int px = 16;
        int py = height - 54;

        guiGraphics.fill(px - 4, py - 4, px + panelW, py + panelH, BG_PANEL);
        guiGraphics.fill(px - 4, py - 4, px + panelW, py - 3, BORDER_PANEL);
        guiGraphics.fill(px - 4, py + panelH - 1, px + panelW, py + panelH, BORDER_PANEL);
        guiGraphics.fill(px - 4, py - 4, px - 3, py + panelH, BORDER_PANEL);

        String threatStr = rawLight > 4 ? "§aSafe" : "§cHostile Spawn Active";

        guiGraphics.drawString(mc.font, "§6[✦] TIME & ENVIRONMENT // TELEMETRY", px, py, COLOR_AMBER_BRIGHT, false);
        guiGraphics.drawString(mc.font, String.format("CLOCK: §f%s §7(Day %d) §8| %s", timeStr, dayCount + 1, celestialPhase), px, py + 11, COLOR_AMBER_DARK, false);
        guiGraphics.drawString(mc.font, String.format("BIOME: §f%s §8| §b%s §8| §f%dm §7(%+dm)", biomeName, dim, elevation, seaOffset), px, py + 21, COLOR_AMBER_DARK, false);
        guiGraphics.drawString(mc.font, String.format("METEOR: %s §8| LUX: §f%d §7(B:§f%d §7S:§f%d) §8| %s", weather, rawLight, blockLight, skyLight, threatStr), px, py + 31, COLOR_AMBER_DARK, false);
    }

    private static String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = true;
        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                nextUpper = true;
                sb.append(c);
            } else if (nextUpper) {
                sb.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
