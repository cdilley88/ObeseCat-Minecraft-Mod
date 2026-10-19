package com.fende.obesecat;

import com.fende.obesecat.client.FissionFirestormOverlay;
import com.fende.obesecat.client.CowKingRenderer;
import com.fende.obesecat.client.TheCarverRenderer;
import com.fende.obesecat.client.SkeletonKingRenderer;
import com.fende.obesecat.client.model.TheCarverModel;
import com.fende.obesecat.client.TabbyriumRefineryScreen;
import com.fende.obesecat.client.CatChargerScreen;
import com.fende.obesecat.client.GeminiExplosionOverlay;
import com.fende.obesecat.client.BigGeminiExplosionOverlay;
import com.fende.obesecat.client.TacticalExplosionOverlay;
import com.fende.obesecat.client.GeminiFireballRenderer;
import com.fende.obesecat.client.BigGeminiFireballRenderer;
import com.fende.obesecat.client.TacticalFireballRenderer;
import com.fende.obesecat.client.NuclearFlashOverlay;
import com.fende.obesecat.client.IonStormOverlay;
import com.fende.obesecat.client.NightVisionOverlay;
import com.fende.obesecat.client.ScannerOverlay;
import com.fende.obesecat.client.ScannerMobTagRenderer;
import com.fende.obesecat.client.TelemetryOverlay;
import com.fende.obesecat.client.ObeseCatRenderer;
import com.fende.obesecat.client.ObeseCatTimerOverlay;
import com.fende.obesecat.client.SniperPacoInputHandler;
import com.fende.obesecat.client.TransmutationCubeScreen;
import com.fende.obesecat.client.EchoingBlastChamberScreen;
import com.fende.obesecat.client.CowKingCrownLayer;
import com.fende.obesecat.client.SkeletonKingCrownLayer;
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
        NeoForge.EVENT_BUS.addListener(GeminiExplosionOverlay::onClientTick);
        NeoForge.EVENT_BUS.addListener(BigGeminiExplosionOverlay::onClientTick);
        NeoForge.EVENT_BUS.addListener(TacticalExplosionOverlay::onClientTick);
        NeoForge.EVENT_BUS.addListener(GeminiFireballRenderer::onRenderLevelStage);
        NeoForge.EVENT_BUS.addListener(BigGeminiFireballRenderer::onRenderLevelStage);
        NeoForge.EVENT_BUS.addListener(TacticalFireballRenderer::onRenderLevelStage);
        NeoForge.EVENT_BUS.register(ScannerMobTagRenderer.class);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ATOMIC_FIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRINITITE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TABBYRIUM_BLOSSOM_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TABBYRIUM_CLUSTER.get(), RenderType.cutout());
        });
    }

    private void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FatManModel.LAYER_LOCATION, FatManModel::createBodyLayer);
        event.registerLayerDefinition(CowKingCrownLayer.LAYER_LOCATION, CowKingCrownLayer::createCrownLayer);
        event.registerLayerDefinition(SkeletonKingCrownLayer.LAYER_LOCATION, SkeletonKingCrownLayer::createCrownLayer);
        event.registerLayerDefinition(TheCarverModel.LAYER_LOCATION, TheCarverModel::createBodyLayer);
    }

    @SuppressWarnings("unchecked")
    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModEntities.OBESE_CAT.get(),
                context -> (EntityRenderer<ObeseCat>) (EntityRenderer<?>) new ObeseCatRenderer(context)
        );
        event.registerEntityRenderer(ModEntities.COW_KING.get(), context -> new CowKingRenderer(context));
        event.registerEntityRenderer(ModEntities.THE_CARVER.get(), context -> new TheCarverRenderer(context));
        event.registerEntityRenderer(ModEntities.SKELETON_KING.get(), context -> new SkeletonKingRenderer(context));
    }

    private void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ObeseCatTimerOverlay.ID, ObeseCatTimerOverlay::render);
        event.registerAboveAll(NightVisionOverlay.ID, NightVisionOverlay::render);
        event.registerAboveAll(TelemetryOverlay.ID, TelemetryOverlay::render);
        event.registerAboveAll(ScannerOverlay.ID, ScannerOverlay::render);
        event.registerAboveAll(FissionFirestormOverlay.ID, FissionFirestormOverlay::render);
        event.registerAboveAll(IonStormOverlay.ID, IonStormOverlay::render);
        event.registerAboveAll(NuclearFlashOverlay.ID, NuclearFlashOverlay::render);
        event.registerAboveAll(GeminiExplosionOverlay.ID, GeminiExplosionOverlay::render);
        event.registerAboveAll(BigGeminiExplosionOverlay.ID, BigGeminiExplosionOverlay::render);
        event.registerAboveAll(TacticalExplosionOverlay.ID, TacticalExplosionOverlay::render);
    }

    private void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.TRANSMUTATION_CUBE.get(), TransmutationCubeScreen::new);
        event.register(ModMenus.ECHOING_BLAST_CHAMBER.get(), EchoingBlastChamberScreen::new);
        event.register(ModMenus.TABBYRIUM_REFINERY.get(), TabbyriumRefineryScreen::new);
        event.register(ModMenus.CAT_CHARGER.get(), CatChargerScreen::new);
    }
}
