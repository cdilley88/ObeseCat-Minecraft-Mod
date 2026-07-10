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
    public static final DeferredHolder<SoundEvent, SoundEvent> DOMINO_MEOW_1 = register("item.domino.meow1");
    public static final DeferredHolder<SoundEvent, SoundEvent> DOMINO_MEOW_2 = register("item.domino.meow2");
    public static final DeferredHolder<SoundEvent, SoundEvent> DOMINO_MEOW_3 = register("item.domino.meow3");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRANSMUTE = register("item.transmute");
    public static final DeferredHolder<SoundEvent, SoundEvent> STASIS_ICE = register("item.stasisice");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPLIT_PUNCH = register("item.splitpunch");
    public static final DeferredHolder<SoundEvent, SoundEvent> SWORD_SKILL_CAST = register("item.swordskillcast");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRUSH_PUNCH_BACK = register("item.crushpunchback");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRUSH_PUNCH_BLADE = register("item.crushpunchblade");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTNING_STAB_FLAME = register("item.lightningstabflame");
    public static final DeferredHolder<SoundEvent, SoundEvent> HOLY_EXPLOSION = register("item.holyexplosion");
    public static final DeferredHolder<SoundEvent, SoundEvent> TOILET_STINK_1 = register("block.toilet.stink1");
    public static final DeferredHolder<SoundEvent, SoundEvent> TOILET_STINK_2 = register("block.toilet.stink2");
    public static final DeferredHolder<SoundEvent, SoundEvent> TOILET_STINK_3 = register("block.toilet.stink3");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, name)
        ));
    }

    private ModSounds() {
    }
}
