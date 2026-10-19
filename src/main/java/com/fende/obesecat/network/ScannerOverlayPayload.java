package com.fende.obesecat.network;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ScannerOverlayPayload(boolean active) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ScannerOverlayPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "scanner_overlay"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScannerOverlayPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeBoolean(payload.active()),
            buffer -> new ScannerOverlayPayload(buffer.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
