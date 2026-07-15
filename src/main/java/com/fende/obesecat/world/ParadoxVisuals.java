package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModItems;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Client-rendered display illusions: no real blocks are changed. */
final class ParadoxVisuals {
    private ParadoxVisuals() {}

    static Effect spawn(ServerLevel level, BlockPos target) {
        Vec3 center = Vec3.atCenterOf(target);
        List<WarpLayer> layers = new ArrayList<>();
        int[] channelColors = {0x00FFFF, 0xFF2020, 0x35FF58};
        for (int i = 0; i < 3; i++) {
            Quaternionf rotation = new Quaternionf().rotateZ((i - 1) * 0.025F);
            Vec3 anchor = center.add((i - 1) * 0.13D, 5.0D + (i == 1 ? 0.07D : 0.0D), (i - 1) * 0.035D);
            layers.add(new WarpLayer(spawnImage(level, anchor, 7.5F, 7.5F, rotation, channelColors[i]), anchor, i));
        }

        List<TerrainEcho> terrain = new ArrayList<>();
        RandomSource random = RandomSource.create(level.getGameTime() ^ target.asLong());
        for (int attempt = 0; attempt < 34; attempt++) {
            int x = random.nextInt(21) - 10;
            int z = random.nextInt(21) - 10;
            if (x * x + z * z > 115) continue;
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.offset(x, 0, z)).below();
            BlockState state = level.getBlockState(surface);
            if (state.isAir()) continue;
            Vec3 anchor = Vec3.atLowerCornerOf(surface);
            terrain.add(new TerrainEcho(spawnBlock(level, anchor, state), anchor, random.nextLong()));
        }
        return new Effect(layers, terrain);
    }

    private static Display.ItemDisplay spawnImage(ServerLevel level, Vec3 pos, float width, float height, Quaternionf rotation, int glowColor) {
        ItemStack stack = new ItemStack(ModItems.PARADOX_GFX.get());
        Tag transform = Transformation.CODEC.encodeStart(NbtOps.INSTANCE,
                new Transformation(new Vector3f(), rotation, new Vector3f(width, height, 1.0F), new Quaternionf()))
                .result().orElse(null);
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level) {{
            CompoundTag cfg = new CompoundTag();
            cfg.put("item", stack.save(registryAccess()));
            cfg.putString("billboard", "center");
            cfg.putFloat("view_range", 64.0F);
            cfg.putBoolean("Glowing", true);
            cfg.putInt("glow_color_override", glowColor);
            if (transform != null) cfg.put("transformation", transform);
            readAdditionalSaveData(cfg);
        }};
        display.setPos(pos); level.addFreshEntity(display); return display;
    }

    private static Display.BlockDisplay spawnBlock(ServerLevel level, Vec3 pos, BlockState state) {
        Display.BlockDisplay display = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level) {{
            CompoundTag cfg = new CompoundTag();
            cfg.put("block_state", NbtUtils.writeBlockState(state));
            cfg.putFloat("view_range", 48.0F);
            readAdditionalSaveData(cfg);
        }};
        display.setPos(pos); level.addFreshEntity(display); return display;
    }

    static final class Effect {
        private final List<WarpLayer> layers;
        private final List<TerrainEcho> terrain;
        Effect(List<WarpLayer> layers, List<TerrainEcho> terrain) { this.layers = layers; this.terrain = terrain; }

        void tick(int age) {
            for (WarpLayer layer : layers) {
                double hardTear = ((age + layer.index * 5) % 13 < 2) ? 1.45D : 0.0D;
                double scan = Math.sin(age * 0.61D + layer.index * 1.7D) * 0.16D;
                layer.display.setPos(layer.anchor.x + scan + hardTear * (layer.index % 2 == 0 ? 1 : -1),
                        layer.anchor.y + Math.cos(age * 0.43D + layer.index) * 0.12D,
                        layer.anchor.z + Math.sin(age * 0.29D + layer.index) * 0.22D);
            }
            for (int i = 0; i < terrain.size(); i++) {
                TerrainEcho echo = terrain.get(i);
                double seedA = (echo.chaos & 0xFFFFL) * 0.00031D;
                double seedB = ((echo.chaos >>> 16) & 0xFFFFL) * 0.00047D;
                double wave = Math.sin(age * (0.19D + seedA % 0.31D) + seedB * 9.0D);
                boolean rupturing = Math.floorMod(age + (int) echo.chaos, 11 + (int) (seedA * 10.0D) % 13) < 2;
                double rupture = rupturing ? 1.2D + (seedB % 2.8D) : 0.0D;
                double sidewaysX = Math.cos(age * (0.13D + seedB % 0.29D) + seedA) * (0.18D + rupture * 0.55D);
                double sidewaysZ = Math.sin(age * (0.17D + seedA % 0.23D) + seedB) * (0.22D + rupture * 0.42D);
                echo.display.setPos(echo.anchor.x + sidewaysX,
                        echo.anchor.y + 0.05D + Math.max(0.0D, wave) * (1.1D + seedA % 2.6D) + rupture,
                        echo.anchor.z + sidewaysZ);
            }
        }

        void discard() {
            layers.forEach(layer -> layer.display.discard());
            terrain.forEach(echo -> echo.display.discard());
        }
    }

    private record WarpLayer(Display.ItemDisplay display, Vec3 anchor, int index) {}
    private record TerrainEcho(Display.BlockDisplay display, Vec3 anchor, long chaos) {}
}
