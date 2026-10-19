package com.fende.obesecat.network;

import com.fende.obesecat.ObeseCatMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TacticalNuclearPayload(BlockPos origin, int durationTicks, float intensity) implements CustomPacketPayload {
    public static final Type<TacticalNuclearPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "tactical_nuclear"));

    public static final StreamCodec<ByteBuf, TacticalNuclearPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            TacticalNuclearPayload::origin,
            ByteBufCodecs.VAR_INT,
            TacticalNuclearPayload::durationTicks,
            ByteBufCodecs.FLOAT,
            TacticalNuclearPayload::intensity,
            TacticalNuclearPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
