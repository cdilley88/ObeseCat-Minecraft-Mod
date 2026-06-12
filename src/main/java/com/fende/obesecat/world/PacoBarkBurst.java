package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModSounds;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class PacoBarkBurst {
    private static final List<PendingBark> PENDING_BARKS = new ArrayList<>();

    private PacoBarkBurst() {
    }

    public static void schedule(ServerLevel level, double x, double y, double z, int delayTicks) {
        PENDING_BARKS.add(new PendingBark(level, x, y, z, Math.max(delayTicks, 0)));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || PENDING_BARKS.isEmpty()) {
            return;
        }

        Iterator<PendingBark> iterator = PENDING_BARKS.iterator();
        while (iterator.hasNext()) {
            PendingBark bark = iterator.next();
            if (bark.level != level) {
                continue;
            }

            bark.delayTicks--;
            if (bark.delayTicks <= 0) {
                playBark(level, bark.x, bark.y, bark.z);
                iterator.remove();
            }
        }
    }

    private static void playBark(ServerLevel level, double x, double y, double z) {
        SoundEvent bark = switch (level.random.nextInt(3)) {
            case 0 -> ModSounds.PACO_BARK_1.get();
            case 1 -> ModSounds.PACO_BARK_2.get();
            default -> ModSounds.PACO_BARK_3.get();
        };
        level.playSound(null, x, y, z, bark, SoundSource.PLAYERS, 1.0F, 0.92F + (level.random.nextFloat() * 0.16F));
    }

    private static final class PendingBark {
        private final ServerLevel level;
        private final double x;
        private final double y;
        private final double z;
        private int delayTicks;

        private PendingBark(ServerLevel level, double x, double y, double z, int delayTicks) {
            this.level = level;
            this.x = x;
            this.y = y;
            this.z = z;
            this.delayTicks = delayTicks;
        }
    }
}
