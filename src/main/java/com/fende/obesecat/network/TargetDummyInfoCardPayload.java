package com.fende.obesecat.network;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TargetDummyInfoCardPayload(int entityId, boolean enabled) implements CustomPacketPayload {
    public static final Type<TargetDummyInfoCardPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "target_dummy_info_card"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TargetDummyInfoCardPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.entityId());
                buffer.writeBoolean(payload.enabled());
            },
            buffer -> new TargetDummyInfoCardPayload(buffer.readVarInt(), buffer.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
