package com.fende.obesecat.network;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record NuclearFlashPayload(BlockPos origin, float intensity) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<NuclearFlashPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "nuclear_flash"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NuclearFlashPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeBlockPos(payload.origin());
                buffer.writeFloat(payload.intensity());
            },
            buffer -> new NuclearFlashPayload(buffer.readBlockPos(), buffer.readFloat())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
