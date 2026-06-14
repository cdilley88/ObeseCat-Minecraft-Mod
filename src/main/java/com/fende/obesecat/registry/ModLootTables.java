package com.fende.obesecat.registry;

import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.event.LootTableLoadEvent;

public final class ModLootTables {
    private static final String DUNGEON_TOILET_POOL = "obesecat:dungeon_toilet";
    private static final String END_CITY_LITHIUM_POOL = "obesecat:end_city_lithium_deuteride";
    private static final String END_CITY_WORMHOLE_EMBER_POOL = "obesecat:end_city_wormhole_ember";

    public static void addLoot(LootTableLoadEvent event) {
        if (event.getKey().equals(BuiltInLootTables.SIMPLE_DUNGEON) && event.getTable().getPool(DUNGEON_TOILET_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(DUNGEON_TOILET_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.22F))
                    .add(LootItem.lootTableItem(ModItems.TOILET.get()))
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
    }

    private ModLootTables() {
    }
}
