package com.fende.obesecat.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public final class LocalSoundHelper {
    private static final double DEFAULT_RADIUS = 24.0D;

    private LocalSoundHelper() {
    }

    public static void playLocalized(ServerLevel level, Vec3 pos, SoundEvent sound) {
        playLocalized(level, pos, sound, DEFAULT_RADIUS, 1.0F, 1.0F);
    }

    public static void playLocalized(ServerLevel level, Vec3 pos, SoundEvent sound, double radius, float volume, float pitch) {
        double radiusSquared = radius * radius;
        for (ServerPlayer player : level.getPlayers(player -> player.distanceToSqr(pos.x, pos.y, pos.z) <= radiusSquared)) {
            player.playNotifySound(sound, SoundSource.PLAYERS, volume, pitch);
        }
    }
}
