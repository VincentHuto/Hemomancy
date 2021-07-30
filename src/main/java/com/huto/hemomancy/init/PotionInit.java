package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.effect.BloodBindingEffect;
import com.huto.hemomancy.effect.BloodLossEffect;
import com.huto.hemomancy.effect.BloodRushEffect;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionInit {
	public static final DeferredRegister<Potion> POTION_TYPES = DeferredRegister.create(ForgeRegistries.POTION_TYPES,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS,
			Hemomancy.MOD_ID);

	public static final RegistryObject<MobEffect> blood_binding = EFFECTS.register("blood_binding",
			() -> new BloodBindingEffect(MobEffectCategory.HARMFUL, 3735555));
	public static final RegistryObject<Potion> potion_of_blood_binding = POTION_TYPES.register(
			"potion_of_blood_binding",
			() -> new Potion("potion_of_blood_binding", new MobEffectInstance(blood_binding.get(), 1000, 3)));

	public static final RegistryObject<MobEffect> blood_loss = EFFECTS.register("blood_loss",
			() -> new BloodLossEffect(MobEffectCategory.HARMFUL, 11075587).addAttributesModifier(Attributes.MOVEMENT_SPEED,
					"7107DE5E-7CE8-4030-940E-514C1F160890", (double) -0.15F,
					AttributeModifier.Operation.MULTIPLY_TOTAL));
	public static final RegistryObject<Potion> potion_of_blood_loss = POTION_TYPES.register("potion_of_blood_loss",
			() -> new Potion("potion_of_blood_loss", new EffectInstance(blood_loss.get(), 1000, 3)));

	public static final RegistryObject<Effect> blood_rush = EFFECTS.register("blood_rush",
			() -> new BloodRushEffect(EffectType.BENEFICIAL, 16711680)
					.addAttributesModifier(Attributes.MOVEMENT_SPEED, "91AEAA56-376B-4498-935B-2F7F68070635",
							(double) 0.2F, AttributeModifier.Operation.MULTIPLY_TOTAL)
					.addAttributesModifier(Attributes.ATTACK_SPEED, "AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3",
							(double) 0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL)
					.addAttributesModifier(Attributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 0.0D,
							AttributeModifier.Operation.ADDITION));
	public static final RegistryObject<Potion> potion_of_blood_rush = POTION_TYPES.register("potion_of_blood_rush",
			() -> new Potion("potion_of_blood_rush", new EffectInstance(blood_rush.get(), 1000, 3)));

}
