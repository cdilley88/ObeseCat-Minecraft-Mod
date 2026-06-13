package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.item.AssaultPacoItem;
import com.fende.obesecat.item.AtomicPacoItem;
import com.fende.obesecat.item.AttackPacoItem;
import com.fende.obesecat.item.BoomStickItem;
import com.fende.obesecat.item.ConcussivePacoItem;
import com.fende.obesecat.item.FireBoomStickItem;
import com.fende.obesecat.item.FireStickItem;
import com.fende.obesecat.item.HellhoundPacoItem;
import com.fende.obesecat.item.JRobertPacoheimerItem;
import com.fende.obesecat.item.PacoItem;
import net.minecraft.world.item.BlockItem;
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

    public static final DeferredItem<ConcussivePacoItem> CONCUSSIVE_PACO = ITEMS.registerItem(
            "concussive_paco",
            ConcussivePacoItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<AttackPacoItem> ATTACK_PACO = ITEMS.registerItem(
            "attack_paco",
            AttackPacoItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<AssaultPacoItem> ASSAULT_PACO = ITEMS.registerItem(
            "assault_paco",
            AssaultPacoItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<JRobertPacoheimerItem> J_ROBERT_PACOHEIMER = ITEMS.registerItem(
            "j_robert_pacoheimer",
            JRobertPacoheimerItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<AtomicPacoItem> ATOMIC_PACO = ITEMS.registerItem(
            "atomic_paco",
            AtomicPacoItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<HellhoundPacoItem> HELLHOUND_PACO = ITEMS.registerItem(
            "hellhound_paco",
            HellhoundPacoItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<BoomStickItem> BOOM_STICK = ITEMS.registerItem(
            "boom_stick",
            BoomStickItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<FireStickItem> FIRE_STICK = ITEMS.registerItem(
            "fire_stick",
            FireStickItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<FireBoomStickItem> FIRE_BOOM_STICK = ITEMS.registerItem(
            "fire_boom_stick",
            FireBoomStickItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<BlockItem> TOILET = ITEMS.register(
            "toilet",
            () -> new BlockItem(ModBlocks.TOILET.get(), new Item.Properties())
    );

    private ModItems() {
    }
}
