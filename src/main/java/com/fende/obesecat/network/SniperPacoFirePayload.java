package com.fende.obesecat.network;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SniperPacoFirePayload(int clickToken) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SniperPacoFirePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "sniper_paco_fire"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SniperPacoFirePayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeVarInt(payload.clickToken()),
            buffer -> new SniperPacoFirePayload(buffer.readVarInt())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
