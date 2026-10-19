package com.fende.obesecat.client;

import com.fende.obesecat.inventory.EchoingBlastChamberMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class EchoingBlastChamberScreen extends AbstractContainerScreen<EchoingBlastChamberMenu> {
    private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");

    // Palette: Sculk Deep Dark + Trinitite Emerald + Blaze Amber + Breeze Cyan
    private static final int BORDER = 0xFF142428;
    private static final int BACKGROUND = 0xFF0B1317;
    private static final int PANEL = 0xFF101C22;
    private static final int INNER = 0xFF060B0E;
    private static final int BLAZE_ACCENT = 0xFFFF7A00;
    private static final int BREEZE_ACCENT = 0xFF38BDF8;
    private static final int TRINITITE_ACCENT = 0xFF39FF14;
    private static final int SCULK_CYAN = 0xFF5EEAD4;
    private static final int TEXT_DIM = 0xFF94A3B8;

    public EchoingBlastChamberScreen(EchoingBlastChamberMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 168;
        inventoryLabelY = 74;
        titleLabelX = 10;
        titleLabelY = 7;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Tooltip for resonant compression meter
        if (mouseX >= leftPos + 70 && mouseX <= leftPos + 104 && mouseY >= topPos + 44 && mouseY <= topPos + 60) {
            int pct = menu.progressWidth() * 100 / 24;
            graphics.renderTooltip(font, List.of(
                    Component.literal("§bAcoustic Compression: §f" + pct + "%"),
                    Component.literal("§7Fusing Blaze heat & Breeze turbulence...")
            ), java.util.Optional.empty(), mouseX, mouseY);
        }

        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Main chamber casing
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, BORDER);
        g.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, BACKGROUND);

        // Upper processing chamber panel
        g.fill(leftPos + 8, topPos + 20, leftPos + imageWidth - 8, topPos + 72, PANEL);
        g.fill(leftPos + 10, topPos + 22, leftPos + imageWidth - 10, topPos + 70, INNER);

        // --- Dual Input Slots ---
        // Blaze slot (Slot 0, x=44, y=35)
        g.fill(leftPos + 42, topPos + 33, leftPos + 62, topPos + 53, BLAZE_ACCENT);
        drawSlot(g, 44, 35);
        g.drawString(font, "FIRE", leftPos + 16, topPos + 39, BLAZE_ACCENT, false);

        // Breeze slot (Slot 1, x=44, y=58)
        g.fill(leftPos + 42, topPos + 56, leftPos + 62, topPos + 76, BREEZE_ACCENT);
        drawSlot(g, 44, 58);
        g.drawString(font, "WIND", leftPos + 14, topPos + 62, BREEZE_ACCENT, false);

        // Converging acoustic channels
        g.fill(leftPos + 62, topPos + 43, leftPos + 70, topPos + 45, BLAZE_ACCENT);
        g.fill(leftPos + 62, topPos + 66, leftPos + 70, topPos + 68, BREEZE_ACCENT);
        g.fill(leftPos + 68, topPos + 45, leftPos + 70, topPos + 66, SCULK_CYAN);

        // --- Central Harmonic Resonance Meter ---
        g.fill(leftPos + 70, topPos + 48, leftPos + 106, topPos + 64, 0xFF02070A);
        g.fill(leftPos + 71, topPos + 49, leftPos + 105, topPos + 63, 0xFF06151B);

        int progress = menu.progressWidth(); // 0 to 24
        if (progress > 0) {
            int waveColor = (menu.isProcessing() && (System.currentTimeMillis() / 250) % 2 == 0) ? 0xFF00FFFF : TRINITITE_ACCENT;
            g.fill(leftPos + 76, topPos + 51, leftPos + 76 + progress, topPos + 61, waveColor);
            g.fill(leftPos + 73, topPos + 55, leftPos + 103, topPos + 57, 0xFF144552);
        } else {
            g.fill(leftPos + 73, topPos + 55, leftPos + 103, topPos + 57, 0xFF0B252D);
        }

        // Channel from meter to output
        g.fill(leftPos + 106, topPos + 55, leftPos + 114, topPos + 57, TRINITITE_ACCENT);

        // --- Output Slot (Slot 2, x=116, y=47) ---
        g.fill(leftPos + 114, topPos + 45, leftPos + 134, topPos + 65, TRINITITE_ACCENT);
        drawSlot(g, 116, 47);
        g.drawString(font, "ROD", leftPos + 138, topPos + 51, TRINITITE_ACCENT, false);

        // Player Inventory slots
        g.fill(leftPos + 6, topPos + 80, leftPos + imageWidth - 6, topPos + 162, INNER);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(g, 8 + col * 18, 84 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlot(g, 8 + col * 18, 142);
        }
    }

    private void drawSlot(GuiGraphics g, int x, int y) {
        g.blitSprite(SLOT, leftPos + x - 1, topPos + y - 1, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, SCULK_CYAN, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT_DIM, false);
    }
}
