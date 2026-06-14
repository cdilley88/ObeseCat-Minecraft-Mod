package com.fende.obesecat.world;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class TinyPlanetProtection {
    private static final double RADIUS = 3.0D;
    private static final int ASTEROID_COUNT = 10;
    private static final double ORBIT_SPEED = 0.18D;
    private static final BlockParticleOption STONE_PARTICLE = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState());
    private static final Map<UUID, Integer> ACTIVE_PLAYERS = new HashMap<>();

    private TinyPlanetProtection() {
    }

    public static void activate(ServerPlayer player, int durationTicks) {
        ACTIVE_PLAYERS.put(player.getUUID(), durationTicks);
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || ACTIVE_PLAYERS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, Integer>> iterator = ACTIVE_PLAYERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                iterator.remove();
                continue;
            }
            if (player.level() != level) {
                continue;
            }

            int remainingTicks = entry.getValue();
            removeProjectiles(level, player);
            spawnOrbitParticles(level, player, remainingTicks);

            remainingTicks--;
            if (remainingTicks <= 0) {
                iterator.remove();
            } else {
                entry.setValue(remainingTicks);
            }
        }
    }

    private static void removeProjectiles(ServerLevel level, ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(RADIUS);
        for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, area, Entity::isAlive)) {
            if (projectile.distanceToSqr(player) <= RADIUS * RADIUS) {
                projectile.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    private static void spawnOrbitParticles(ServerLevel level, ServerPlayer player, int remainingTicks) {
        long gameTime = level.getGameTime();
        double centerX = player.getX();
        double centerY = player.getY() + 1.05D;
        double centerZ = player.getZ();
        double baseAngle = -gameTime * ORBIT_SPEED;

        for (int i = 0; i < ASTEROID_COUNT; i++) {
            double spacing = Math.PI * 2.0D / ASTEROID_COUNT;
            double angle = baseAngle + i * spacing;
            double radius = i % 2 == 0 ? 1.65D : 2.15D;
            double yOffset = Math.sin(angle * 2.0D) * 0.22D;
            double x = centerX + Math.cos(angle) * radius;
            double y = centerY + yOffset;
            double z = centerZ + Math.sin(angle) * radius;
            double motionX = Math.sin(angle) * 0.035D;
            double motionZ = -Math.cos(angle) * 0.035D;

            level.sendParticles(STONE_PARTICLE, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            if ((remainingTicks + i) % 12 == 0) {
                level.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, motionX, 0.0D, motionZ, 0.01D);
            }
        }
    }
}
