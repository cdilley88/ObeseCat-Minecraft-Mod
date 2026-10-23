package com.fende.obesecat.counterstrike;

import com.fende.obesecat.counterstrike.registry.CSBlockEntities;
import com.fende.obesecat.counterstrike.registry.CSBlocks;
import com.fende.obesecat.counterstrike.registry.CSCreativeTabs;
import com.fende.obesecat.counterstrike.registry.CSEntities;
import com.fende.obesecat.counterstrike.registry.CSItems;
import com.fende.obesecat.counterstrike.registry.CSMenus;
import com.fende.obesecat.counterstrike.registry.CSNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ObeseCatCounterstrike.MOD_ID)
public class ObeseCatCounterstrike {
    public static final String MOD_ID = "obesecat_counterstrike";
    private static final Logger LOGGER = LoggerFactory.getLogger(ObeseCatCounterstrike.class);

    public ObeseCatCounterstrike(IEventBus modEventBus) {
        LOGGER.info("[ObeseCat: Counterstrike] Battlefield Control Online. Initializing feline defense networks.");

        CSBlocks.BLOCKS.register(modEventBus);
        CSBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        CSItems.ITEMS.register(modEventBus);
        CSEntities.ENTITY_TYPES.register(modEventBus);
        CSMenus.MENUS.register(modEventBus);
        CSCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(CSNetworking::registerPayloads);
    }
}
