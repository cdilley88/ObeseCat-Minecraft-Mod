package com.fende.obesecat.world;

import com.fende.obesecat.network.SammyCrossActivationPayload;
import com.fende.obesecat.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SammyCrossManager {
    private SammyCrossManager() {
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();

        // Damage that bypasses invulnerability (e.g. /kill, void) ignores totem protection
        if (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        // Check both hands for Sammy's Cross
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = entity.getItemInHand(hand);
            if (stack.is(ModItems.SAMMYS_CROSS.get())) {
                stack.shrink(1);

                // Mirror vanilla Totem of Undying effect sequence
                entity.setHealth(1.0F);
                entity.removeEffectsCuredBy(EffectCures.PROTECTED_BY_TOTEM);
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
                entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
                entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));

                // Vanilla entity event 35 → particles + sound for all nearby clients
                entity.level().broadcastEntityEvent(entity, (byte) 35);

                // Send custom packet to override the overlay texture with SammyCross on the local player
                if (entity instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer, new SammyCrossActivationPayload());
                }

                event.setCanceled(true);
                return;
            }
        }
    }
}
