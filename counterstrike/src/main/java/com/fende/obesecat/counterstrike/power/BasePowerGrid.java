package com.fende.obesecat.counterstrike.power;

import java.util.UUID;

/**
 * Tracks the total power generation and consumption for a player/team base power grid.
 */
public class BasePowerGrid {
    private final UUID ownerId;
    private int generated = 0;
    private int consumed = 0;

    public BasePowerGrid(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public int getGenerated() {
        return generated;
    }

    public int getConsumed() {
        return consumed;
    }

    public void setGenerated(int generated) {
        this.generated = Math.max(0, generated);
    }

    public void setConsumed(int consumed) {
        this.consumed = Math.max(0, consumed);
    }

    public void addGenerated(int amount) {
        this.generated += amount;
    }

    public void removeGenerated(int amount) {
        this.generated = Math.max(0, this.generated - amount);
    }

    public void addConsumed(int amount) {
        this.consumed += amount;
    }

    public void removeConsumed(int amount) {
        this.consumed = Math.max(0, this.consumed - amount);
    }

    /**
     * In C&C, if consumption exceeds generation, base enters Low Power mode.
     */
    public boolean isLowPower() {
        return consumed > generated;
    }

    public float getEfficiency() {
        if (consumed == 0) return 1.0f;
        if (generated == 0) return 0.0f;
        if (generated >= consumed) return 1.0f;
        return (float) generated / (float) consumed;
    }
}
