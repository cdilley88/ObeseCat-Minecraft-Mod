package com.fende.obesecat.counterstrike.network;

import com.fende.obesecat.counterstrike.ObeseCatCounterstrike;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RotateStructurePayload(BlockPos corePos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RotateStructurePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ObeseCatCounterstrike.MOD_ID, "rotate_structure"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RotateStructurePayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeBlockPos(payload.corePos()),
            buffer -> new RotateStructurePayload(buffer.readBlockPos())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
