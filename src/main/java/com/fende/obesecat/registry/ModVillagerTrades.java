package com.fende.obesecat.registry;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

public final class ModVillagerTrades {
    private static final int NOVICE = 1;
    private static final int EMERALD_COST = 1;
    private static final int MAX_TRADES = 12;
    private static final int XP = 1;
    private static final float PRICE_MULTIPLIER = 0.05F;

    public static void addTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.FARMER) {
            return;
        }

        event.getTrades().get(NOVICE).add(trade(new ItemStack(ModItems.OBESE_CAT_SPAWN_EGG.get())));
        event.getTrades().get(NOVICE).add(trade(new ItemStack(ModItems.PLUTONIUM_CAT_FOOD.get())));
        event.getTrades().get(NOVICE).add(trade(new ItemStack(ModItems.PACO.get())));
    }

    private static BasicItemListing trade(ItemStack itemForSale) {
        return new BasicItemListing(EMERALD_COST, itemForSale, MAX_TRADES, XP, PRICE_MULTIPLIER);
    }

    private ModVillagerTrades() {
    }
}
