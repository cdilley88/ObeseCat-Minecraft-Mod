package com.fende.obesecat.registry;

import com.fende.obesecat.client.FissionFirestormOverlay;
import com.fende.obesecat.client.NuclearFlashOverlay;
import com.fende.obesecat.client.NightVisionOverlay;
import com.fende.obesecat.network.FissionFirestormPayload;
import com.fende.obesecat.network.NightVisionOverlayPayload;
import com.fende.obesecat.network.SniperPacoFirePayload;
import com.fende.obesecat.network.NuclearFlashPayload;
import com.fende.obesecat.world.SniperPacoManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(NuclearFlashPayload.TYPE, NuclearFlashPayload.STREAM_CODEC, ModNetworking::handleNuclearFlash)
                .playToClient(NightVisionOverlayPayload.TYPE, NightVisionOverlayPayload.STREAM_CODEC, ModNetworking::handleNightVisionOverlay)
                .playToClient(FissionFirestormPayload.TYPE, FissionFirestormPayload.STREAM_CODEC, ModNetworking::handleFissionFirestorm)
                .playToServer(SniperPacoFirePayload.TYPE, SniperPacoFirePayload.STREAM_CODEC, ModNetworking::handleSniperPacoFire);
    }

    private static void handleNuclearFlash(NuclearFlashPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                NuclearFlashOverlay.trigger(payload.intensity());
            }
        });
    }

    private static void handleNightVisionOverlay(NightVisionOverlayPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                NightVisionOverlay.setActive(payload.active());
            }
        });
    }

    private static void handleFissionFirestorm(FissionFirestormPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                FissionFirestormOverlay.trigger(payload.holdTicks());
            }
        });
    }

    private static void handleSniperPacoFire(SniperPacoFirePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                SniperPacoManager.queueShot(serverPlayer);
            }
        });
    }
}
