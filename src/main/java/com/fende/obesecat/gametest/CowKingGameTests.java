package com.fende.obesecat.gametest;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.entity.CowKing;
import com.fende.obesecat.world.CowKingFortSpawner;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ObeseCatMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CowKingGameTests {
    private static final String TEMPLATE = "village/plains/houses/cow_king_fort";

    @GameTest(template = TEMPLATE)
    public static void cowKingFortSpawnerTriggersOnApproachAndRespectsCooldown(GameTestHelper helper) {
        var level = helper.getLevel();
        var bounds = helper.getBounds();
        var player = helper.makeMockServerPlayerInLevel();
        player.moveTo(bounds.minX - 64.0D, bounds.minY + 1.0D, bounds.minZ - 64.0D, 0.0F, 0.0F);

        CowKingFortSpawner.registerFort(level, bounds);
        CowKingFortSpawner.tick(level);
        helper.assertValueEqual(
                level.getEntitiesOfClass(CowKing.class, bounds.inflate(48.0D), cow -> true).size(),
                0,
                "Cow King Fort should not spawn a boss until a player approaches"
        );

        var center = bounds.getCenter();
        player.moveTo(center.x + 8.0D, bounds.minY + 1.0D, center.z + 8.0D, 0.0F, 0.0F);
        CowKingFortSpawner.tick(level);
        helper.assertValueEqual(
                level.getEntitiesOfClass(CowKing.class, bounds.inflate(48.0D), cow -> true).size(),
                1,
                "Cow King Fort should spawn exactly one boss when a player approaches"
        );

        CowKing cowKing = level.getEntitiesOfClass(CowKing.class, bounds.inflate(48.0D), cow ->
                cow.getTags().contains(CowKingFortSpawner.STRUCTURE_ENTITY_TAG)).get(0);
        helper.assertTrue(
                cowKing.blockPosition().getX() < bounds.minX || cowKing.blockPosition().getX() > bounds.maxX
                        || cowKing.blockPosition().getZ() < bounds.minZ || cowKing.blockPosition().getZ() > bounds.maxZ,
                "Cow King should spawn just outside the fort footprint"
        );
        cowKing.discard();

        CowKingFortSpawner.tick(level);
        helper.assertValueEqual(
                level.getEntitiesOfClass(CowKing.class, bounds.inflate(48.0D), cow -> true).size(),
                0,
                "Cow King Fort should not immediately respawn after the boss is removed"
        );

        helper.runAfterDelay(CowKingFortSpawner.COOLDOWN_TICKS + 1L, () -> {
            CowKingFortSpawner.tick(level);
            helper.assertValueEqual(
                    level.getEntitiesOfClass(CowKing.class, bounds.inflate(48.0D), cow -> true).size(),
                    1,
                    "Cow King Fort should respawn after the cooldown expires"
            );
            helper.succeed();
        });
    }

    private CowKingGameTests() {
    }
}
