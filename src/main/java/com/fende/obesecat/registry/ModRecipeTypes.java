package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.recipe.TransmutationRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, ObeseCatMod.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ObeseCatMod.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<TransmutationRecipe>> TRANSMUTATION =
            RECIPE_TYPES.register("transmutation", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "transmutation")
            ));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TransmutationRecipe>>
            TRANSMUTATION_SERIALIZER = RECIPE_SERIALIZERS.register(
                    "transmutation", TransmutationRecipe.Serializer::new
            );

    private ModRecipeTypes() {
    }
}
