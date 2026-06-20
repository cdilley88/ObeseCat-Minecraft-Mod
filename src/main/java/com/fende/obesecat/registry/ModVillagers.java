package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModVillagers {
    public static final ResourceKey<PoiType> MANHATTAN_PHYSICIST_POI_KEY = ResourceKey.create(
            Registries.POINT_OF_INTEREST_TYPE,
            ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "manhattan_physicist")
    );

    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, ObeseCatMod.MOD_ID);

    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, ObeseCatMod.MOD_ID);

    public static final DeferredHolder<PoiType, PoiType> MANHATTAN_PHYSICIST_POI =
            POI_TYPES.register(
                    "manhattan_physicist",
                    () -> new PoiType(getBlockStates(ModBlocks.NUCLEAR_LIBRARY.get()), 1, 1)
            );

    public static final DeferredHolder<VillagerProfession, VillagerProfession> MANHATTAN_PHYSICIST =
            PROFESSIONS.register(
                    "manhattan_physicist",
                    () -> new VillagerProfession(
                            "manhattan_physicist",
                            holder -> holder.is(MANHATTAN_PHYSICIST_POI_KEY),
                            holder -> holder.is(MANHATTAN_PHYSICIST_POI_KEY),
                            ImmutableSet.of(),
                            ImmutableSet.of(),
                            SoundEvents.VILLAGER_WORK_LIBRARIAN
                    )
            );

    private static Set<BlockState> getBlockStates(net.minecraft.world.level.block.Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }

    private ModVillagers() {
    }
}
