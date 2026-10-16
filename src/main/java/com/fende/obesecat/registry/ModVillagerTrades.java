package com.fende.obesecat.registry;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModVillagerTrades {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModVillagerTrades.class);
    private static final int NOVICE = 1;
    private static final int EMERALD_COST = 1;
    private static final int DEFAULT_MAX_TRADES = 12;
    private static final int PERMANENT_MAX_TRADES = 64;
    private static final int FARMER_XP = 1;
    private static final int PHYSICIST_XP = 0; // Option A (Fixed Specialist): Does not level up
    private static final float PRICE_MULTIPLIER = 0.05F;

    public static void addTrades(VillagerTradesEvent event) {
        if (event.getType() == ModVillagers.MANHATTAN_PHYSICIST.get()) {
            LOGGER.info("Registering Manhattan Physicist trades from unified trade list");
            for (BasicItemListing listing : getManhattanPhysicistTradeList()) {
                event.getTrades().get(NOVICE).add(listing);
            }
            return;
        }

        // Base Domino, Base Paco, Base Ember, & Base Dorito: 1 Emerald trade available across all vanilla professions
        event.getTrades().get(NOVICE).add(farmerTrade(new ItemStack(ModItems.DOMINO.get())));
        event.getTrades().get(NOVICE).add(farmerTrade(new ItemStack(ModItems.PACO.get())));
        event.getTrades().get(NOVICE).add(farmerTrade(new ItemStack(ModItems.EMBER.get())));
        event.getTrades().get(NOVICE).add(farmerTrade(new ItemStack(ModItems.DORITO.get())));

        if (event.getType() != VillagerProfession.FARMER) {
            return;
        }

        event.getTrades().get(NOVICE).add(farmerTrade(new ItemStack(ModItems.OBESE_CAT_SPAWN_EGG.get())));
        event.getTrades().get(NOVICE).add(farmerTrade(new ItemStack(ModItems.PLUTONIUM_CAT_FOOD.get())));
        event.getTrades().get(NOVICE).add(farmerTrade(new ItemStack(ModItems.EMERGENCY_STARTER_HOME.get())));
    }

    // =========================================================================
    // MANHATTAN PHYSICIST UNIFIED TRADE LIST (Option A - Fixed Specialist)
    // Add new trades here to easily expand the Physicist's inventory!
    // =========================================================================
    public static List<BasicItemListing> getManhattanPhysicistTradeList() {
        List<BasicItemListing> trades = new ArrayList<>();

        // Sole Manhattan Physicist Trade: 5 Emeralds -> 1 Plutonium Cat Food
        trades.add(emeraldTrade(new ItemStack(ModItems.PLUTONIUM_CAT_FOOD.get()), 5));

        return trades;
    }

    public static MerchantOffers createManhattanPhysicistOffers(Entity trader) {
        MerchantOffers offers = new MerchantOffers();
        for (BasicItemListing trade : getManhattanPhysicistTradeList()) {
            MerchantOffer offer = trade.getOffer(trader, trader.getRandom());
            if (offer != null) {
                offers.add(offer);
            }
        }
        return offers;
    }

    private static BasicItemListing farmerTrade(ItemStack itemForSale) {
        return new BasicItemListing(EMERALD_COST, itemForSale, DEFAULT_MAX_TRADES, FARMER_XP, PRICE_MULTIPLIER);
    }

    public static BasicItemListing trinititeTrade(ItemStack itemForSale, int trinititeCost) {
        return new BasicItemListing(new ItemStack(ModItems.TRINITITE.get(), trinititeCost), itemForSale, PERMANENT_MAX_TRADES, PHYSICIST_XP, 0.0F);
    }

    public static BasicItemListing emeraldTrade(ItemStack itemForSale, int emeraldCost) {
        return new BasicItemListing(new ItemStack(Items.EMERALD, emeraldCost), itemForSale, PERMANENT_MAX_TRADES, PHYSICIST_XP, 0.0F);
    }

    public static BasicItemListing customTrade(ItemStack price, ItemStack itemForSale, int maxTrades) {
        return new BasicItemListing(price, itemForSale, maxTrades, PHYSICIST_XP, PRICE_MULTIPLIER);
    }

    private ModVillagerTrades() {
    }
}

