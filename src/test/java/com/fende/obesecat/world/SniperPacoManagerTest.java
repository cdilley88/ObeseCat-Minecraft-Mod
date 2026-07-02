package com.fende.obesecat.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class SniperPacoManagerTest {
    @Test
    void sniperPacoDealsEightDamagePerShot() throws ReflectiveOperationException {
        Field damageField = SniperPacoManager.class.getDeclaredField("SNIPER_DAMAGE");
        damageField.setAccessible(true);

        assertEquals(8.0F, damageField.getFloat(null));
    }
}
