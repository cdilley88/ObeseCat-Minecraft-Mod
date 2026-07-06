package com.fende.obesecat.network;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SammyCrossActivationPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SammyCrossActivationPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "sammy_cross_activation"));
    public static final StreamCodec<FriendlyByteBuf, SammyCrossActivationPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> { /* no data */ },
            buffer -> new SammyCrossActivationPayload()
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
