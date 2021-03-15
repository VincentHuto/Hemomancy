package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.effects.BloodBindingEffect;
import com.huto.hemomancy.effects.BloodLossEffect;

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

	public static final RegistryObject<Effect> blood_binding = EFFECTS.register("blood_binding", () -> new BloodBindingEffect(EffectType.HARMFUL, 3735555));
	public static final RegistryObject<Potion> potion_of_blood_binding = POTION_TYPES.register("potion_of_blood_binding",
			() -> new Potion("potion_of_blood_binding", new EffectInstance(blood_binding.get(),1000,3)));
	
	public static final RegistryObject<Effect> blood_loss = EFFECTS.register("blood_loss", () -> new BloodLossEffect(EffectType.HARMFUL, 11075587));
	public static final RegistryObject<Potion> potion_of_blood_loss = POTION_TYPES.register("potion_of_blood_loss",
			() -> new Potion("potion_of_blood_loss", new EffectInstance(blood_loss.get(),1000,3)));
	
	}
