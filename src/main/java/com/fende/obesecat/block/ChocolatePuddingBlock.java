package com.fende.obesecat.block;

import com.fende.obesecat.registry.ModSounds;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ChocolatePuddingBlock extends CakeBlock {
    public ChocolatePuddingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        SoundType cakeSounds = SoundType.WOOL;
        return new SoundType(
                cakeSounds.getVolume(),
                cakeSounds.getPitch(),
                ModSounds.CHOCOLATE_PUDDING_BREAK.get(),
                cakeSounds.getStepSound(),
                cakeSounds.getPlaceSound(),
                cakeSounds.getHitSound(),
                cakeSounds.getFallSound()
        );
    }
}