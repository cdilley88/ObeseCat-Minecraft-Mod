package com.fende.obesecat.item;

import com.fende.obesecat.registry.ModSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class DominoItem extends PacoItem {
    public DominoItem(Properties properties) {
        super(properties);
    }

    @Override
    protected SoundEvent getBarkSound(Level level) {
        return switch (level.random.nextInt(3)) {
            case 0 -> ModSounds.DOMINO_MEOW_1.get();
            case 1 -> ModSounds.DOMINO_MEOW_2.get();
            default -> ModSounds.DOMINO_MEOW_3.get();
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable(getDescriptionId(stack) + ".caption").withStyle(ChatFormatting.YELLOW));
    }
}
