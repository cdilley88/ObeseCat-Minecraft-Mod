package com.fende.obesecat.gametest;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.registry.ModItems;
import com.fende.obesecat.world.SniperPacoManager;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ObeseCatMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SniperPacoGameTests {
    private static final String TEMPLATE = "village/plains/houses/manhattan_bunker";
    private static final float SNIPER_DAMAGE = 8.0F;

    @GameTest(template = TEMPLATE)
    public static void sniperPacoZoomsAndFiresImmediately(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockServerPlayerInLevel();
        player.moveTo(1.5D, 2.0D, 1.5D, 0.0F, 0.0F);

        Mob target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 7, 2, 1);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());

        ItemStack sniperStack = new ItemStack(ModItems.SNIPER_PACO.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, sniperStack);

        helper.assertValueEqual(
                UseAnim.SPYGLASS,
                sniperStack.getItem().getUseAnimation(sniperStack),
                "Sniper Paco should use the spyglass animation"
        );

        sniperStack.getItem().use(level, player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.isUsingItem(), "Sniper Paco should start zooming on right click");
        helper.assertTrue(player.isScoping(), "Sniper Paco should trigger the real spyglass zoom state");

        AttackEntityEvent attackEvent = new AttackEntityEvent(player, target);
        NeoForge.EVENT_BUS.post(attackEvent);
        helper.assertTrue(attackEvent.isCanceled(), "Sniper Paco should cancel the normal melee attack while zoomed");
        SniperPacoManager.queueShot(player);
        helper.assertValueEqual(
                target.getMaxHealth() - SNIPER_DAMAGE,
                target.getHealth(),
                "The zoomed shot should land immediately after the left click"
        );
        helper.succeed();
    }

    private SniperPacoGameTests() {
    }
}
