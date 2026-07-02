package com.fende.obesecat.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SniperPacoInputHandlerTest {
    @Test
    void sniperPacoOnlyInterceptsPrimaryButtonPressesWhileScoped() {
        assertTrue(SniperPacoInputHandler.shouldInterceptMouseButton(0, 1, true));
        assertFalse(SniperPacoInputHandler.shouldInterceptMouseButton(0, 0, true));
        assertFalse(SniperPacoInputHandler.shouldInterceptMouseButton(1, 1, true));
        assertFalse(SniperPacoInputHandler.shouldInterceptMouseButton(0, 1, false));
    }
}
