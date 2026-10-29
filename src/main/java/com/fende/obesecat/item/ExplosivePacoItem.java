package com.fende.obesecat.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class ExplosivePacoItem extends PacoItem {
    private static final double EXPLOSION_RANGE = 128.0D;
    private static final float EXPLOSION_POWER = 4.0F; // Standard TNT explosion power
    private static final int BARK_TO_EXPLOSION_DELAY_TICKS = 15; // Short delay after the bark (0.75s)
    private static final int COOLDOWN_TICKS = 30; // 1.5 seconds

    private static final List<PendingExplosion> PENDING_EXPLOSIONS = new ArrayList<>();

    public ExplosivePacoItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void applyBarkEffect(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos targetPos = resolveTargetPosition(player, EXPLOSION_RANGE);
        if (targetPos == null) {
            return;
        }

        scheduleDelayedExplosion(serverLevel, player, targetPos, BARK_TO_EXPLOSION_DELAY_TICKS);
    }

    public static void scheduleDelayedExplosion(ServerLevel level, Player player, BlockPos pos, int delayTicks) {
        if (delayTicks <= 0) {
            level.explode(
                    player,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    EXPLOSION_POWER,
                    Level.ExplosionInteraction.TNT
            );
            return;
        }

        // Prompt cues at target location: sound of TNT primed fuse hiss and smoke puff
        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.2F);
        level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 8, 0.25D, 0.25D, 0.25D, 0.02D);

        PENDING_EXPLOSIONS.add(new PendingExplosion(level, player, pos.immutable(), delayTicks));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || PENDING_EXPLOSIONS.isEmpty()) {
            return;
        }

        Iterator<PendingExplosion> iterator = PENDING_EXPLOSIONS.iterator();
        while (iterator.hasNext()) {
            PendingExplosion pending = iterator.next();
            if (pending.level != level) {
                continue;
            }

            pending.delayTicks--;
            if (pending.delayTicks <= 0) {
                pending.level.explode(
                        pending.player,
                        pending.pos.getX() + 0.5D,
                        pending.pos.getY() + 0.5D,
                        pending.pos.getZ() + 0.5D,
                        EXPLOSION_POWER,
                        Level.ExplosionInteraction.TNT
                );
                iterator.remove();
            }
        }
    }

    @Override
    protected double getRange() {
        return EXPLOSION_RANGE;
    }

    @Override
    protected int getBarkCooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    protected String getCaptionKey() {
        return "item.obesecat.explosive_paco.caption";
    }

    private static final class PendingExplosion {
        private final ServerLevel level;
        private final Player player;
        private final BlockPos pos;
        private int delayTicks;

        private PendingExplosion(ServerLevel level, Player player, BlockPos pos, int delayTicks) {
            this.level = level;
            this.player = player;
            this.pos = pos;
            this.delayTicks = delayTicks;
        }
    }
}
