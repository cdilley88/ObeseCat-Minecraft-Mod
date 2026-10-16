package com.fende.obesecat.network;

import com.fende.obesecat.ObeseCatMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BigGeminiNuclearPayload(BlockPos origin, int durationTicks, float intensity) implements CustomPacketPayload {
    public static final Type<BigGeminiNuclearPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "big_gemini_nuclear"));

    public static final StreamCodec<ByteBuf, BigGeminiNuclearPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            BigGeminiNuclearPayload::origin,
            ByteBufCodecs.VAR_INT,
            BigGeminiNuclearPayload::durationTicks,
            ByteBufCodecs.FLOAT,
            BigGeminiNuclearPayload::intensity,
            BigGeminiNuclearPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
