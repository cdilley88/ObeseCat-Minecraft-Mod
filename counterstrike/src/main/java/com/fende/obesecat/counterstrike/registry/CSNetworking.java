package com.fende.obesecat.counterstrike.registry;

import com.fende.obesecat.counterstrike.block.entity.ConstructionYardCoreBlockEntity;
import com.fende.obesecat.counterstrike.deploy.IDeployedCore;
import com.fende.obesecat.counterstrike.network.DepositResourcesPayload;
import com.fende.obesecat.counterstrike.network.RotateStructurePayload;
import com.fende.obesecat.counterstrike.network.StartConstructionPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class CSNetworking {
    private CSNetworking() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(RotateStructurePayload.TYPE, RotateStructurePayload.STREAM_CODEC, CSNetworking::handleRotateStructure)
                .playToServer(StartConstructionPayload.TYPE, StartConstructionPayload.STREAM_CODEC, CSNetworking::handleStartConstruction)
                .playToServer(DepositResourcesPayload.TYPE, DepositResourcesPayload.STREAM_CODEC, CSNetworking::handleDepositResources);
    }

    private static void handleRotateStructure(RotateStructurePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.level().getBlockEntity(payload.corePos()) instanceof IDeployedCore core) {
                    core.rotateStructure(player.level(), payload.corePos(), player);
                }
            }
        });
    }

    private static void handleStartConstruction(StartConstructionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.level().getBlockEntity(payload.corePos()) instanceof ConstructionYardCoreBlockEntity conYard) {
                    conYard.startConstruction(payload.recipeId(), player);
                } else if (player.level().getBlockEntity(payload.corePos()) instanceof com.fende.obesecat.counterstrike.block.entity.BarracksCoreBlockEntity barracks) {
                    barracks.startConstruction(payload.recipeId(), player);
                }
            }
        });
    }

    private static void handleDepositResources(DepositResourcesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.level().getBlockEntity(payload.corePos()) instanceof ConstructionYardCoreBlockEntity conYard) {
                    conYard.depositPlayerInventory(player);
                }
            }
        });
    }
}
