package com.fende.obesecat.world;

import com.fende.obesecat.network.IonStormPayload;
import com.fende.obesecat.registry.ModSounds;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class IonStormManager {
    public static final int DURATION_TICKS = 20 * 60;
    public static final int WARNING_DELAY_TICKS = 20 * 3;
    public static final double RADIUS = 150.0D;
    private static final double RADIUS_SQUARED = RADIUS * RADIUS;
    private static final List<Storm> STORMS = new ArrayList<>();
    private IonStormManager() {}

    public static void start(ServerLevel level, BlockPos center) {
        BlockPos stormCenter = center.immutable();
        LocalSoundHelper.playLocalized(level, Vec3.atCenterOf(stormCenter), ModSounds.ION_STORM_WARNING.get(), RADIUS, 1.5F, 1.0F);
        STORMS.add(new Storm(level, stormCenter));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        for (Storm storm : List.copyOf(STORMS)) {
            if (storm.level != level) continue;
            if (storm.warningTicks > 0) {
                storm.warningTicks--;
                continue;
            }
            storm.remainingTicks--;
            if (storm.strikeDelay-- <= 0) {
                strike(storm);
                storm.strikeDelay = 6 + level.random.nextInt(9);
            }
            if (storm.remainingTicks % 10 == 0) emitAtmosphere(storm);
            if (storm.remainingTicks % 20 == 0) syncPlayers(storm);
            if (storm.remainingTicks <= 0) {
                end(storm);
                STORMS.remove(storm);
            }
        }
    }

    private static void strike(Storm storm) {
        double angle = storm.level.random.nextDouble() * Math.PI * 2.0D;
        double distance = Math.sqrt(storm.level.random.nextDouble()) * RADIUS;
        int x = storm.center.getX() + (int)Math.round(Math.cos(angle) * distance);
        int z = storm.center.getZ() + (int)Math.round(Math.sin(angle) * distance);
        BlockPos target = storm.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, storm.center.getY(), z));
        LightningStabManager.scheduleIonStormStrike(storm.level, target);
    }

    private static void emitAtmosphere(Storm storm) {
        for (ServerPlayer player : storm.level.players()) {
            if (player.distanceToSqr(storm.center.getX() + 0.5D, storm.center.getY() + 0.5D, storm.center.getZ() + 0.5D) > RADIUS_SQUARED) continue;
            storm.level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 4.0D, player.getZ(), 6, 12.0D, 5.0D, 12.0D, 0.025D);
            storm.level.sendParticles(ParticleTypes.ASH, player.getX(), player.getY() + 7.0D, player.getZ(), 22, 14.0D, 7.0D, 14.0D, 0.018D);
            storm.level.sendParticles(ParticleTypes.WHITE_ASH, player.getX(), player.getY() + 6.0D, player.getZ(), 14, 13.0D, 6.0D, 13.0D, 0.012D);
        }
    }

    private static void syncPlayers(Storm storm) {
        Set<UUID> inside = new HashSet<>();
        for (ServerPlayer player : storm.level.players()) {
            boolean isInside = player.distanceToSqr(storm.center.getX() + 0.5D, storm.center.getY() + 0.5D, storm.center.getZ() + 0.5D) <= RADIUS_SQUARED;
            if (isInside) {
                inside.add(player.getUUID());
                PacketDistributor.sendToPlayer(player, new IonStormPayload(true, storm.remainingTicks + 25));
            } else if (storm.viewers.remove(player.getUUID())) {
                PacketDistributor.sendToPlayer(player, new IonStormPayload(false, 0));
            }
        }
        storm.viewers.addAll(inside);
    }

    private static void end(Storm storm) {
        for (ServerPlayer player : storm.level.players())
            if (storm.viewers.contains(player.getUUID())) PacketDistributor.sendToPlayer(player, new IonStormPayload(false, 0));
    }

    private static final class Storm {
        final ServerLevel level; final BlockPos center; final Set<UUID> viewers = new HashSet<>();
        int warningTicks = WARNING_DELAY_TICKS; int remainingTicks = DURATION_TICKS; int strikeDelay = 1;
        Storm(ServerLevel level, BlockPos center) { this.level = level; this.center = center; }
    }
}
