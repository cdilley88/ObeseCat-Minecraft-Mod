package com.fende.obesecat.counterstrike.network;

import com.fende.obesecat.counterstrike.ObeseCatCounterstrike;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record StartConstructionPayload(BlockPos corePos, String recipeId) implements CustomPacketPayload {
    public static final Type<StartConstructionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ObeseCatCounterstrike.MOD_ID, "start_construction"));

    public static final StreamCodec<ByteBuf, StartConstructionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            StartConstructionPayload::corePos,
            ByteBufCodecs.STRING_UTF8,
            StartConstructionPayload::recipeId,
            StartConstructionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
