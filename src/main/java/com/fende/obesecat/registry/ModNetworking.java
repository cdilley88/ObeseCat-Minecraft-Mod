package com.fende.obesecat.registry;

import com.fende.obesecat.client.FissionFirestormOverlay;
import com.fende.obesecat.client.NuclearFlashOverlay;
import com.fende.obesecat.client.BigGeminiExplosionOverlay;
import com.fende.obesecat.client.GeminiExplosionOverlay;
import com.fende.obesecat.network.BigGeminiNuclearPayload;
import com.fende.obesecat.network.GeminiNuclearPayload;
import com.fende.obesecat.client.NightVisionOverlay;
import com.fende.obesecat.client.SammyCrossActivationOverlay;
import com.fende.obesecat.network.FissionFirestormPayload;
import com.fende.obesecat.network.IonStormPayload;
import com.fende.obesecat.client.IonStormOverlay;
import com.fende.obesecat.network.NightVisionOverlayPayload;
import com.fende.obesecat.network.TelemetryOverlayPayload;
import com.fende.obesecat.network.SammyCrossActivationPayload;
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
                .playToClient(GeminiNuclearPayload.TYPE, GeminiNuclearPayload.STREAM_CODEC, ModNetworking::handleGeminiNuclear)
                .playToClient(BigGeminiNuclearPayload.TYPE, BigGeminiNuclearPayload.STREAM_CODEC, ModNetworking::handleBigGeminiNuclear)
                .playToClient(com.fende.obesecat.network.TacticalNuclearPayload.TYPE, com.fende.obesecat.network.TacticalNuclearPayload.STREAM_CODEC, ModNetworking::handleTacticalNuclear)
                .playToClient(NightVisionOverlayPayload.TYPE, NightVisionOverlayPayload.STREAM_CODEC, ModNetworking::handleNightVisionOverlay)
                .playToClient(TelemetryOverlayPayload.TYPE, TelemetryOverlayPayload.STREAM_CODEC, ModNetworking::handleTelemetryOverlay)
                .playToClient(com.fende.obesecat.network.ScannerOverlayPayload.TYPE, com.fende.obesecat.network.ScannerOverlayPayload.STREAM_CODEC, ModNetworking::handleScannerOverlay)
                .playToClient(FissionFirestormPayload.TYPE, FissionFirestormPayload.STREAM_CODEC, ModNetworking::handleFissionFirestorm)
                .playToClient(IonStormPayload.TYPE, IonStormPayload.STREAM_CODEC, ModNetworking::handleIonStorm)
                .playToClient(SammyCrossActivationPayload.TYPE, SammyCrossActivationPayload.STREAM_CODEC, ModNetworking::handleSammyCrossActivation)
                .playToServer(SniperPacoFirePayload.TYPE, SniperPacoFirePayload.STREAM_CODEC, ModNetworking::handleSniperPacoFire);
    }

    private static void handleGeminiNuclear(GeminiNuclearPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                GeminiExplosionOverlay.trigger(payload.origin(), payload.durationTicks(), payload.intensity());
                com.fende.obesecat.client.GeminiFireballRenderer.spawn(payload.origin(), payload.durationTicks(), 78.0F);
            }
        });
    }

    private static void handleBigGeminiNuclear(BigGeminiNuclearPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                BigGeminiExplosionOverlay.trigger(payload.origin(), payload.durationTicks(), payload.intensity());
                com.fende.obesecat.client.BigGeminiFireballRenderer.spawn(payload.origin(), payload.durationTicks(), 148.0F);
            }
        });
    }

    private static void handleTacticalNuclear(com.fende.obesecat.network.TacticalNuclearPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                com.fende.obesecat.client.TacticalExplosionOverlay.trigger(payload.origin(), payload.durationTicks(), payload.intensity());
                com.fende.obesecat.client.TacticalFireballRenderer.spawn(payload.origin(), payload.durationTicks(), 14.0F);
            }
        });
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

    private static void handleTelemetryOverlay(TelemetryOverlayPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                com.fende.obesecat.client.TelemetryOverlay.setActive(payload.active());
            }
        });
    }

    private static void handleScannerOverlay(com.fende.obesecat.network.ScannerOverlayPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                com.fende.obesecat.client.ScannerOverlay.setActive(payload.active());
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

    private static void handleIonStorm(IonStormPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> { if (FMLEnvironment.dist.isClient()) IonStormOverlay.setActive(payload.active(), payload.remainingTicks()); });
    }

    private static void handleSammyCrossActivation(SammyCrossActivationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                SammyCrossActivationOverlay.trigger();
            }
        });
    }

    private static void handleSniperPacoFire(SniperPacoFirePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                SniperPacoManager.queueShot(serverPlayer);
            }
        });
    }}


