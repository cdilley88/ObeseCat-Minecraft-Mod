package com.fende.obesecat;

import com.fende.obesecat.client.FissionFirestormOverlay;
import com.fende.obesecat.client.CowKingRenderer;
import com.fende.obesecat.client.CanOpenerScreen;
import com.fende.obesecat.client.CatChargerScreen;
import com.fende.obesecat.client.NuclearFlashOverlay;
import com.fende.obesecat.client.IonStormOverlay;
import com.fende.obesecat.client.NightVisionOverlay;
import com.fende.obesecat.client.ObeseCatRenderer;
import com.fende.obesecat.client.ObeseCatTimerOverlay;
import com.fende.obesecat.client.SniperPacoInputHandler;
import com.fende.obesecat.client.TransmutationCubeScreen;
import com.fende.obesecat.client.TargetDummyRenderer;
import com.fende.obesecat.client.TargetDummyScreen;
import com.fende.obesecat.client.EchoingBlastChamberScreen;
import com.fende.obesecat.client.model.FatManModel;
import com.fende.obesecat.entity.ObeseCat;
import com.fende.obesecat.registry.ModBlocks;
import com.fende.obesecat.registry.ModEntities;
import com.fende.obesecat.registry.ModMenus;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = ObeseCatMod.MOD_ID, dist = Dist.CLIENT)
public class ObeseCatModClient {
    public ObeseCatModClient(IEventBus modEventBus) {
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerLayerDefinitions);
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerGuiLayers);
        modEventBus.addListener(this::registerMenuScreens);
        NeoForge.EVENT_BUS.addListener(SniperPacoInputHandler::onMouseButton);
        NeoForge.EVENT_BUS.addListener(IonStormOverlay::suppressSkyFlash);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ATOMIC_FIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRINITITE.get(), RenderType.translucent());
        });
    }

    private void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FatManModel.LAYER_LOCATION, FatManModel::createBodyLayer);
    }

    @SuppressWarnings("unchecked")
    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModEntities.OBESE_CAT.get(),
                context -> (EntityRenderer<ObeseCat>) (EntityRenderer<?>) new ObeseCatRenderer(context)
        );
        event.registerEntityRenderer(ModEntities.COW_KING.get(), context -> new CowKingRenderer(context));
        event.registerEntityRenderer(ModEntities.TARGET_DUMMY.get(), TargetDummyRenderer::new);
    }

    private void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ObeseCatTimerOverlay.ID, ObeseCatTimerOverlay::render);
        event.registerAboveAll(NightVisionOverlay.ID, NightVisionOverlay::render);
        event.registerAboveAll(FissionFirestormOverlay.ID, FissionFirestormOverlay::render);
        event.registerAboveAll(IonStormOverlay.ID, IonStormOverlay::render);
        event.registerAboveAll(NuclearFlashOverlay.ID, NuclearFlashOverlay::render);
    }

    private void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.TRANSMUTATION_CUBE.get(), TransmutationCubeScreen::new);
        event.register(ModMenus.ECHOING_BLAST_CHAMBER.get(), EchoingBlastChamberScreen::new);
        event.register(ModMenus.CAN_OPENER.get(), CanOpenerScreen::new);
        event.register(ModMenus.CAT_CHARGER.get(), CatChargerScreen::new);
        event.register(ModMenus.TARGET_DUMMY.get(), TargetDummyScreen::new);
    }
}
