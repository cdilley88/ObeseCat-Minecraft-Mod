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
    private static final String DUNGEON_CHOCOLATE_PUDDING_POOL = "obesecat:dungeon_chocolate_pudding";
    private static final String END_CITY_LITHIUM_POOL = "obesecat:end_city_lithium_deuteride";
    private static final String END_CITY_SKELETON_CROWN_POOL = "obesecat:end_city_skeleton_crown";
    private static final String ANCIENT_CITY_HOLY_SWORD_POOL = "obesecat:ancient_city_holy_sword";
    private static final String ANCIENT_CITY_MIGHTY_SWORD_POOL = "obesecat:ancient_city_mighty_sword";
    private static final String ANCIENT_CITY_DARK_SWORD_POOL = "obesecat:ancient_city_dark_sword";
    private static final String BASTION_CURSED_CLEAVER_POOL = "obesecat:bastion_cursed_cleaver";
    private static final String WARDEN_VIRTS_LEG_POOL = "obesecat:warden_virts_leg";
    private static final String PIGLIN_BARTER_TP_TOME_POOL = "obesecat:piglin_barter_tp_tome";
    private static final String SHIPWRECK_DOMINO_POOL = "obesecat:shipwreck_domino";

    public static void addLoot(LootTableLoadEvent event) {
        if (event.getKey().equals(BuiltInLootTables.SIMPLE_DUNGEON) && event.getTable().getPool(DUNGEON_TOILET_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(DUNGEON_TOILET_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.22F))
                    .add(LootItem.lootTableItem(ModItems.TOILET.get()))
                    .build());
        }

        if (event.getKey().equals(BuiltInLootTables.SIMPLE_DUNGEON) && event.getTable().getPool(DUNGEON_CHOCOLATE_PUDDING_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(DUNGEON_CHOCOLATE_PUDDING_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.25F))
                    .add(LootItem.lootTableItem(ModItems.CHOCOLATE_PUDDING.get()))
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

        if (event.getKey().equals(BuiltInLootTables.END_CITY_TREASURE) && event.getTable().getPool(END_CITY_SKELETON_CROWN_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(END_CITY_SKELETON_CROWN_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.24F))
                    .add(LootItem.lootTableItem(ModItems.SKELETON_CROWN.get()))
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

        if (event.getKey().equals(BuiltInLootTables.ANCIENT_CITY) && event.getTable().getPool(ANCIENT_CITY_MIGHTY_SWORD_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(ANCIENT_CITY_MIGHTY_SWORD_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.12F))
                    .add(LootItem.lootTableItem(ModItems.MIGHTY_SWORD.get()))
                    .build());
        }

        if (event.getKey().equals(BuiltInLootTables.ANCIENT_CITY) && event.getTable().getPool(ANCIENT_CITY_DARK_SWORD_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(ANCIENT_CITY_DARK_SWORD_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.12F))
                    .add(LootItem.lootTableItem(ModItems.DARK_SWORD_BASE.get()))
                    .build());
        }

        if (isBastionLoot(event) && event.getTable().getPool(BASTION_CURSED_CLEAVER_POOL) == null) {
            float chance = event.getKey().equals(BuiltInLootTables.BASTION_TREASURE) ? 0.35F : 0.16F;
            event.getTable().addPool(LootPool.lootPool()
                    .name(BASTION_CURSED_CLEAVER_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(chance))
                    .add(LootItem.lootTableItem(ModItems.CURSED_CLEAVER.get()))
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

        if (isShipwreckLoot(event) && event.getTable().getPool(SHIPWRECK_DOMINO_POOL) == null) {
            event.getTable().addPool(LootPool.lootPool()
                    .name(SHIPWRECK_DOMINO_POOL)
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.18F))
                    .add(LootItem.lootTableItem(ModItems.DOMINO.get()))
                    .build());
        }
    }

    private static boolean isBastionLoot(LootTableLoadEvent event) {
        return event.getKey().equals(BuiltInLootTables.BASTION_TREASURE)
                || event.getKey().equals(BuiltInLootTables.BASTION_OTHER)
                || event.getKey().equals(BuiltInLootTables.BASTION_BRIDGE)
                || event.getKey().equals(BuiltInLootTables.BASTION_HOGLIN_STABLE);
    }

    private static boolean isShipwreckLoot(LootTableLoadEvent event) {
        return event.getKey().equals(BuiltInLootTables.SHIPWRECK_SUPPLY)
                || event.getKey().equals(BuiltInLootTables.SHIPWRECK_TREASURE);
    }

    private ModLootTables() {
    }
}
