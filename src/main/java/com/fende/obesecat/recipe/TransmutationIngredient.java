package com.fende.obesecat.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public record TransmutationIngredient(Item item, int count) {
    public static final Codec<TransmutationIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec()
                    .validate(item -> item == Items.AIR
                            ? DataResult.error(() -> "Transmutation ingredients cannot be air")
                            : DataResult.success(item))
                    .fieldOf("item")
                    .forGetter(TransmutationIngredient::item),
            Codec.INT.validate(count -> count > 0
                            ? DataResult.success(count)
                            : DataResult.error(() -> "Transmutation ingredient count must be positive"))
                    .fieldOf("count")
                    .forGetter(TransmutationIngredient::count)
    ).apply(instance, TransmutationIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TransmutationIngredient> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.registry(Registries.ITEM), TransmutationIngredient::item,
                    ByteBufCodecs.VAR_INT, TransmutationIngredient::count,
                    TransmutationIngredient::new
            );
}
