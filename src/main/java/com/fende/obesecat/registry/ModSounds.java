package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, ObeseCatMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> PACO_BARK_1 = register("item.paco.bark1");
    public static final DeferredHolder<SoundEvent, SoundEvent> PACO_BARK_2 = register("item.paco.bark2");
    public static final DeferredHolder<SoundEvent, SoundEvent> PACO_BARK_3 = register("item.paco.bark3");
    public static final DeferredHolder<SoundEvent, SoundEvent> PACO_HELLBARK_1 = register("item.paco.hellbark1");
    public static final DeferredHolder<SoundEvent, SoundEvent> PACO_HELLBARK_2 = register("item.paco.hellbark2");
    public static final DeferredHolder<SoundEvent, SoundEvent> PACO_HELLBARK_3 = register("item.paco.hellbark3");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, name)
        ));
    }

    private ModSounds() {
    }
}
