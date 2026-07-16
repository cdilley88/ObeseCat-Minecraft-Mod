package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.entity.CowKing;
import com.fende.obesecat.entity.ObeseCat;
import com.fende.obesecat.entity.TargetDummy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ObeseCatMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ObeseCat>> OBESE_CAT =
            ENTITY_TYPES.register("obese_cat", () -> EntityType.Builder
                    .of(ObeseCat::new, MobCategory.CREATURE)
                    .sized(0.9F, 0.84F)
                    .clientTrackingRange(8)
                    .build(ObeseCatMod.MOD_ID + ":obese_cat"));

    public static final DeferredHolder<EntityType<?>, EntityType<CowKing>> COW_KING = ENTITY_TYPES.register(
            "cow_king",
            () -> EntityType.Builder.of(CowKing::new, MobCategory.MONSTER)
                    .sized(0.9F, 1.4F)
                    .clientTrackingRange(8)
                    .build(ObeseCatMod.MOD_ID + ":cow_king")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<TargetDummy>> TARGET_DUMMY = ENTITY_TYPES.register(
            "target_dummy", () -> EntityType.Builder.of(TargetDummy::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F).clientTrackingRange(32)
                    .build(ObeseCatMod.MOD_ID + ":target_dummy"));

    private ModEntities() {
    }
}
