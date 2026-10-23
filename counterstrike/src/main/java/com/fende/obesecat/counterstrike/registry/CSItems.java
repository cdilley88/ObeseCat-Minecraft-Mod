package com.fende.obesecat.counterstrike.registry;

import com.fende.obesecat.counterstrike.ObeseCatCounterstrike;
import com.fende.obesecat.counterstrike.item.CommandCursorItem;
import com.fende.obesecat.counterstrike.item.MeowbileConstructionVehicleItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CSItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ObeseCatCounterstrike.MOD_ID);

    public static final DeferredItem<CommandCursorItem> COMMAND_CURSOR = ITEMS.registerItem(
            "command_cursor",
            CommandCursorItem::new
    );

    public static final DeferredItem<MeowbileConstructionVehicleItem> MEOWBILE_CONSTRUCTION_VEHICLE = ITEMS.register(
            "meowbile_construction_vehicle",
            () -> new MeowbileConstructionVehicleItem(CSBlocks.MEOWBILE_CONSTRUCTION_VEHICLE.get())
    );

    public static final DeferredItem<BlockItem> CONSTRUCTION_YARD_CORE = ITEMS.register(
            "construction_yard_core",
            () -> new BlockItem(CSBlocks.CONSTRUCTION_YARD_CORE.get(), new Item.Properties().rarity(Rarity.EPIC))
    );

    public static final DeferredItem<BlockItem> CONSTRUCTION_YARD_HULL = ITEMS.register(
            "construction_yard_hull",
            () -> new BlockItem(CSBlocks.CONSTRUCTION_YARD_HULL.get(), new Item.Properties().rarity(Rarity.UNCOMMON))
    );

    public static final DeferredItem<net.minecraft.world.item.DoubleHighBlockItem> PNEUMATIC_DOOR = ITEMS.register(
            "pneumatic_door",
            () -> new net.minecraft.world.item.DoubleHighBlockItem(CSBlocks.PNEUMATIC_DOOR.get(), new Item.Properties().rarity(Rarity.UNCOMMON))
    );

    public static final DeferredItem<BlockItem> FISSION_TURBINE_CRATE = ITEMS.register(
            "fission_turbine_crate",
            () -> new BlockItem(CSBlocks.FISSION_TURBINE_CRATE.get(), new Item.Properties().rarity(Rarity.EPIC))
    );

    public static final DeferredItem<BlockItem> FISSION_TURBINE_CORE = ITEMS.register(
            "fission_turbine_core",
            () -> new BlockItem(CSBlocks.FISSION_TURBINE_CORE.get(), new Item.Properties().rarity(Rarity.RARE))
    );

    public static final DeferredItem<BlockItem> TURBINE_REACTOR_CORE = ITEMS.register(
            "turbine_reactor_core",
            () -> new BlockItem(CSBlocks.TURBINE_REACTOR_CORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON))
    );

    public static final DeferredItem<BlockItem> BARRACKS_CRATE = ITEMS.register(
            "barracks_crate",
            () -> new BlockItem(CSBlocks.BARRACKS_CRATE.get(), new Item.Properties().rarity(Rarity.EPIC))
    );

    public static final DeferredItem<BlockItem> BARRACKS_CORE = ITEMS.register(
            "barracks_core",
            () -> new BlockItem(CSBlocks.BARRACKS_CORE.get(), new Item.Properties().rarity(Rarity.RARE))
    );

    public static final DeferredItem<BlockItem> ARMORY_LOCKER = ITEMS.register(
            "armory_locker",
            () -> new BlockItem(CSBlocks.ARMORY_LOCKER.get(), new Item.Properties().rarity(Rarity.UNCOMMON))
    );

    public static final DeferredItem<com.fende.obesecat.counterstrike.item.AutomaticRifleItem> AUTOMATIC_RIFLE = ITEMS.register(
            "automatic_rifle",
            () -> new com.fende.obesecat.counterstrike.item.AutomaticRifleItem()
    );

    public static final DeferredItem<com.fende.obesecat.counterstrike.item.RocketLauncherItem> ROCKET_LAUNCHER = ITEMS.register(
            "rocket_launcher",
            () -> new com.fende.obesecat.counterstrike.item.RocketLauncherItem()
    );

    public static final DeferredItem<com.fende.obesecat.counterstrike.item.FragGrenadeItem> FRAG_GRENADE = ITEMS.register(
            "frag_grenade",
            () -> new com.fende.obesecat.counterstrike.item.FragGrenadeItem()
    );

    public static final DeferredItem<com.fende.obesecat.counterstrike.item.FlamethrowerItem> FLAMETHROWER = ITEMS.register(
            "flamethrower",
            () -> new com.fende.obesecat.counterstrike.item.FlamethrowerItem()
    );

    public static final DeferredItem<com.fende.obesecat.counterstrike.item.CommandoRifleItem> COMMANDO_RIFLE = ITEMS.register(
            "commando_rifle",
            () -> new com.fende.obesecat.counterstrike.item.CommandoRifleItem()
    );

    private CSItems() {
    }
}
