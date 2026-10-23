package com.fende.obesecat.counterstrike.resource;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public final class ResourceValues {
    public static final int MAX_TANK_CAPACITY = 10000;

    public enum ResourceType {
        IRON("Iron", "Fe", 0xFFD0D0D0, MAX_TANK_CAPACITY),
        GOLD("Gold", "Au", 0xFFFFD700, MAX_TANK_CAPACITY),
        DIAMOND("Diamond", "Dm", 0xFF55FFFF, MAX_TANK_CAPACITY),
        TRINITITE("Trinitite", "Tr", 0xFF55FF55, MAX_TANK_CAPACITY),
        TABBYRIUM("Tabbyrium", "Tb", 0xFFFF9900, MAX_TANK_CAPACITY);

        private final String displayName;
        private final String symbol;
        private final int color;
        private final int maxCapacity;

        ResourceType(String displayName, String symbol, int color, int maxCapacity) {
            this.displayName = displayName;
            this.symbol = symbol;
            this.color = color;
            this.maxCapacity = maxCapacity;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getSymbol() {
            return symbol;
        }

        public int getColor() {
            return color;
        }

        public int getMaxCapacity() {
            return maxCapacity;
        }
    }

    private ResourceValues() {
    }

    public static @Nullable ResourceType getResourceType(ItemStack stack) {
        if (stack.isEmpty()) return null;

        if (stack.is(Items.IRON_NUGGET) || stack.is(Items.IRON_INGOT) || stack.is(Items.RAW_IRON)
                || stack.is(Items.IRON_BLOCK) || stack.is(Items.RAW_IRON_BLOCK)) {
            return ResourceType.IRON;
        }

        if (stack.is(Items.GOLD_NUGGET) || stack.is(Items.GOLD_INGOT) || stack.is(Items.RAW_GOLD)
                || stack.is(Items.GOLD_BLOCK) || stack.is(Items.RAW_GOLD_BLOCK)) {
            return ResourceType.GOLD;
        }

        if (stack.is(Items.DIAMOND) || stack.is(Items.DIAMOND_BLOCK)) {
            return ResourceType.DIAMOND;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if ("obesecat".equals(id.getNamespace())) {
            String path = id.getPath();
            if ("trinitite".equals(path)) {
                return ResourceType.TRINITITE;
            }
            if (path.contains("tabbyrium")) {
                return ResourceType.TABBYRIUM;
            }
        }

        return null;
    }

    public static int getUnitValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        // Iron
        if (stack.is(Items.IRON_NUGGET)) return 1;
        if (stack.is(Items.IRON_INGOT) || stack.is(Items.RAW_IRON)) return 9;
        if (stack.is(Items.IRON_BLOCK) || stack.is(Items.RAW_IRON_BLOCK)) return 81;

        // Gold
        if (stack.is(Items.GOLD_NUGGET)) return 1;
        if (stack.is(Items.GOLD_INGOT) || stack.is(Items.RAW_GOLD)) return 9;
        if (stack.is(Items.GOLD_BLOCK) || stack.is(Items.RAW_GOLD_BLOCK)) return 81;

        // Diamond
        if (stack.is(Items.DIAMOND)) return 9;
        if (stack.is(Items.DIAMOND_BLOCK)) return 81;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if ("obesecat".equals(id.getNamespace())) {
            String path = id.getPath();
            if ("trinitite".equals(path)) {
                return 81;
            }
            if ("tabbyrium_shard".equals(path)) {
                return 1;
            }
            if ("tabbyrium_crystal".equals(path)) {
                return 4;
            }
            if ("tabbyrium_cluster".equals(path)) {
                return 16;
            }
            if ("tabbyrium_crust".equals(path) || "tabbyrium_stalk".equals(path) || "tabbyrium_planks".equals(path)) {
                return 9;
            }
            if ("tabbyrium_silo".equals(path)) {
                return 81;
            }
        }

        return 0;
    }

    public static boolean matchesType(ItemStack stack, ResourceType type) {
        return getResourceType(stack) == type;
    }
}
