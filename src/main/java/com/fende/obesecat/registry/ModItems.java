package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.item.AssaultPacoItem;
import com.fende.obesecat.item.AtomicPacoItem;
import com.fende.obesecat.item.AttackPacoItem;
import com.fende.obesecat.item.BigFireBoomStickItem;
import com.fende.obesecat.item.BoomStickItem;
import com.fende.obesecat.item.CaptionedItem;
import com.fende.obesecat.item.CowLevelPortalItem;
import com.fende.obesecat.item.ConcussivePacoItem;
import com.fende.obesecat.item.DominoItem;
import com.fende.obesecat.item.EmberSingularityItem;
import com.fende.obesecat.item.EnigmaEmberItem;
import com.fende.obesecat.item.FireBoomStickItem;
import com.fende.obesecat.item.FireStickItem;
import com.fende.obesecat.item.HellhoundPacoItem;
import com.fende.obesecat.item.HolySwordItem;
import com.fende.obesecat.item.JRobertPacoheimerItem;
import com.fende.obesecat.item.MrKittysPawsItem;
import com.fende.obesecat.item.ManhattanPhysicistSpawnEggItem;
import com.fende.obesecat.item.NightVisionMrKittyItem;
import com.fende.obesecat.item.PacoItem;
import com.fende.obesecat.item.SniperPacoItem;
import com.fende.obesecat.item.SplitPunchSwordItem;
import com.fende.obesecat.item.StasisSwordItem;
import com.fende.obesecat.item.TimeDominoItem;
import com.fende.obesecat.item.TinyPlanetItem;
import com.fende.obesecat.item.TransmutationCubeItem;
import com.fende.obesecat.item.WeatherDominoItem;
import com.fende.obesecat.item.WormholeEmberItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ObeseCatMod.MOD_ID);

    public static final DeferredItem<SpawnEggItem> OBESE_CAT_SPAWN_EGG = ITEMS.registerItem(
            "obese_cat_spawn_egg",
            properties -> new SpawnEggItem(ModEntities.OBESE_CAT.get(), 0xD66B24, 0xFFF1D6, properties)
    );

    public static final DeferredItem<SpawnEggItem> COW_KING_SPAWN_EGG = ITEMS.registerItem(
            "cow_king_spawn_egg",
            properties -> new SpawnEggItem(ModEntities.COW_KING.get(), 0x8B6914, 0xFFD700, properties)
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

    public static final DeferredItem<DominoItem> DOMINO = ITEMS.registerItem(
            "domino",
            DominoItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
    );

    public static final DeferredItem<TimeDominoItem> DAWN_DOMINO = ITEMS.registerItem(
            "dawn_domino",
            properties -> new TimeDominoItem(properties, 1000L),
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
    );

    public static final DeferredItem<TimeDominoItem> MIDDAY_DOMINO = ITEMS.registerItem(
            "midday_domino",
            properties -> new TimeDominoItem(properties, 6000L),
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
    );

    public static final DeferredItem<TimeDominoItem> DUSK_DOMINO = ITEMS.registerItem(
            "dusk_domino",
            properties -> new TimeDominoItem(properties, 13000L),
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
    );

    public static final DeferredItem<TimeDominoItem> MIDNIGHT_DOMINO = ITEMS.registerItem(
            "midnight_domino",
            properties -> new TimeDominoItem(properties, 18000L),
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
    );

    public static final DeferredItem<WeatherDominoItem> CALM_DOMINO = ITEMS.registerItem(
            "calm_domino",
            properties -> new WeatherDominoItem(properties, false, false),
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
    );

    public static final DeferredItem<WeatherDominoItem> DOWNPOUR_DOMINO = ITEMS.registerItem(
            "downpour_domino",
            properties -> new WeatherDominoItem(properties, true, false),
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
    );

    public static final DeferredItem<WeatherDominoItem> THUNDERSTORM_DOMINO = ITEMS.registerItem(
            "thunderstorm_domino",
            properties -> new WeatherDominoItem(properties, true, true),
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
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

    public static final DeferredItem<CaptionedItem> VIRTS_LEG = ITEMS.registerItem(
            "virts_leg",
            properties -> new CaptionedItem(properties, "item.obesecat.virts_leg.caption"),
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<CaptionedItem> TP_TOME = ITEMS.registerItem(
            "tp_tome",
            properties -> new CaptionedItem(properties, "item.obesecat.tp_tome.caption"),
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<CowLevelPortalItem> COW_LEVEL_PORTAL = ITEMS.registerItem(
            "cow_level_portal",
            CowLevelPortalItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
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

    public static final DeferredItem<CaptionedItem> SAMMYS_CROSS = ITEMS.registerItem(
            "sammy_cross",
            properties -> new CaptionedItem(properties, "item.obesecat.sammy_cross.caption"),
            new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
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
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE).craftRemainder(Items.SPYGLASS)
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

    public static final DeferredItem<HolySwordItem> HOLY_SWORD = ITEMS.registerItem(
            "holy_sword",
            HolySwordItem::new,
            skillSwordProperties(Rarity.UNCOMMON)
    );

    public static final DeferredItem<CaptionedItem> HOLY_KNIGHT_TOKEN = ITEMS.registerItem(
            "holy_knight_token",
            properties -> new CaptionedItem(properties, "item.obesecat.holy_knight_token.caption"),
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
    );

    public static final DeferredItem<CaptionedItem> WHITE_KNIGHT_SYMBOL = ITEMS.registerItem(
            "white_knight_symbol",
            properties -> new CaptionedItem(properties, "item.obesecat.white_knight_symbol.caption"),
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

    public static final DeferredItem<StasisSwordItem> STASIS_SWORD = ITEMS.registerItem(
            "stasis_sword",
            StasisSwordItem::new,
            skillSwordProperties(Rarity.RARE)
    );

    public static final DeferredItem<SplitPunchSwordItem> SPLIT_PUNCH = ITEMS.registerItem(
            "split_punch",
            SplitPunchSwordItem::new,
            skillSwordProperties(Rarity.RARE)
    );

    // Internal display-only item — carries the SplitPunchGFX texture for the cast ItemDisplay entity.
    public static final DeferredItem<Item> SPLIT_PUNCH_GFX = ITEMS.registerSimpleItem(
            "split_punch_gfx",
            new Item.Properties().stacksTo(1)
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

    private static Item.Properties skillSwordProperties(Rarity rarity) {
        return new Item.Properties()
                .stacksTo(1)
                .rarity(rarity)
                .attributes(SwordItem.createAttributes(Tiers.DIAMOND, 3, -2.4F))
                .component(DataComponents.TOOL, SwordItem.createToolProperties());
    }
}
