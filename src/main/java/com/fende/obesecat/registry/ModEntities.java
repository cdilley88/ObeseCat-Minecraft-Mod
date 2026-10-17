package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.entity.CowKing;
import com.fende.obesecat.entity.ObeseCat;
import com.fende.obesecat.entity.SkeletonKing;
import com.fende.obesecat.entity.TheCarver;
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
                    .sized(3.8F, 5.6F)
                    .eyeHeight(4.8F)
                    .clientTrackingRange(12)
                    .build(ObeseCatMod.MOD_ID + ":cow_king")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<TheCarver>> THE_CARVER = ENTITY_TYPES.register(
            "the_carver",
            () -> EntityType.Builder.of(TheCarver::new, MobCategory.MONSTER)
                    .sized(1.4F, 2.85F)
                    .eyeHeight(2.35F)
                    .clientTrackingRange(16)
                    .build(ObeseCatMod.MOD_ID + ":the_carver")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<SkeletonKing>> SKELETON_KING = ENTITY_TYPES.register(
            "skeleton_king",
            () -> EntityType.Builder.of(SkeletonKing::new, MobCategory.MONSTER)
                    .sized(1.1F, 3.2F)
                    .eyeHeight(2.8F)
                    .clientTrackingRange(16)
                    .build(ObeseCatMod.MOD_ID + ":skeleton_king")
    );

    private ModEntities() {
    }
}
