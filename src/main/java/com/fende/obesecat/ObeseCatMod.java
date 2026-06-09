package com.fende.obesecat;

import com.fende.obesecat.registry.ModEntities;
import com.fende.obesecat.registry.ModItems;
import com.fende.obesecat.registry.ModSounds;
import com.fende.obesecat.registry.ModVillagerTrades;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ObeseCatMod.MOD_ID)
public class ObeseCatMod {
    public static final String MOD_ID = "obesecat";

    public ObeseCatMod(IEventBus modEventBus) {
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);

        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::addCreative);

        NeoForge.EVENT_BUS.addListener(ModVillagerTrades::addTrades);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.OBESE_CAT.get(), Cat.createAttributes().build());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.OBESE_CAT_SPAWN_EGG.get());
        }
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.PLUTONIUM_CAT_FOOD.get());
            event.accept(ModItems.LITHIUM_DEUTERIDE_CAT_FOOD.get());
        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.PACO.get());
        }
    }
}
