package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
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

    private ModBlockEntities() {
    }
}
