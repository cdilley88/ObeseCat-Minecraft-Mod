package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.block.entity.CatChargerBlockEntity;
import com.fende.obesecat.block.entity.CreativeTabbyriumSiloBlockEntity;
import com.fende.obesecat.block.entity.EchoingBlastChamberBlockEntity;
import com.fende.obesecat.block.entity.TabbyriumRefineryBlockEntity;
import com.fende.obesecat.block.entity.TabbyriumSiloBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ObeseCatMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EchoingBlastChamberBlockEntity>> ECHOING_BLAST_CHAMBER =
            BLOCK_ENTITIES.register("echoing_blast_chamber", () -> BlockEntityType.Builder.of(
                    EchoingBlastChamberBlockEntity::new,
                    ModBlocks.ECHOING_BLAST_CHAMBER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TabbyriumRefineryBlockEntity>> TABBYRIUM_REFINERY =
            BLOCK_ENTITIES.register("tabbyrium_refinery", () -> BlockEntityType.Builder.of(
                    TabbyriumRefineryBlockEntity::new,
                    ModBlocks.TABBYRIUM_REFINERY.get(),
                    ModBlocks.CAN_OPENER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TabbyriumSiloBlockEntity>> TABBYRIUM_SILO =
            BLOCK_ENTITIES.register("tabbyrium_silo", () -> BlockEntityType.Builder.of(
                    TabbyriumSiloBlockEntity::new,
                    ModBlocks.TABBYRIUM_SILO.get(),
                    ModBlocks.FOOD_BIN.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CatChargerBlockEntity>> CAT_CHARGER =
            BLOCK_ENTITIES.register("cat_charger", () -> BlockEntityType.Builder.of(
                    CatChargerBlockEntity::new,
                    ModBlocks.CAT_CHARGER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeTabbyriumSiloBlockEntity>> CREATIVE_TABBYRIUM_SILO =
            BLOCK_ENTITIES.register("creative_tabbyrium_silo", () -> BlockEntityType.Builder.of(
                    CreativeTabbyriumSiloBlockEntity::new,
                    ModBlocks.CREATIVE_TABBYRIUM_SILO.get(),
                    ModBlocks.CREATIVE_FOOD_BIN.get()
            ).build(null));

    // Compatibility references
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TabbyriumRefineryBlockEntity>> CAN_OPENER = TABBYRIUM_REFINERY;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TabbyriumSiloBlockEntity>> FOOD_BIN = TABBYRIUM_SILO;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeTabbyriumSiloBlockEntity>> CREATIVE_FOOD_BIN = CREATIVE_TABBYRIUM_SILO;

    private ModBlockEntities() {
    }
}
