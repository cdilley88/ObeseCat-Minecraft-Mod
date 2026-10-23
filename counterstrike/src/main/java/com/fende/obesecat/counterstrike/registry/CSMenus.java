package com.fende.obesecat.counterstrike.registry;

import com.fende.obesecat.counterstrike.ObeseCatCounterstrike;
import com.fende.obesecat.counterstrike.menu.ConstructionYardMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CSMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ObeseCatCounterstrike.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ConstructionYardMenu>> CONSTRUCTION_YARD =
            MENUS.register("construction_yard", () -> IMenuTypeExtension.create(ConstructionYardMenu::fromNetwork));

    public static final DeferredHolder<MenuType<?>, MenuType<com.fende.obesecat.counterstrike.menu.FissionTurbineMenu>> FISSION_TURBINE =
            MENUS.register("fission_turbine", () -> IMenuTypeExtension.create(com.fende.obesecat.counterstrike.menu.FissionTurbineMenu::fromNetwork));

    public static final DeferredHolder<MenuType<?>, MenuType<com.fende.obesecat.counterstrike.menu.BarracksMenu>> BARRACKS =
            MENUS.register("barracks", () -> IMenuTypeExtension.create(com.fende.obesecat.counterstrike.menu.BarracksMenu::fromNetwork));

    private CSMenus() {
    }
}
