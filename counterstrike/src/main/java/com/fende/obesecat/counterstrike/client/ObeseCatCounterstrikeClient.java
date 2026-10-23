package com.fende.obesecat.counterstrike.client;

import com.fende.obesecat.counterstrike.ObeseCatCounterstrike;
import com.fende.obesecat.counterstrike.registry.CSEntities;
import com.fende.obesecat.counterstrike.registry.CSMenus;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = ObeseCatCounterstrike.MOD_ID, dist = Dist.CLIENT)
public class ObeseCatCounterstrikeClient {

    public ObeseCatCounterstrikeClient(IEventBus modEventBus) {
        modEventBus.addListener(this::registerMenuScreens);
        modEventBus.addListener(this::registerEntityRenderers);
    }

    private void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(CSMenus.CONSTRUCTION_YARD.get(), ConstructionYardScreen::new);
        event.register(CSMenus.FISSION_TURBINE.get(), FissionTurbineScreen::new);
        event.register(CSMenus.BARRACKS.get(), BarracksScreen::new);
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CSEntities.ROCKET.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(CSEntities.FRAG_GRENADE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(CSEntities.C4_CHARGE.get(), ThrownItemRenderer::new);
    }
}
