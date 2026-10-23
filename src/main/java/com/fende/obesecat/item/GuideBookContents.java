package com.fende.obesecat.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WrittenBookContent;

public final class GuideBookContents {
    private GuideBookContents() {
    }

    public static WrittenBookContent createContent() {
        List<Filterable<Component>> pages = new ArrayList<>();

        // Page 1: Title & Overview
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lPROJECT OBESECAT§r\n" +
                "§8§oClassified Field Manual§r\n" +
                "§8-------------------§r\n\n" +
                "§0Underground research into anomalous feline mass and atomic containment has yielded revolutionary discoveries.\n\n" +
                "This guide details the §4critical steps§0 required to survive and harness the energy."
        )));

        // Page 2: Chapter 1 - The Manhattan Bunker
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lCH. 1: THE BUNKER§r\n" +
                "§8-------------------§r\n" +
                "§0Scattered across the Overworld surface lie fortified §4Manhattan Bunkers§0.\n\n" +
                "Inside, you will find the solitary §1Manhattan Physicist§0 and a dormant §2Fat Man§0.\n\n" +
                "Bring §a5 Emeralds§0 to purchase §2Plutonium Cat Food§0."
        )));

        // Page 3: Chapter 2 - Detonation Protocol
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lCH. 2: DETONATION§r\n" +
                "§8-------------------§r\n" +
                "§0Fat Man requires §23 cans§0 of Plutonium Cat Food to reach critical mass.\n\n" +
                "§4WARNING:§0 Lure him far from your base!\n\n" +
                "The 3rd meal triggers a §c10s fuse§0. Drink a §9Potion of Swiftness§0 and sprint §460m+§0 away!"
        )));

        // Page 4: Chapter 3 - Trinitite & The Cube
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lCH. 3: TRINITITE§r\n" +
                "§8-------------------§r\n" +
                "§0The blast excavates a colossal 100m crater, crystallizing §2Trinitite§0 at the epicenter.\n\n" +
                "Mine it with a §8Netherite Pickaxe§0.\n\n" +
                "Craft the §5Transmutation Cube§0:\n" +
                "§8• Chest\n" +
                "§8• Crafting Table\n" +
                "§8• Trinitite§r"
        )));

        // Page 5: Chapter 4 - The Secret Cow Level
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lCH. 4: COW LEVEL§r\n" +
                "§8-------------------§r\n" +
                "§0The Cube unlocks dimensional pathways:\n\n" +
                "§81.§0 Slay the §1Warden§0 for §6Wirt's Leg§0.\n" +
                "§82.§0 Barter with Nether §6Piglins§0 for the §5Tome of Town Portal§0.\n\n" +
                "Transmute both in the Cube to summon the §cCow King§0!"
        )));

        // Page 6: Chapter 5 - Holy Sword Skills
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lCH. 5: HOLY SWORD§r\n" +
                "§8-------------------§r\n" +
                "§0Found dormant in §3Ancient Cities§0.\n" +
                "Awaken in the Cube with Bovine tokens:\n\n" +
                "§b• Stasis Sword:§0 Freeze mobs & projectiles in a temporal dome.\n" +
                "§6• Split Punch:§0 Supersonic kinetic shockwave launches enemies.\n" +
                "§e• Crush Punch:§0 Ground slam earthquake stuns mobs.\n" +
                "§c• Lightning Stab:§0 Piercing celestial lightning arc.\n" +
                "§d• Holy Explosion:§0 Sacred radiant dome vaporizes undead mobs!§r"
        )));

        // Page 7: Chapter 6 - Mighty Sword & Skeleton King
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lCH. 6: MIGHTY SWORD§r\n" +
                "§8-------------------§r\n" +
                "§0Base found in Ancient Cities.\n\n" +
                "§4Summon Skeleton King:§0\n" +
                "§81. Find Skeleton Crown in End Cities.\n" +
                "§82. Build vertical T-altar: 4 Bone Blocks.\n" +
                "§83. Use Crown on center block!§0\n\n" +
                "§5King Drops & Mighty Skills:§0\n" +
                "§8• Cherche -> Shellbust Stab (Armor Pierce)\n" +
                "§8• Salty Rage -> Blastar Punch (Explosion)\n" +
                "§8• Chantage -> Hellcry Punch (Sonic Debuff)\n" +
                "§8• Setiemson -> Icewolf Bite (Frost Freeze)\n" +
                "§6+ Drops Quantum Jah Semiconductor!§r"
        )));

        // Page 8: Chapter 7 - Dark Sword & The Carver
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lCH. 7: DARK SWORD§r\n" +
                "§8-------------------§r\n" +
                "§0Base found in Ancient Cities.\n\n" +
                "§4Summon The Carver:§0\n" +
                "§81. Find Cursed Cleaver in Bastions.\n" +
                "§82. Place Anvil atop Sculk Catalyst.\n" +
                "§83. Strike Anvil with Cleaver + 30 XP Lvls!§0\n\n" +
                "§5Carver Drops & Dark Arts:§0\n" +
                "§8• Bloodied Hook -> Night Sword (Vampiric)\n" +
                "§8• Demon Heart -> Dark Sword (Abyssal Void)\n" +
                "§6+ Drops Atomic Ber Semiconductor!§r"
        )));

        // Page 9: Chapter 8 - Temporal Felines
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lCH. 8: TEMPORAL FELINES§r\n" +
                "§8-------------------§r\n" +
                "§0Adopt §2Domino§0 from any village for §a1 Emerald§0.\n\n" +
                "Channel Dragon's Breath & Trinitite in the Cube:\n\n" +
                "§6• Chrono Domino:§0\n" +
                "  Breath + Clock + Trinitite + Domino\n\n" +
                "§b• Atmospheric Domino:§0\n" +
                "  Breath + Heart of Sea + Trinitite + Domino\n\n" +
                "§c• Chronospheric:§0\n" +
                "  Chrono + Atmospheric\n" +
                "§4Warning: Chronosphere activated! Unlocks Domino Painting!§r"
        )));

        // Page 10: Chapter 9 - Tactical Canines
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lCH. 9: TACTICAL CANINES§r\n" +
                "§8-------------------§r\n" +
                "§0Adopt §2Paco§0 from any village for §a1 Emerald§0.\n\n" +
                "§6Tactical Field Upgrades:§0\n" +
                "§8• Concussive:§0 Paco + Piston\n" +
                "§8• Attack:§0 Concussive + Diamond\n" +
                "§8• Assault:§0 Attack + Nether Star\n" +
                "§8• Sniper:§0 Assault + Spyglass\n\n" +
                "§5Cube Transmutations:§0\n" +
                "§c• Hellhound:§0\n" +
                "  Paco + Blaze Powder + Dragon's Breath\n" +
                "§4• J. Paco Barkkenheimer:§0\n" +
                "  Paco + Oppenheimer's Hat (Awards Paco Painting!)§r"
        )));

        // Page 11: Chapter 10 - Mobility Felines & Runes
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lCH. 10: MOBILITY FELINES§r\n" +
                "§8-------------------§r\n" +
                "§0Adopt §2Ember§0 from any village for §a1 Emerald§0.\n\n" +
                "§6Field & Arcane Tools:§0\n" +
                "§b• Ember Singularity:§0\n" +
                "  Ember + 8 Eyes of Ender (Vacuum)\n\n" +
                "§d• Wormhole Ember:§0\n" +
                "  Ember + Tome of Town Portal (Return Home)\n\n" +
                "§5Cube Transmutation:§0\n" +
                "§c• Enigma Ember:§0\n" +
                "  Ember + 3 Semiconductors\n" +
                "§8• Quantum (Jah): Skeleton King\n" +
                "§8• Dimensional (Ith): Cow King\n" +
                "§8• Atomic (Ber): The Carver\n" +
                "§6Awards Ember Painting!§r"
        )));

        // Page 12: Chapter 11 - Sensory & Hazard Mitigation
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lCH. 11: HAZARD MITIGATION§r\n" +
                "§8-------------------§r\n" +
                "§0Adopt §6Dorito§0 from any village for §a1 Emerald§0.\n\n" +
                "§5Defensive Gadgets (Cube):§0\n" +
                "§6• Dorito's Paws:§0\n" +
                "  Dorito + Rabbit Foot + Feather\n" +
                "  §8Curios Feet. Converts lethal fall impact to hunger!§0\n\n" +
                "§3• Sculk Dorito's Paws:§0\n" +
                "  Paws + Sculk Catalyst\n" +
                "  §8Total vibration silence to Wardens while fed!§0\n\n" +
                "§6• Tiny Planet:§0\n" +
                "  Dorito + Arrow + Fire Charge + Lodestone + Gold Block\n" +
                "  §85s orbital shields: deflects projectiles into hunger!§r"
        )));

        // Page 13: Chapter 12 - Tactical Telemetry & Optics
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lCH. 12: TACTICAL VISORS§r\n" +
                "§8-------------------§r\n" +
                "§0Craft or Transmute in the Cube:\n\n" +
                "§a• Night Vision Dorito (Green):§0\n" +
                "  Dorito + Glass + Amethyst + Quantum (Jah)\n" +
                "  §8Curios Head. NVG optics & LUX meter!§0\n\n" +
                "§6• Telemetry Visor (Yellow):§0\n" +
                "  Dorito + Glass + Amethyst + Dimensional (Ith)\n" +
                "  §8Coordinates, heading, chunk radar, & clock!§0\n\n" +
                "§b• Scanner Visor (Cyan):§0\n" +
                "  Dorito + Glass + Amethyst + Atomic (Ber)\n" +
                "  §83D floating cyan tags: Name, HP, Armor!§r"
        )));

        // Page 14: Chapter 13 - The Omni-Visor Ultimate Form
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lCH. 13: THE OMNI-VISOR§r\n" +
                "§8-------------------§r\n" +
                "§5Cube Transmutation:§0\n" +
                "§0Combine all 3 visors:\n" +
                "§a• Night Vision Dorito (Green)\n" +
                "§6• Telemetry Visor Dorito (Yellow)\n" +
                "§b• Scanner Visor Dorito (Cyan)\n" +
                "§8• Netherite Ingot§0\n\n" +
                "§6The Omni-Visor fuses all 3 sensory feeds with zero overlap or hunger drain!§r\n\n" +
                "§d★ Unlocks the Dorito Painting!§r"
        )));

        // Page 15: Chapter 14 - Tabbyrium Grid & Silos
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lCH. 14: TABBYRIUM GRID§r\n" +
                "§8-------------------§r\n" +
                "§0Craft Sapling: 4 Trinitite, 2 Overdrive Powder, Redstone Dust, & Sapling.\n\n" +
                "§6• Refinery:§0 Refines Shards & Crystals to FE.\n\n" +
                "§a• Tabbyrium Silo:§0\n" +
                "  Iron + Copper + Trinitite\n" +
                "  §8Stores 1,000,000 FE with live fill gauge.\n" +
                "  Right-click opens the Charging Console to charge held gear & Portable Chargers!\n" +
                "  §cEVA Warning: SILOS NEEDED when full!§r"
        )));

        // Page 16: Chapter 15 - Magicite Summons & Relics
        pages.add(Filterable.passThrough(Component.literal(
                "§1§lCH. 15: MAGICITE & RELICS§r\n" +
                "§8-------------------§r\n" +
                "§5Magicite Summons (10k FE):§0\n" +
                "§8• Praxis: Blue Ice (Frost pulses)\n" +
                "§8• Veritas: Amethyst (Purifier)\n" +
                "§8• Paradox: Eye of Ender (Reality shear)\n" +
                "§8• Aequitas: Soul Campfire (Soul storm)\n\n" +
                "§2Shelters & Dungeon Jokes:§0\n" +
                "§8• Emergency Home: 1 Emerald novice trade.\n" +
                "§8• Luxury Home: Emergency + Iron/Gold/Diamond/Emerald Blocks!\n" +
                "§8• Dungeon Toilet & Chocolate Pudding: Rare joke loot in dungeons!§r"
        )));

        return new WrittenBookContent(
                Filterable.passThrough("Manhattan Project Field Guide"),
                "J. Robert Oppenheimer",
                0,
                pages,
                true
        );
    }
}
