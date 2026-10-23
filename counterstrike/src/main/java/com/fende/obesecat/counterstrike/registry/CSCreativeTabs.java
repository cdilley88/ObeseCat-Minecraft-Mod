package com.fende.obesecat.counterstrike.registry;

import com.fende.obesecat.counterstrike.ObeseCatCounterstrike;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class CSCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ObeseCatCounterstrike.MOD_ID);

    public static final Supplier<CreativeModeTab> COUNTERSTRIKE_TAB = CREATIVE_MODE_TABS.register(
            "counterstrike_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.obesecat_counterstrike"))
                    .icon(() -> new ItemStack(CSItems.COMMAND_CURSOR.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(CSItems.COMMAND_CURSOR.get());
                        output.accept(CSItems.MEOWBILE_CONSTRUCTION_VEHICLE.get());
                        output.accept(CSItems.CONSTRUCTION_YARD_CORE.get());
                        output.accept(CSItems.CONSTRUCTION_YARD_HULL.get());
                        output.accept(CSItems.PNEUMATIC_DOOR.get());
                        output.accept(CSItems.FISSION_TURBINE_CRATE.get());
                        output.accept(CSItems.FISSION_TURBINE_CORE.get());
                        output.accept(CSItems.TURBINE_REACTOR_CORE.get());
                        output.accept(CSItems.BARRACKS_CRATE.get());
                        output.accept(CSItems.BARRACKS_CORE.get());
                        output.accept(CSItems.ARMORY_LOCKER.get());
                        output.accept(CSItems.AUTOMATIC_RIFLE.get());
                        output.accept(CSItems.ROCKET_LAUNCHER.get());
                        output.accept(CSItems.FRAG_GRENADE.get());
                        output.accept(CSItems.FLAMETHROWER.get());
                        output.accept(CSItems.COMMANDO_RIFLE.get());
                    })
                    .build()
    );

    private CSCreativeTabs() {
    }
}
