package com.fende.obesecat.network;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GeminiNuclearPayload(BlockPos origin, int durationTicks, float intensity) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GeminiNuclearPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "gemini_nuclear"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GeminiNuclearPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeBlockPos(payload.origin());
                buffer.writeInt(payload.durationTicks());
                buffer.writeFloat(payload.intensity());
            },
            buffer -> new GeminiNuclearPayload(buffer.readBlockPos(), buffer.readInt(), buffer.readFloat())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
