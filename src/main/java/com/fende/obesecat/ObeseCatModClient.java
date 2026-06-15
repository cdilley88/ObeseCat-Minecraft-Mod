package com.fende.obesecat;

import com.fende.obesecat.client.FissionFirestormOverlay;
import com.fende.obesecat.client.NuclearFlashOverlay;
import com.fende.obesecat.client.NightVisionOverlay;
import com.fende.obesecat.client.ObeseCatRenderer;
import com.fende.obesecat.client.ObeseCatTimerOverlay;
import com.fende.obesecat.client.model.FatManModel;
import com.fende.obesecat.entity.ObeseCat;
import com.fende.obesecat.registry.ModBlocks;
import com.fende.obesecat.registry.ModEntities;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@Mod(value = ObeseCatMod.MOD_ID, dist = Dist.CLIENT)
public class ObeseCatModClient {
    public ObeseCatModClient(IEventBus modEventBus) {
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerLayerDefinitions);
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerGuiLayers);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(ModBlocks.ATOMIC_FIRE.get(), RenderType.cutout()));
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
    }

    private void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ObeseCatTimerOverlay.ID, ObeseCatTimerOverlay::render);
        event.registerAboveAll(NightVisionOverlay.ID, NightVisionOverlay::render);
        event.registerAboveAll(FissionFirestormOverlay.ID, FissionFirestormOverlay::render);
        event.registerAboveAll(NuclearFlashOverlay.ID, NuclearFlashOverlay::render);
    }
}
