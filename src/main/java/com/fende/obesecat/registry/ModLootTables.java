package com.fende.obesecat.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.event.LootTableLoadEvent;

public final class ModLootTables {
    private static final ResourceKey<LootTable> WARDEN_LOOT_TABLE = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.withDefaultNamespace("entities/warden")
    );
    private static final String DUNGEON_TOILET_POOL = "obesecat:dungeon_toilet";
    private static final String DUNGEON_MR_KITTYS_PAWS_POOL = "obesecat:dungeon_mr_kittys_paws";
    private static final String END_CITY_LITHIUM_POOL = "obesecat:end_city_lithium_deuteride";
    private static final String END_CITY_WORMHOLE_EMBER_POOL = "obesecat:end_city_wormhole_ember";
    private static final String END_CITY_ENIGMA_EMBER_POOL = "obesecat:end_city_enigma_ember";
    private static final String ANCIENT_CITY_HOLY_SWORD_POOL = "obesecat:ancient_city_holy_sword";
    private static final String BASTION_NIGHT_VISION_MR_KITTY_POOL = "obesecat:bastion_night_vision_mr_kitty";
    private static final String WARDEN_VIRTS_LEG_POOL = "obesecat:warden_virts_leg";
    private static final String PIGLIN_BARTER_TP_TOME_POOL = "obesecat:piglin_barter_tp_tome";

    public static void addLoot(LootTableLoadEvent event) {
        if (event.getKey().equals(BuiltInLootTables.SIMPLE_DUNGEON) && event.getTable().getPool(DUNGEON_TOILET_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(DUNGEON_TOILET_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.22F))
                    .add(LootItem.lootTableItem(ModItems.TOILET.get()))
                    .build());
        }

        if (event.getKey().equals(BuiltInLootTables.SIMPLE_DUNGEON) && event.getTable().getPool(DUNGEON_MR_KITTYS_PAWS_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(DUNGEON_MR_KITTYS_PAWS_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.08F))
                    .add(LootItem.lootTableItem(ModItems.MR_KITTYS_PAWS.get()))
                    .build());
        }

        if (event.getKey().equals(BuiltInLootTables.END_CITY_TREASURE) && event.getTable().getPool(END_CITY_LITHIUM_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(END_CITY_LITHIUM_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.28F))
                    .add(LootItem.lootTableItem(ModItems.LITHIUM_DEUTERIDE_CAT_FOOD.get()))
                    .build());
        }

        if (event.getKey().equals(BuiltInLootTables.END_CITY_TREASURE) && event.getTable().getPool(END_CITY_WORMHOLE_EMBER_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(END_CITY_WORMHOLE_EMBER_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.18F))
                    .add(LootItem.lootTableItem(ModItems.WORMHOLE_EMBER.get()))
                    .build());
        }

        if (event.getKey().equals(BuiltInLootTables.END_CITY_TREASURE) && event.getTable().getPool(END_CITY_ENIGMA_EMBER_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(END_CITY_ENIGMA_EMBER_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.18F))
                    .add(LootItem.lootTableItem(ModItems.ENIGMA_EMBER.get()))
                    .build());
        }

        if (event.getKey().equals(BuiltInLootTables.ANCIENT_CITY) && event.getTable().getPool(ANCIENT_CITY_HOLY_SWORD_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(ANCIENT_CITY_HOLY_SWORD_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.12F))
                    .add(LootItem.lootTableItem(ModItems.HOLY_SWORD.get()))
                    .build());
        }

        if (isBastionLoot(event) && event.getTable().getPool(BASTION_NIGHT_VISION_MR_KITTY_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(BASTION_NIGHT_VISION_MR_KITTY_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.12F))
                    .add(LootItem.lootTableItem(ModItems.NIGHT_VISION_MR_KITTY.get()))
                    .build());
        }

        if (event.getKey().equals(WARDEN_LOOT_TABLE) && event.getTable().getPool(WARDEN_VIRTS_LEG_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(WARDEN_VIRTS_LEG_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootItem.lootTableItem(ModItems.VIRTS_LEG.get()))
                    .build());
        }

        if (event.getKey().equals(BuiltInLootTables.PIGLIN_BARTERING) && event.getTable().getPool(PIGLIN_BARTER_TP_TOME_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(PIGLIN_BARTER_TP_TOME_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.05F))
                    .add(LootItem.lootTableItem(ModItems.TP_TOME.get()))
                    .build());
        }
    }

    private static boolean isBastionLoot(LootTableLoadEvent event) {
        return event.getKey().equals(BuiltInLootTables.BASTION_TREASURE)
                || event.getKey().equals(BuiltInLootTables.BASTION_OTHER)
                || event.getKey().equals(BuiltInLootTables.BASTION_BRIDGE)
                || event.getKey().equals(BuiltInLootTables.BASTION_HOGLIN_STABLE);
    }

    private ModLootTables() {
    }
}
