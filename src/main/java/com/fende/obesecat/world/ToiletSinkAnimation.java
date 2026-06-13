package com.fende.obesecat.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class ToiletSinkAnimation {
    private static final int SINK_TICKS = 60;
    private static final double SINK_DISTANCE = 1.35D;
    private static final double SINK_SPEED = -SINK_DISTANCE / SINK_TICKS;
    private static final List<ActiveSink> ACTIVE_SINKS = new ArrayList<>();

    private ToiletSinkAnimation() {
    }

    public static void create(ServerLevel level, BlockPos pos, BlockState state) {
        FallingBlockEntity display = FallingBlockEntity.fall(level, pos, state);
        display.dropItem = false;
        display.noPhysics = true;
        display.setNoGravity(true);
        display.setDeltaMovement(0.0D, SINK_SPEED, 0.0D);
        ACTIVE_SINKS.add(new ActiveSink(level, display));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || ACTIVE_SINKS.isEmpty()) {
            return;
        }

        Iterator<ActiveSink> iterator = ACTIVE_SINKS.iterator();
        while (iterator.hasNext()) {
            ActiveSink sink = iterator.next();
            if (sink.level != level) {
                continue;
            }

            if (!sink.display.isAlive()) {
                iterator.remove();
                continue;
            }

            sink.ageTicks++;
            sink.display.noPhysics = true;
            sink.display.setNoGravity(true);
            sink.display.setDeltaMovement(0.0D, SINK_SPEED, 0.0D);

            if (sink.ageTicks >= SINK_TICKS) {
                sink.display.discard();
                iterator.remove();
            }
        }
    }

    private static final class ActiveSink {
        private final ServerLevel level;
        private final FallingBlockEntity display;
        private int ageTicks;

        private ActiveSink(ServerLevel level, FallingBlockEntity display) {
            this.level = level;
            this.display = display;
        }
    }
}
