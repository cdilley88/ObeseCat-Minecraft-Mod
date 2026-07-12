package com.fende.obesecat.network;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record IonStormPayload(boolean active, int remainingTicks) implements CustomPacketPayload {
    public static final Type<IonStormPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "ion_storm"));
    public static final StreamCodec<RegistryFriendlyByteBuf, IonStormPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> { buffer.writeBoolean(payload.active()); buffer.writeVarInt(payload.remainingTicks()); },
            buffer -> new IonStormPayload(buffer.readBoolean(), buffer.readVarInt())
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
