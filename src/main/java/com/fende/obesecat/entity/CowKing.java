package com.fende.obesecat.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;

public class CowKing extends Cow {
    public CowKing(EntityType<? extends Cow> entityType, Level level) {
        super(entityType, level);
    }
}
