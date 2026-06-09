package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.item.PacoItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ObeseCatMod.MOD_ID);

    public static final DeferredItem<SpawnEggItem> OBESE_CAT_SPAWN_EGG = ITEMS.registerItem(
            "obese_cat_spawn_egg",
            properties -> new SpawnEggItem(ModEntities.OBESE_CAT.get(), 0xD66B24, 0xFFF1D6, properties)
    );

    public static final DeferredItem<Item> PLUTONIUM_CAT_FOOD = ITEMS.registerSimpleItem(
            "plutonium_cat_food",
            new Item.Properties()
    );

    public static final DeferredItem<Item> LITHIUM_DEUTERIDE_CAT_FOOD = ITEMS.registerSimpleItem(
            "lithium_deuteride_cat_food",
            new Item.Properties()
    );

    public static final DeferredItem<PacoItem> PACO = ITEMS.registerItem(
            "paco",
            PacoItem::new,
            new Item.Properties().stacksTo(1)
    );

    private ModItems() {
    }
}
