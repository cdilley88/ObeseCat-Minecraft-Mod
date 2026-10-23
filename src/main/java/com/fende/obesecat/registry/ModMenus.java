package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.inventory.EchoingBlastChamberMenu;
import com.fende.obesecat.inventory.TabbyriumRefineryMenu;
import com.fende.obesecat.inventory.TabbyriumSiloMenu;
import com.fende.obesecat.inventory.TransmutationCubeMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ObeseCatMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<TransmutationCubeMenu>> TRANSMUTATION_CUBE = MENUS.register(
            "transmutation_cube",
            () -> IMenuTypeExtension.create(TransmutationCubeMenu::fromNetwork)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<EchoingBlastChamberMenu>> ECHOING_BLAST_CHAMBER = MENUS.register(
            "echoing_blast_chamber",
            () -> IMenuTypeExtension.create(EchoingBlastChamberMenu::fromNetwork)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<TabbyriumRefineryMenu>> TABBYRIUM_REFINERY = MENUS.register(
            "tabbyrium_refinery",
            () -> IMenuTypeExtension.create(TabbyriumRefineryMenu::fromNetwork)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<TabbyriumRefineryMenu>> CAN_OPENER = TABBYRIUM_REFINERY;

    public static final DeferredHolder<MenuType<?>, MenuType<TabbyriumSiloMenu>> TABBYRIUM_SILO = MENUS.register(
            "tabbyrium_silo",
            () -> IMenuTypeExtension.create(TabbyriumSiloMenu::fromNetwork)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<TabbyriumSiloMenu>> CAT_CHARGER = TABBYRIUM_SILO;

    private ModMenus() {
    }
}
