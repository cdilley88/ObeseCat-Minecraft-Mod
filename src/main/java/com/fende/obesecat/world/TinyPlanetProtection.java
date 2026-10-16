package com.fende.obesecat.world;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class TinyPlanetProtection {
    private static final double DEFLECT_RADIUS = 3.2D;
    private static final double ORBIT_RADIUS = 2.4D;
    private static final double ORBIT_SPEED = 0.22D;

    private static final BlockParticleOption LODESTONE_PARTICLE = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.LODESTONE.defaultBlockState());
    private static final BlockParticleOption DEEPSLATE_PARTICLE = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState());
    private static final BlockParticleOption GOLD_PARTICLE = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.RAW_GOLD_BLOCK.defaultBlockState());
    private static final BlockParticleOption AMETHYST_PARTICLE = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_BLOCK.defaultBlockState());

    private static final Map<UUID, Integer> ACTIVE_PLAYERS = new HashMap<>();

    private TinyPlanetProtection() {
    }

    public static void activate(ServerPlayer player, int durationTicks) {
        ACTIVE_PLAYERS.put(player.getUUID(), durationTicks);
    }

    public static void deactivate(ServerPlayer player) {
        ACTIVE_PLAYERS.remove(player.getUUID());
    }

    public static boolean isActive(Player player) {
        return ACTIVE_PLAYERS.containsKey(player.getUUID());
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || ACTIVE_PLAYERS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, Integer>> iterator = ACTIVE_PLAYERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null || !player.isAlive()) {
                iterator.remove();
                continue;
            }
            if (player.level() != level) {
                continue;
            }

            // Collapse shield if player ran out of food and is not in creative/instabuild
            if (!player.getAbilities().instabuild && player.getFoodData().getFoodLevel() <= 0) {
                triggerShieldOverload(level, player);
                iterator.remove();
                continue;
            }

            int remainingTicks = entry.getValue();

            // Intercept incoming projectiles
            deflectProjectiles(level, player);

            // Spawn orbiting moons and celestial asteroid belt
            spawnOrbitingMoons(level, player, remainingTicks);

            remainingTicks--;
            if (remainingTicks <= 0) {
                // Shield expired naturally
                triggerShieldExpired(level, player);
                iterator.remove();
            } else {
                entry.setValue(remainingTicks);
            }
        }
    }

    /**
     * Blocks projectile damage and converts damage to hunger at a quarter rate.
     */
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        if (!isActive(player)) {
            return;
        }

        DamageSource source = event.getSource();
        boolean isProjectileDamage = source.is(DamageTypeTags.IS_PROJECTILE)
                || source.getDirectEntity() instanceof Projectile
                || (source.is(DamageTypeTags.IS_FIRE) && source.getDirectEntity() instanceof Projectile);

        if (!isProjectileDamage) {
            return;
        }

        float incomingDamage = event.getAmount();
        int hungerCost = Math.max(1, Math.round(incomingDamage / 4.0F));

        if (!player.getAbilities().instabuild) {
            int currentFood = player.getFoodData().getFoodLevel();
            int newFood = Math.max(0, currentFood - hungerCost);
            player.getFoodData().setFoodLevel(newFood);
            player.causeFoodExhaustion(hungerCost * 0.5F);

            if (newFood <= 0) {
                triggerShieldOverload(level, player);
                deactivate(player);
            }
        }

        // Completely negate the damage to health
        event.setCanceled(true);

        // Impact effects
        spawnDeflectionEffects(level, player.getX(), player.getY() + 1.0D, player.getZ());
        player.displayClientMessage(Component.translatable("item.obesecat.tiny_planet.absorbed", hungerCost), true);
    }

    private static void deflectProjectiles(ServerLevel level, ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(DEFLECT_RADIUS);
        List<Projectile> projectiles = level.getEntitiesOfClass(Projectile.class, area, Entity::isAlive);

        for (Projectile projectile : projectiles) {
            // Ignore player's own newly fired projectiles
            if (projectile.getOwner() == player && projectile.tickCount < 12) {
                continue;
            }

            double distSqr = projectile.distanceToSqr(player);
            if (distSqr <= DEFLECT_RADIUS * DEFLECT_RADIUS) {
                float nominalDamage = calculateNominalDamage(projectile);
                int hungerCost = Math.max(1, Math.round(nominalDamage / 4.0F));

                if (!player.getAbilities().instabuild) {
                    int currentFood = player.getFoodData().getFoodLevel();
                    int newFood = Math.max(0, currentFood - hungerCost);
                    player.getFoodData().setFoodLevel(newFood);
                    player.causeFoodExhaustion(hungerCost * 0.5F);

                    if (newFood <= 0) {
                        triggerShieldOverload(level, player);
                        deactivate(player);
                    }
                }

                // Visual & sound deflection effects
                spawnDeflectionEffects(level, projectile.getX(), projectile.getY(), projectile.getZ());

                // Intercept & discard the projectile before it can hit or explode
                projectile.discard();

                player.displayClientMessage(Component.translatable("item.obesecat.tiny_planet.deflected", hungerCost), true);

                if (!isActive(player)) {
                    break;
                }
            }
        }
    }

    private static float calculateNominalDamage(Projectile projectile) {
        if (projectile instanceof DragonFireball) {
            return 12.0F;
        } else if (projectile instanceof LargeFireball || projectile instanceof WitherSkull) {
            return 10.0F;
        } else if (projectile instanceof SmallFireball) {
            return 6.0F;
        } else if (projectile instanceof AbstractArrow arrow) {
            return Math.max(4.0F, (float) (arrow.getBaseDamage() * 2.5F));
        }
        return 5.0F;
    }

    private static void spawnDeflectionEffects(ServerLevel level, double x, double y, double z) {
        level.sendParticles(ParticleTypes.FLASH, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(AMETHYST_PARTICLE, x, y, z, 14, 0.25D, 0.25D, 0.25D, 0.08D);
        level.sendParticles(LODESTONE_PARTICLE, x, y, z, 10, 0.2D, 0.2D, 0.2D, 0.05D);
        level.sendParticles(ParticleTypes.POOF, x, y, z, 5, 0.15D, 0.15D, 0.15D, 0.02D);

        level.playSound(null, x, y, z, SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 1.4F);
        level.playSound(null, x, y, z, SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 1.2F, 1.8F);
        level.playSound(null, x, y, z, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.5F, 2.0F);
    }

    private static void triggerShieldOverload(ServerLevel level, ServerPlayer player) {
        level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 1.0D, player.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(AMETHYST_PARTICLE, player.getX(), player.getY() + 1.0D, player.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.1D);
        level.sendParticles(DEEPSLATE_PARTICLE, player.getX(), player.getY() + 1.0D, player.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.1D);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 1.0F, 1.2F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.2F, 1.4F);

        player.displayClientMessage(Component.translatable("item.obesecat.tiny_planet.overloaded"), true);
    }

    private static void triggerShieldExpired(ServerLevel level, ServerPlayer player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CONDUIT_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 1.8F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 0.65F);
        player.displayClientMessage(Component.translatable("item.obesecat.tiny_planet.expired"), true);
    }

    /**
     * Renders 3 distinct orbiting moons/asteroids around the player:
     * 1. Lodestone/Deepslate Rock Asteroid
     * 2. Golden Meteorite
     * 3. Amethyst Lunar Moon
     */
    private static void spawnOrbitingMoons(ServerLevel level, ServerPlayer player, int remainingTicks) {
        long gameTime = level.getGameTime();
        double baseAngle = gameTime * ORBIT_SPEED;
        double centerX = player.getX();
        double centerY = player.getY() + 1.15D;
        double centerZ = player.getZ();

        double[] angleOffsets = {0.0D, (Math.PI * 2.0D) / 3.0D, (Math.PI * 4.0D) / 3.0D};

        for (int i = 0; i < 3; i++) {
            double angle = baseAngle + angleOffsets[i];
            double waveY = Math.sin(angle * 2.0D) * 0.32D;

            double mx = centerX + Math.cos(angle) * ORBIT_RADIUS;
            double my = centerY + waveY;
            double mz = centerZ + Math.sin(angle) * ORBIT_RADIUS;

            // Spawn distinct moon body based on index
            if (i == 0) {
                // Asteroid 1: Lodestone & Deepslate Core with dust trail
                level.sendParticles(LODESTONE_PARTICLE, mx, my, mz, 2, 0.05D, 0.05D, 0.05D, 0.0D);
                level.sendParticles(DEEPSLATE_PARTICLE, mx, my - 0.08D, mz, 1, 0.02D, 0.02D, 0.02D, 0.0D);
                level.sendParticles(ParticleTypes.SMOKE, mx, my, mz, 1, 0.0D, 0.0D, 0.0D, 0.01D);
            } else if (i == 1) {
                // Asteroid 2: Raw Gold Meteorite with golden shimmer
                level.sendParticles(GOLD_PARTICLE, mx, my, mz, 2, 0.05D, 0.05D, 0.05D, 0.0D);
                level.sendParticles(ParticleTypes.GLOW, mx, my, mz, 1, 0.02D, 0.02D, 0.02D, 0.01D);
                if (gameTime % 2 == 0) {
                    level.sendParticles(ParticleTypes.CRIT, mx, my, mz, 1, 0.0D, 0.0D, 0.0D, 0.02D);
                }
            } else {
                // Asteroid 3: Amethyst Lunar Moon with astral gleam
                level.sendParticles(AMETHYST_PARTICLE, mx, my, mz, 2, 0.05D, 0.05D, 0.05D, 0.0D);
                level.sendParticles(ParticleTypes.END_ROD, mx, my, mz, 1, 0.01D, 0.01D, 0.01D, 0.01D);
                if (gameTime % 2 == 0) {
                    level.sendParticles(ParticleTypes.ENCHANT, mx, my, mz, 1, 0.0D, 0.0D, 0.0D, 0.02D);
                }
            }
        }

        // Faint orbital ring trajectory tracing around the player
        if (gameTime % 2 == 0) {
            double ringAngle = (gameTime * 0.45D) % (Math.PI * 2.0D);
            double rx = centerX + Math.cos(ringAngle) * ORBIT_RADIUS;
            double ry = centerY + Math.sin(ringAngle * 2.0D) * 0.32D;
            double rz = centerZ + Math.sin(ringAngle) * ORBIT_RADIUS;
            level.sendParticles(ParticleTypes.PORTAL, rx, ry, rz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }
}
