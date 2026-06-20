package com.fende.obesecat.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.joml.Vector3f;

public class NuclearLibraryBlock extends Block {
    public static final MapCodec<NuclearLibraryBlock> CODEC = simpleCodec(NuclearLibraryBlock::new);
    private static final Vector3f LIME_GREEN = new Vector3f(0.56F, 1.0F, 0.12F);
    private static final Vector3f NEON_YELLOW = new Vector3f(0.96F, 1.0F, 0.18F);

    public NuclearLibraryBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public NuclearLibraryBlock() {
        this(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                .mapColor(MapColor.WOOD)
                .instrument(NoteBlockInstrument.BASS)
                .sound(SoundType.WOOD)
                .lightLevel(state -> 15));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) {
            return;
        }

        double x = pos.getX() + 0.2D + random.nextDouble() * 0.6D;
        double y = pos.getY() + 1.02D;
        double z = pos.getZ() + 0.2D + random.nextDouble() * 0.6D;
        double xSpeed = (random.nextDouble() - 0.5D) * 0.02D;
        double ySpeed = 0.02D + random.nextDouble() * 0.02D;
        double zSpeed = (random.nextDouble() - 0.5D) * 0.02D;
        Vector3f color = random.nextBoolean() ? LIME_GREEN : NEON_YELLOW;

        level.addParticle(new DustParticleOptions(color, 1.1F), x, y, z, xSpeed, ySpeed, zSpeed);
    }
}
