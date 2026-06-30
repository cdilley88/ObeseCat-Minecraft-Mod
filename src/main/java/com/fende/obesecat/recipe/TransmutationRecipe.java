package com.fende.obesecat.recipe;

import com.fende.obesecat.inventory.TransmutationCubeInventory;
import com.fende.obesecat.registry.ModRecipeTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record TransmutationRecipe(List<TransmutationIngredient> ingredients, ItemStack result) implements Recipe<TransmutationInput> {
    private static final MapCodec<TransmutationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TransmutationIngredient.CODEC.listOf()
                    .validate(ingredients -> ingredients.isEmpty()
                            ? DataResult.error(() -> "A transmutation recipe needs at least one ingredient")
                            : ingredients.size() > TransmutationCubeInventory.SLOT_COUNT
                            ? DataResult.error(() -> "A transmutation recipe cannot use more than 12 ingredients")
                            : DataResult.success(ingredients))
                    .fieldOf("ingredients")
                    .forGetter(TransmutationRecipe::ingredients),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(TransmutationRecipe::result)
    ).apply(instance, TransmutationRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TransmutationRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public TransmutationRecipe decode(RegistryFriendlyByteBuf buffer) {
            int ingredientCount = buffer.readVarInt();
            if (ingredientCount < 1) {
                throw new DecoderException("A transmutation recipe needs at least one ingredient");
            }
            if (ingredientCount > TransmutationCubeInventory.SLOT_COUNT) {
                throw new DecoderException("A transmutation recipe cannot use more than 12 ingredients");
            }

            List<TransmutationIngredient> ingredients = new ArrayList<>(ingredientCount);
            for (int index = 0; index < ingredientCount; index++) {
                ingredients.add(TransmutationIngredient.STREAM_CODEC.decode(buffer));
            }
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            return new TransmutationRecipe(ingredients, result);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, TransmutationRecipe recipe) {
            int ingredientCount = recipe.ingredients.size();
            if (ingredientCount < 1) {
                throw new EncoderException("A transmutation recipe needs at least one ingredient");
            }
            if (ingredientCount > TransmutationCubeInventory.SLOT_COUNT) {
                throw new EncoderException("A transmutation recipe cannot use more than 12 ingredients");
            }

            buffer.writeVarInt(ingredientCount);
            for (TransmutationIngredient ingredient : recipe.ingredients) {
                TransmutationIngredient.STREAM_CODEC.encode(buffer, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
        }
    };

    public TransmutationRecipe {
        ingredients = List.copyOf(ingredients);
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("A transmutation recipe needs at least one ingredient");
        }
        if (ingredients.size() > TransmutationCubeInventory.SLOT_COUNT) {
            throw new IllegalArgumentException("A transmutation recipe cannot use more than 12 ingredients");
        }
        result = result.copy();
    }

    @Override
    public boolean matches(TransmutationInput input, Level level) {
        return expectedCounts().equals(actualCounts(input));
    }

    @Override
    public ItemStack assemble(TransmutationInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= ingredients.size();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.TRANSMUTATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.TRANSMUTATION.get();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(TransmutationInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }

    private Map<Item, Integer> expectedCounts() {
        Map<Item, Integer> counts = new HashMap<>();
        ingredients.forEach(ingredient -> counts.merge(ingredient.item(), ingredient.count(), Integer::sum));
        return counts;
    }

    private static Map<Item, Integer> actualCounts(TransmutationInput input) {
        Map<Item, Integer> counts = new HashMap<>();
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (!stack.isEmpty()) {
                counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }
        return counts;
    }

    public static final class Serializer implements RecipeSerializer<TransmutationRecipe> {
        @Override
        public MapCodec<TransmutationRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TransmutationRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
