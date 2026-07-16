package com.fende.obesecat.client;

import com.fende.obesecat.inventory.TargetDummyMenu;
import com.fende.obesecat.network.TargetDummyInfoCardPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class TargetDummyScreen extends AbstractContainerScreen<TargetDummyMenu> {
    private static final int PANEL = 0xF21A202A;
    private static final int PANEL_INNER = 0xFF252E3A;
    private static final int CYAN = 0xFF42D9E8;
    private static final int ORANGE = 0xFFFFA23A;
    private static final int SLOT_BORDER = 0xFF76879A;
    private static final int SLOT_DARK = 0xFF111820;
    private Button infoCardButton;

    public TargetDummyScreen(TargetDummyMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 184;
        titleLabelX = 12;
        titleLabelY = 8;
        inventoryLabelX = 8;
        inventoryLabelY = 90;
    }

    @Override
    protected void init() {
        super.init();
        infoCardButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            int entityId = menu.getDummyEntityId();
            if (entityId >= 0) {
                boolean enabled = !menu.isInfoCardEnabled();
                menu.setInfoCardEnabled(enabled);
                PacketDistributor.sendToServer(new TargetDummyInfoCardPayload(entityId, enabled));
            }
        }).bounds(leftPos + 101, topPos + 62, 65, 16).build());
        updateInfoButton();
    }

    private void updateInfoButton() {
        if (infoCardButton != null) {
            infoCardButton.setMessage(Component.literal(menu.isInfoCardEnabled() ? "CARD: ON" : "CARD: OFF"));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, PANEL);
        graphics.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + imageHeight - 3, PANEL_INNER);
        graphics.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + 5, CYAN);
        graphics.fill(leftPos + 3, topPos + 82, leftPos + imageWidth - 3, topPos + 84, ORANGE);

        graphics.drawString(font, "ARMOR LOADOUT", leftPos + 30, topPos + 70, 0xFFA9F5FC, false);
        graphics.drawString(font, "HAND SLOTS", leftPos + 105, topPos + 18, 0xFFFFC978, false);

        for (int i = 0; i < menu.slots.size(); i++) {
            var slot = menu.slots.get(i);
            drawSlot(graphics, leftPos + slot.x, topPos + slot.y, i < 6 ? (i < 4 ? CYAN : ORANGE) : SLOT_BORDER);
        }

        drawSlotLabel(graphics, "HEAD", 65, 21);
        drawSlotLabel(graphics, "CHEST", 65, 39);
        drawSlotLabel(graphics, "LEGS", 65, 57);
        drawSlotLabel(graphics, "FEET", 65, 75);
        drawSlotLabel(graphics, "MAIN", 138, 39);
        drawSlotLabel(graphics, "OFF", 138, 75);
        graphics.drawString(font, "INVENTORY", leftPos + inventoryLabelX, topPos + inventoryLabelY,
                0xFFE9EEF5, false);
        graphics.drawString(font, "Sneak + Right Click: Pack Up", leftPos + 12, topPos + 174,
                0xFF8EA0B4, false);
    }

    private void drawSlot(GuiGraphics graphics, int x, int y, int accent) {
        graphics.fill(x - 2, y - 2, x + 18, y + 18, 0xFF090D12);
        graphics.fill(x - 1, y - 1, x + 17, y + 17, accent);
        graphics.fill(x, y, x + 16, y + 16, SLOT_DARK);
        graphics.fill(x + 1, y + 1, x + 15, y + 15, 0xFF1B2632);
    }

    private void drawSlotLabel(GuiGraphics graphics, String text, int x, int y) {
        graphics.drawString(font, text, leftPos + x, topPos + y, 0xFFB8C6D6, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateInfoButton();
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}


