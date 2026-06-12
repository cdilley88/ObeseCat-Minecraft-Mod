package com.fende.obesecat;

import com.fende.obesecat.registry.ModBlocks;
import com.fende.obesecat.registry.ModEntities;
import com.fende.obesecat.registry.ModItems;
import com.fende.obesecat.registry.ModNetworking;
import com.fende.obesecat.registry.ModSounds;
import com.fende.obesecat.registry.ModVillagerTrades;
import com.fende.obesecat.world.AtomicFireSphere;
import com.fende.obesecat.world.NuclearCatExplosion;
import com.fende.obesecat.world.PacoBarkBurst;
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
        ModBlocks.BLOCKS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);

        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(ModNetworking::registerPayloads);

        NeoForge.EVENT_BUS.addListener(NuclearCatExplosion::onLevelTick);
        NeoForge.EVENT_BUS.addListener(AtomicFireSphere::onLevelTick);
        NeoForge.EVENT_BUS.addListener(PacoBarkBurst::onLevelTick);
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
            event.accept(ModItems.CONCUSSIVE_PACO.get());
            event.accept(ModItems.ATTACK_PACO.get());
            event.accept(ModItems.ASSAULT_PACO.get());
            event.accept(ModItems.J_ROBERT_PACOHEIMER.get());
            event.accept(ModItems.HELLHOUND_PACO.get());
            event.accept(ModItems.BOOM_STICK.get());
            event.accept(ModItems.FIRE_STICK.get());
            event.accept(ModItems.FIRE_BOOM_STICK.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.BOOM_STICK.get());
            event.accept(ModItems.FIRE_STICK.get());
            event.accept(ModItems.FIRE_BOOM_STICK.get());
        }
    }
}
