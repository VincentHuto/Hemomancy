package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundInit {
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,
			Hemomancy.MOD_ID);

	private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
		return SOUNDS.register(name, () -> new SoundEvent(Hemomancy.rloc(name)));
	}

	public static final RegistryObject<SoundEvent> ENTITY_ABHORENT_THOUGHT_AMBIENT = registerSoundEvent(
			"entity.abhorent_thought.ambient");
	public static final RegistryObject<SoundEvent> ENTITY_ABHORENT_THOUGHT_HURT = registerSoundEvent(
			"entity.abhorent_thought.hurt");
	public static final RegistryObject<SoundEvent> ENTITY_ABHORENT_THOUGHT_DEATH = registerSoundEvent(
			"entity.abhorent_thought.death");

}