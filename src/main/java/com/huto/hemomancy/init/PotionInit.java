package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.effects.EffectTest;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionInit {
	public static final DeferredRegister<Potion> POTION_TYPES = DeferredRegister
			.create(ForgeRegistries.POTION_TYPES, Hemomancy.MOD_ID);
	public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, Hemomancy.MOD_ID);

	public static final RegistryObject<Effect> blood_binding = EFFECTS.register("blood_binding", () -> new EffectTest(EffectType.HARMFUL, 3735555));
	
	public static final RegistryObject<Potion> test_potion = POTION_TYPES.register("test_potion",
			() -> new Potion("test_potion", new EffectInstance(blood_binding.get(),1000,3)));
	
	}
