package com.fende.obesecat.counterstrike.registry;

import com.fende.obesecat.counterstrike.ObeseCatCounterstrike;
import com.fende.obesecat.counterstrike.entity.C4ChargeEntity;
import com.fende.obesecat.counterstrike.entity.FragGrenadeProjectile;
import com.fende.obesecat.counterstrike.entity.RocketProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CSEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ObeseCatCounterstrike.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<RocketProjectile>> ROCKET =
            ENTITY_TYPES.register("rocket_projectile", () -> EntityType.Builder
                    .<RocketProjectile>of(RocketProjectile::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build(ObeseCatCounterstrike.MOD_ID + ":rocket_projectile"));

    public static final DeferredHolder<EntityType<?>, EntityType<FragGrenadeProjectile>> FRAG_GRENADE =
            ENTITY_TYPES.register("frag_grenade_projectile", () -> EntityType.Builder
                    .<FragGrenadeProjectile>of(FragGrenadeProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build(ObeseCatCounterstrike.MOD_ID + ":frag_grenade_projectile"));

    public static final DeferredHolder<EntityType<?>, EntityType<C4ChargeEntity>> C4_CHARGE =
            ENTITY_TYPES.register("c4_charge", () -> EntityType.Builder
                    .<C4ChargeEntity>of(C4ChargeEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.2F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build(ObeseCatCounterstrike.MOD_ID + ":c4_charge"));

    private CSEntities() {
    }
}
