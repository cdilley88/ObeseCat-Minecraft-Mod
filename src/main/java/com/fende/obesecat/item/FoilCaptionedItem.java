package com.fende.obesecat.item;

import net.minecraft.world.item.ItemStack;

public class FoilCaptionedItem extends CaptionedItem {
    public FoilCaptionedItem(Properties properties, String captionKey) {
        super(properties, captionKey);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
