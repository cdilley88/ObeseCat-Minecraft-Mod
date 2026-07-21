package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.inventory.TransmutationCubeMenu;
import com.fende.obesecat.inventory.EchoingBlastChamberMenu;
import com.fende.obesecat.inventory.CanOpenerMenu;
import com.fende.obesecat.inventory.CatChargerMenu;
import com.fende.obesecat.inventory.TargetDummyMenu;
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
    public static final DeferredHolder<MenuType<?>, MenuType<CanOpenerMenu>> CAN_OPENER = MENUS.register(
            "can_opener",
            () -> IMenuTypeExtension.create(CanOpenerMenu::fromNetwork)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<CatChargerMenu>> CAT_CHARGER = MENUS.register(
            "cat_charger",
            () -> IMenuTypeExtension.create(CatChargerMenu::fromNetwork)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<TargetDummyMenu>> TARGET_DUMMY = MENUS.register(
            "target_dummy", () -> IMenuTypeExtension.create(TargetDummyMenu::fromNetwork));

    private ModMenus() {
    }
}
