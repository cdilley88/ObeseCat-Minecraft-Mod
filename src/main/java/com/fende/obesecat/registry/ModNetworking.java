package com.fende.obesecat.registry;

import com.fende.obesecat.client.NuclearFlashOverlay;
import com.fende.obesecat.network.NuclearFlashPayload;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(NuclearFlashPayload.TYPE, NuclearFlashPayload.STREAM_CODEC, ModNetworking::handleNuclearFlash);
    }

    private static void handleNuclearFlash(NuclearFlashPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                NuclearFlashOverlay.trigger(payload.intensity());
            }
        });
    }
}
