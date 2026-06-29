package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.item.AssaultPacoItem;
import com.fende.obesecat.item.AtomicPacoItem;
import com.fende.obesecat.item.AttackPacoItem;
import com.fende.obesecat.item.BigFireBoomStickItem;
import com.fende.obesecat.item.BoomStickItem;
import com.fende.obesecat.item.CaptionedItem;
import com.fende.obesecat.item.ConcussivePacoItem;
import com.fende.obesecat.item.EmberSingularityItem;
import com.fende.obesecat.item.EnigmaEmberItem;
import com.fende.obesecat.item.FireBoomStickItem;
import com.fende.obesecat.item.FireStickItem;
import com.fende.obesecat.item.HellhoundPacoItem;
import com.fende.obesecat.item.JRobertPacoheimerItem;
import com.fende.obesecat.item.MrKittysPawsItem;
import com.fende.obesecat.item.ManhattanPhysicistSpawnEggItem;
import com.fende.obesecat.item.NightVisionMrKittyItem;
import com.fende.obesecat.item.PacoItem;
import com.fende.obesecat.item.SniperPacoItem;
import com.fende.obesecat.item.TinyPlanetItem;
import com.fende.obesecat.item.TransmutationCubeItem;
import com.fende.obesecat.item.WormholeEmberItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ObeseCatMod.MOD_ID);

    public static final DeferredItem<SpawnEggItem> OBESE_CAT_SPAWN_EGG = ITEMS.registerItem(
            "obese_cat_spawn_egg",
            properties -> new SpawnEggItem(ModEntities.OBESE_CAT.get(), 0xD66B24, 0xFFF1D6, properties)
    );

    public static final DeferredItem<ManhattanPhysicistSpawnEggItem> MANHATTAN_PHYSICIST_SPAWN_EGG = ITEMS.registerItem(
            "manhattan_physicist_spawn_egg",
            ManhattanPhysicistSpawnEggItem::new,
            new Item.Properties().stacksTo(64)
    );

    public static final DeferredItem<Item> PLUTONIUM_CAT_FOOD = ITEMS.registerSimpleItem(
            "plutonium_cat_food",
            new Item.Properties().rarity(Rarity.RARE)
    );

    public static final DeferredItem<Item> LITHIUM_DEUTERIDE_CAT_FOOD = ITEMS.registerSimpleItem(
            "lithium_deuteride_cat_food",
            new Item.Properties().rarity(Rarity.EPIC)
    );

    public static final DeferredItem<PacoItem> PACO = ITEMS.registerItem(
            "paco",
            PacoItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<CaptionedItem> EMBER = ITEMS.registerItem(
            "ember",
            properties -> new CaptionedItem(properties, "item.obesecat.ember.caption"),
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<CaptionedItem> MR_KITTY = ITEMS.registerItem(
            "mr_kitty",
            properties -> new CaptionedItem(properties, "item.obesecat.mr_kitty.caption"),
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<NightVisionMrKittyItem> NIGHT_VISION_MR_KITTY = ITEMS.registerItem(
            "night_vision_mr_kitty",
            NightVisionMrKittyItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<MrKittysPawsItem> MR_KITTYS_PAWS = ITEMS.registerItem(
            "mr_kittys_paws",
            MrKittysPawsItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<EmberSingularityItem> EMBER_SINGULARITY = ITEMS.registerItem(
            "ember_singularity",
            EmberSingularityItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<WormholeEmberItem> WORMHOLE_EMBER = ITEMS.registerItem(
            "wormhole_ember",
            WormholeEmberItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
    );

    public static final DeferredItem<EnigmaEmberItem> ENIGMA_EMBER = ITEMS.registerItem(
            "enigma_ember",
            EnigmaEmberItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
    );

    public static final DeferredItem<TinyPlanetItem> TINY_PLANET = ITEMS.registerItem(
            "tiny_planet",
            TinyPlanetItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<TransmutationCubeItem> TRANSMUTATION_CUBE = ITEMS.registerItem(
            "transmutation_cube",
            TransmutationCubeItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<ConcussivePacoItem> CONCUSSIVE_PACO = ITEMS.registerItem(
            "concussive_paco",
            ConcussivePacoItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
    );

    public static final DeferredItem<AttackPacoItem> ATTACK_PACO = ITEMS.registerItem(
            "attack_paco",
            AttackPacoItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
    );

    public static final DeferredItem<AssaultPacoItem> ASSAULT_PACO = ITEMS.registerItem(
            "assault_paco",
            AssaultPacoItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<SniperPacoItem> SNIPER_PACO = ITEMS.registerItem(
            "sniper_paco",
            SniperPacoItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<JRobertPacoheimerItem> J_ROBERT_PACOHEIMER = ITEMS.registerItem(
            "j_robert_pacoheimer",
            JRobertPacoheimerItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
    );

    public static final DeferredItem<AtomicPacoItem> ATOMIC_PACO = ITEMS.registerItem(
            "atomic_paco",
            AtomicPacoItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
    );

    public static final DeferredItem<HellhoundPacoItem> HELLHOUND_PACO = ITEMS.registerItem(
            "hellhound_paco",
            HellhoundPacoItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<Item> OPPENHEIMERS_HAT = ITEMS.registerSimpleItem(
            "oppenheimers_hat",
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<BoomStickItem> BOOM_STICK = ITEMS.registerItem(
            "boom_stick",
            BoomStickItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<FireStickItem> FIRE_STICK = ITEMS.registerItem(
            "fire_stick",
            FireStickItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<FireBoomStickItem> FIRE_BOOM_STICK = ITEMS.registerItem(
            "fire_boom_stick",
            FireBoomStickItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<BigFireBoomStickItem> BIG_FIRE_BOOM_STICK = ITEMS.registerItem(
            "big_fire_boom_stick",
            BigFireBoomStickItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<BlockItem> TOILET = ITEMS.register(
            "toilet",
            () -> new BlockItem(ModBlocks.TOILET.get(), new Item.Properties())
    );

    public static final DeferredItem<BlockItem> TRINITITE = ITEMS.register(
            "trinitite",
            () -> new BlockItem(ModBlocks.TRINITITE.get(), new Item.Properties().rarity(Rarity.RARE))
    );

    public static final DeferredItem<BlockItem> NUCLEAR_LIBRARY = ITEMS.register(
            "nuclear_library",
            () -> new BlockItem(ModBlocks.NUCLEAR_LIBRARY.get(), new Item.Properties().rarity(Rarity.UNCOMMON))
    );

    private ModItems() {
    }
}
