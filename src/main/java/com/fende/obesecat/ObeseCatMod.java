package com.fende.obesecat;

import com.fende.obesecat.registry.ModBlocks;
import com.fende.obesecat.registry.ModBlockEntities;
import com.fende.obesecat.registry.ModEntities;
import com.fende.obesecat.registry.ModItems;
import com.fende.obesecat.registry.ModRecipeTypes;
import com.fende.obesecat.registry.ModLootTables;
import com.fende.obesecat.registry.ModMenus;
import com.fende.obesecat.registry.ModNetworking;
import com.fende.obesecat.registry.ModSounds;
import com.fende.obesecat.registry.ModVillagerTrades;
import com.fende.obesecat.registry.ModVillagers;
import com.fende.obesecat.world.AequitasSummonManager;
import com.fende.obesecat.world.AtomicFireSphere;
import com.fende.obesecat.world.CowKingFortSpawner;
import com.fende.obesecat.world.CowLevelSafetyManager;
import com.fende.obesecat.world.CrushPunchManager;
import com.fende.obesecat.world.EmberSingularityMagnet;
import com.fende.obesecat.world.HolyExplosionManager;
import com.fende.obesecat.world.LightningStabManager;
import com.fende.obesecat.world.ShellbustStabManager;
import com.fende.obesecat.world.IonStormManager;
import com.fende.obesecat.world.ManhattanBunkerDebug;
import com.fende.obesecat.world.ManhattanBunkerResidentSpawner;
import com.fende.obesecat.world.ManhattanPhysicistSpawner;
import com.fende.obesecat.world.MightySwordBreakManager;
import com.fende.obesecat.world.MrKittysPawsManager;
import com.fende.obesecat.world.NuclearCatExplosion;
import com.fende.obesecat.world.DarkSwordManager;
import com.fende.obesecat.world.NightVisionMrKittyManager;
import com.fende.obesecat.world.NightSwordManager;
import com.fende.obesecat.world.PacoBarkBurst;
import com.fende.obesecat.world.ParadoxSummonManager;
import com.fende.obesecat.world.PraxisSummonManager;
import com.fende.obesecat.world.VeritasSummonManager;
import com.fende.obesecat.world.SammyCrossManager;
import com.fende.obesecat.world.SniperPacoManager;
import com.fende.obesecat.world.SplitPunchManager;
import com.fende.obesecat.world.StasisSwordManager;
import com.fende.obesecat.world.TinyPlanetProtection;
import com.fende.obesecat.world.ToiletSinkAnimation;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Monster;
import com.fende.obesecat.entity.TargetDummy;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ObeseCatMod.MOD_ID)
public class ObeseCatMod {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObeseCatMod.class);
    public static final String MOD_ID = "obesecat";

    public ObeseCatMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModRecipeTypes.RECIPE_TYPES.register(modEventBus);
        ModRecipeTypes.RECIPE_SERIALIZERS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModVillagers.POI_TYPES.register(modEventBus);
        ModVillagers.PROFESSIONS.register(modEventBus);

        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(ModNetworking::registerPayloads);

        NeoForge.EVENT_BUS.addListener(NuclearCatExplosion::onLevelTick);
        NeoForge.EVENT_BUS.addListener(AtomicFireSphere::onLevelTick);
        NeoForge.EVENT_BUS.addListener(EmberSingularityMagnet::onLevelTick);
        NeoForge.EVENT_BUS.addListener(NightVisionMrKittyManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(PacoBarkBurst::onLevelTick);
        NeoForge.EVENT_BUS.addListener(ParadoxSummonManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(VeritasSummonManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(PraxisSummonManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(AequitasSummonManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(SniperPacoManager::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(SplitPunchManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(CrushPunchManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(LightningStabManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(IonStormManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(HolyExplosionManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(ShellbustStabManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(MightySwordBreakManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(DarkSwordManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(NightSwordManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(StasisSwordManager::onLevelTick);
        NeoForge.EVENT_BUS.addListener(TinyPlanetProtection::onLevelTick);
        NeoForge.EVENT_BUS.addListener(ToiletSinkAnimation::onLevelTick);
        NeoForge.EVENT_BUS.addListener(ModVillagerTrades::addTrades);
        NeoForge.EVENT_BUS.addListener(ModLootTables::addLoot);
        NeoForge.EVENT_BUS.addListener(CowKingFortSpawner::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(CowKingFortSpawner::onLevelTick);
        NeoForge.EVENT_BUS.addListener(CowLevelSafetyManager::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(MrKittysPawsManager::onLivingFall);
        NeoForge.EVENT_BUS.addListener(ManhattanBunkerResidentSpawner::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(ManhattanPhysicistSpawner::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(ManhattanBunkerDebug::registerCommands);
        NeoForge.EVENT_BUS.addListener(ManhattanBunkerDebug::onServerStarted);
        NeoForge.EVENT_BUS.addListener(SammyCrossManager::onLivingDeath);

        LOGGER.info("ObeseCat initialized with standalone Manhattan bunker worldgen and custom Manhattan Physicist trades");
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.OBESE_CAT.get(), Cat.createAttributes().build());
        event.put(ModEntities.COW_KING.get(), Monster.createMonsterAttributes().build());
        event.put(ModEntities.TARGET_DUMMY.get(), TargetDummy.createAttributes().build());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.OBESE_CAT_SPAWN_EGG.get());
            event.accept(ModItems.COW_KING_SPAWN_EGG.get());
            event.accept(ModItems.MANHATTAN_PHYSICIST_SPAWN_EGG.get());
        }
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.PLUTONIUM_CAT_FOOD.get());
            event.accept(ModItems.LITHIUM_DEUTERIDE_CAT_FOOD.get());
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.TRINITITE.get());
            event.accept(ModItems.OVERDRIVE_ROD.get());
            event.accept(ModItems.OVERDRIVE_POWDER.get());
            event.accept(ModItems.OPPENHEIMERS_HAT.get());
            event.accept(ModItems.VIRTS_LEG.get());
            event.accept(ModItems.TP_TOME.get());
            event.accept(ModItems.HOLY_KNIGHT_TOKEN.get());
            event.accept(ModItems.WHITE_KNIGHT_SYMBOL.get());
            event.accept(ModItems.ARK_KNIGHT_JEWEL.get());
            event.accept(ModItems.SWORD_SAINT_INSIGNIA.get());
            event.accept(ModItems.COW_LEVEL_PORTAL.get());
        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.PACO.get());
            event.accept(ModItems.DOMINO.get());
            event.accept(ModItems.DAWN_DOMINO.get());
            event.accept(ModItems.MIDDAY_DOMINO.get());
            event.accept(ModItems.DUSK_DOMINO.get());
            event.accept(ModItems.MIDNIGHT_DOMINO.get());
            event.accept(ModItems.CALM_DOMINO.get());
            event.accept(ModItems.DOWNPOUR_DOMINO.get());
            event.accept(ModItems.THUNDERSTORM_DOMINO.get());
            event.accept(ModItems.EMBER.get());
            event.accept(ModItems.MR_KITTY.get());
            event.accept(ModItems.NIGHT_VISION_MR_KITTY.get());
            event.accept(ModItems.MR_KITTYS_PAWS.get());
            event.accept(ModItems.EMBER_SINGULARITY.get());
            event.accept(ModItems.WORMHOLE_EMBER.get());
            event.accept(ModItems.ENIGMA_EMBER.get());
            event.accept(ModItems.TINY_PLANET.get());
            event.accept(ModItems.CONCUSSIVE_PACO.get());
            event.accept(ModItems.ATTACK_PACO.get());
            event.accept(ModItems.ASSAULT_PACO.get());
            event.accept(ModItems.SNIPER_PACO.get());
            event.accept(ModItems.ATOMIC_PACO.get());
            event.accept(ModItems.J_ROBERT_PACOHEIMER.get());
            event.accept(ModItems.HELLHOUND_PACO.get());
            event.accept(ModItems.OPPENHEIMERS_HAT.get());
            event.accept(ModItems.HOLY_SWORD.get());
            event.accept(ModItems.MIGHTY_SWORD.get());
            event.accept(ModItems.DARK_SWORD.get());
            event.accept(ModItems.STASIS_SWORD.get());
            event.accept(ModItems.SPLIT_PUNCH.get());
            event.accept(ModItems.CRUSH_PUNCH.get());
            event.accept(ModItems.LIGHTNING_STAB.get());
            event.accept(ModItems.HOLY_EXPLOSION.get());
            event.accept(ModItems.SHELLBUST_STAB.get());
            event.accept(ModItems.BLASTAR_PUNCH.get());
            event.accept(ModItems.HELLCRY_PUNCH.get());
            event.accept(ModItems.ICEWOLF_BITE.get());
            event.accept(ModItems.NIGHT_SWORD.get());
            event.accept(ModItems.DARK_KNIGHT_BRACER.get());
            event.accept(ModItems.BOOM_STICK.get());
            event.accept(ModItems.FIRE_STICK.get());
            event.accept(ModItems.FIRE_BOOM_STICK.get());
            event.accept(ModItems.BIG_FIRE_BOOM_STICK.get());
            event.accept(ModItems.ION_STORM_STICK.get());
            event.accept(ModItems.SAMMYS_CROSS.get());
            event.accept(ModItems.PARADOX.get());
            event.accept(ModItems.VERITAS.get());
            event.accept(ModItems.PRAXIS.get());
            event.accept(ModItems.AEQUITAS.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.TOILET.get());
            event.accept(ModItems.TRINITITE.get());
            event.accept(ModItems.ECHOING_BLAST_CHAMBER.get());
            event.accept(ModItems.NUCLEAR_LIBRARY.get());
            event.accept(ModItems.EMBER_SINGULARITY.get());
            event.accept(ModItems.WORMHOLE_EMBER.get());
            event.accept(ModItems.ENIGMA_EMBER.get());
            event.accept(ModItems.TINY_PLANET.get());
            event.accept(ModItems.TRANSMUTATION_CUBE.get());
            event.accept(ModItems.TARGET_DUMMY.get());
            event.accept(ModItems.NIGHT_VISION_MR_KITTY.get());
            event.accept(ModItems.MR_KITTYS_PAWS.get());
            event.accept(ModItems.BOOM_STICK.get());
            event.accept(ModItems.FIRE_STICK.get());
            event.accept(ModItems.FIRE_BOOM_STICK.get());
            event.accept(ModItems.BIG_FIRE_BOOM_STICK.get());
            event.accept(ModItems.EMERGENCY_STARTER_HOME.get());
            event.accept(ModItems.LUXURY_STARTER_HOME.get());
        }
    }
}
