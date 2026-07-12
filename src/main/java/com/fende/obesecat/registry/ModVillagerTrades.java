package com.fende.obesecat.registry;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
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
    private static final int MAX_TRADES = 12;
    private static final int XP = 1;
    private static final float PRICE_MULTIPLIER = 0.05F;

    public static void addTrades(VillagerTradesEvent event) {
        if (event.getType() == ModVillagers.MANHATTAN_PHYSICIST.get()) {
            LOGGER.info("Registering Manhattan Physicist-specific trades");
            event.getTrades().get(NOVICE).add(hellhoundPacoTrade());
            event.getTrades().get(NOVICE).add(oppenheimersHatTrade());
            event.getTrades().get(NOVICE).add(deuteriumCatFoodTrade());
            return;
        }

        if (event.getType() != VillagerProfession.FARMER) {
            return;
        }

        event.getTrades().get(NOVICE).add(trade(new ItemStack(ModItems.OBESE_CAT_SPAWN_EGG.get())));
        event.getTrades().get(NOVICE).add(trade(new ItemStack(ModItems.PLUTONIUM_CAT_FOOD.get())));
        event.getTrades().get(NOVICE).add(trade(new ItemStack(ModItems.PACO.get())));
        event.getTrades().get(NOVICE).add(trade(new ItemStack(ModItems.EMBER.get())));
        event.getTrades().get(NOVICE).add(trade(new ItemStack(ModItems.MR_KITTY.get())));
        event.getTrades().get(NOVICE).add(trade(new ItemStack(ModItems.EMERGENCY_STARTER_HOME.get())));
    }

    private static BasicItemListing trade(ItemStack itemForSale) {
        return new BasicItemListing(EMERALD_COST, itemForSale, MAX_TRADES, XP, PRICE_MULTIPLIER);
    }

    private static BasicItemListing trinititeTrade(ItemStack itemForSale) {
        return new BasicItemListing(new ItemStack(ModItems.TRINITITE.get()), itemForSale, MAX_TRADES, XP, PRICE_MULTIPLIER);
    }

    private static BasicItemListing trinititeTrade(ItemStack itemForSale, int trinititeCost) {
        return new BasicItemListing(new ItemStack(ModItems.TRINITITE.get(), trinititeCost), itemForSale, MAX_TRADES, XP, PRICE_MULTIPLIER);
    }

    public static MerchantOffers createManhattanPhysicistOffers(net.minecraft.world.entity.Entity trader) {
        MerchantOffers offers = new MerchantOffers();
        addOffer(offers, hellhoundPacoTrade().getOffer(trader, trader.getRandom()));
        addOffer(offers, oppenheimersHatTrade().getOffer(trader, trader.getRandom()));
        addOffer(offers, deuteriumCatFoodTrade().getOffer(trader, trader.getRandom()));
        return offers;
    }

    private static BasicItemListing hellhoundPacoTrade() {
        return trinititeTrade(new ItemStack(ModItems.HELLHOUND_PACO.get()));
    }

    private static BasicItemListing oppenheimersHatTrade() {
        return trinititeTrade(new ItemStack(ModItems.OPPENHEIMERS_HAT.get()), 5);
    }

    private static BasicItemListing deuteriumCatFoodTrade() {
        return trinititeTrade(new ItemStack(ModItems.LITHIUM_DEUTERIDE_CAT_FOOD.get(), 10));
    }

    private static void addOffer(MerchantOffers offers, MerchantOffer offer) {
        if (offer != null) {
            offers.add(offer);
        }
    }

    private ModVillagerTrades() {
    }
}
