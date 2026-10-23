package com.fende.obesecat.counterstrike.network;

import com.fende.obesecat.counterstrike.ObeseCatCounterstrike;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DepositResourcesPayload(BlockPos corePos) implements CustomPacketPayload {
    public static final Type<DepositResourcesPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ObeseCatCounterstrike.MOD_ID, "deposit_resources"));

    public static final StreamCodec<ByteBuf, DepositResourcesPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            DepositResourcesPayload::corePos,
            DepositResourcesPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
