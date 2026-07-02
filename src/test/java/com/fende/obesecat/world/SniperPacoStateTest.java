package com.fende.obesecat.world;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class SniperPacoStateTest {
    @Test
    void sniperPacoReloadsAfterTenShots() {
        CompoundTag state = new CompoundTag();

        long gameTime = 0L;
        for (int shot = 0; shot < SniperPacoState.CLIP_SIZE; shot++) {
            int cooldownTicks = SniperPacoState.consumeShot(state, gameTime);
            assertTrue(cooldownTicks > 0, "Shot " + (shot + 1) + " should fire");
            if (shot < SniperPacoState.CLIP_SIZE - 1) {
                assertEquals(SniperPacoState.SHOT_DELAY_TICKS, cooldownTicks, "Non-final shots should use the short delay");
            } else {
                assertEquals(SniperPacoState.RELOAD_TICKS, cooldownTicks, "The final shot should trigger the long reload");
            }
            gameTime += SniperPacoState.SHOT_DELAY_TICKS;
        }

        assertFalse(
                SniperPacoState.consumeShot(state, gameTime) > 0,
                "The sniper should not fire again until the 10 second reload finishes"
        );

        assertFalse(
                SniperPacoState.consumeShot(state, gameTime + SniperPacoState.RELOAD_TICKS - SniperPacoState.SHOT_DELAY_TICKS - 1L) > 0,
                "The reload should still be in progress just before it completes"
        );

        assertTrue(
                SniperPacoState.consumeShot(state, gameTime + SniperPacoState.RELOAD_TICKS - SniperPacoState.SHOT_DELAY_TICKS) > 0,
                "The sniper should fire again once the reload delay expires"
        );
    }
}
