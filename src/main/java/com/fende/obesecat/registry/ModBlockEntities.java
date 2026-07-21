package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.block.entity.AtomicCanOpenerBlockEntity;
import com.fende.obesecat.block.entity.FoodBinBlockEntity;
import com.fende.obesecat.block.entity.CatChargerBlockEntity;
import com.fende.obesecat.block.entity.CreativeFoodBinBlockEntity;
import com.fende.obesecat.block.entity.EchoingBlastChamberBlockEntity;
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AtomicCanOpenerBlockEntity>> CAN_OPENER =
            BLOCK_ENTITIES.register("can_opener", () -> BlockEntityType.Builder.of(
                    AtomicCanOpenerBlockEntity::new,
                    ModBlocks.CAN_OPENER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoodBinBlockEntity>> FOOD_BIN =
            BLOCK_ENTITIES.register("food_bin", () -> BlockEntityType.Builder.of(
                    FoodBinBlockEntity::new,
                    ModBlocks.FOOD_BIN.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CatChargerBlockEntity>> CAT_CHARGER =
            BLOCK_ENTITIES.register("cat_charger", () -> BlockEntityType.Builder.of(
                    CatChargerBlockEntity::new,
                    ModBlocks.CAT_CHARGER.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeFoodBinBlockEntity>> CREATIVE_FOOD_BIN =
            BLOCK_ENTITIES.register("creative_food_bin", () -> BlockEntityType.Builder.of(
                    CreativeFoodBinBlockEntity::new,
                    ModBlocks.CREATIVE_FOOD_BIN.get()
            ).build(null));

    private ModBlockEntities() {
    }
}
