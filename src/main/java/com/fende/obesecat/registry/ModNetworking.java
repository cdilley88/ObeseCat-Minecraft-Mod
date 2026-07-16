package com.fende.obesecat.registry;

import com.fende.obesecat.client.FissionFirestormOverlay;
import com.fende.obesecat.client.NuclearFlashOverlay;
import com.fende.obesecat.client.NightVisionOverlay;
import com.fende.obesecat.client.SammyCrossActivationOverlay;
import com.fende.obesecat.network.FissionFirestormPayload;
import com.fende.obesecat.network.IonStormPayload;
import com.fende.obesecat.client.IonStormOverlay;
import com.fende.obesecat.network.NightVisionOverlayPayload;
import com.fende.obesecat.network.SammyCrossActivationPayload;
import com.fende.obesecat.network.SniperPacoFirePayload;
import com.fende.obesecat.network.TargetDummyInfoCardPayload;
import com.fende.obesecat.entity.TargetDummy;
import com.fende.obesecat.inventory.TargetDummyMenu;
import com.fende.obesecat.network.NuclearFlashPayload;
import com.fende.obesecat.world.SniperPacoManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
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
                .playToClient(IonStormPayload.TYPE, IonStormPayload.STREAM_CODEC, ModNetworking::handleIonStorm)
                .playToClient(SammyCrossActivationPayload.TYPE, SammyCrossActivationPayload.STREAM_CODEC, ModNetworking::handleSammyCrossActivation)
                .playToServer(SniperPacoFirePayload.TYPE, SniperPacoFirePayload.STREAM_CODEC, ModNetworking::handleSniperPacoFire)
                .playToServer(TargetDummyInfoCardPayload.TYPE, TargetDummyInfoCardPayload.STREAM_CODEC, ModNetworking::handleTargetDummyInfoCard);
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
    }

    private static void handleTargetDummyInfoCard(TargetDummyInfoCardPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)
                    || !(serverPlayer.level().getEntity(payload.entityId()) instanceof TargetDummy dummy)
                    || !(serverPlayer.containerMenu instanceof TargetDummyMenu menu)
                    || !menu.controls(dummy)
                    || serverPlayer.distanceToSqr(dummy) >= 64.0D) {
                return;
            }
            dummy.setInfoCardEnabled(payload.enabled());
            serverPlayer.displayClientMessage(
                    Component.literal("Target Dummy info card: " + (payload.enabled() ? "ON" : "OFF")),
                    true
            );
        });
    }
}


