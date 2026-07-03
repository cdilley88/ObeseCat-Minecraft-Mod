package com.fende.obesecat.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class CowKing extends Monster {
    public CowKing(EntityType<? extends CowKing> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
    }
}
