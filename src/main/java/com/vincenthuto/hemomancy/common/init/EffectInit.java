package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.effect.ArachnidAnastomosisEffect;
import com.vincenthuto.hemomancy.common.effect.BloodBindingEffect;
import com.vincenthuto.hemomancy.common.effect.BloodLossEffect;
import com.vincenthuto.hemomancy.common.effect.BloodRushEffect;
import com.vincenthuto.hemomancy.common.effect.ChitinousBulwarkEffect;
import com.vincenthuto.hemomancy.common.effect.EchoicPerceptionEffect;
import com.vincenthuto.hemomancy.common.effect.ElytraEffect;
import com.vincenthuto.hemomancy.common.effect.HemolysisEffect;
import com.vincenthuto.hemomancy.common.effect.LuminousDissipationEffect;
import com.vincenthuto.hemomancy.common.effect.MycorrhizalMendingEffect;
import com.vincenthuto.hemomancy.common.effect.BurrowersInstinctEffect;
import com.vincenthuto.hemomancy.common.effect.HemorrhagicVenomEffect;
import com.vincenthuto.hemomancy.common.effect.SanguineFertilityEffect;
import com.vincenthuto.hemomancy.common.effect.SanguineSiphonEffect;
import com.vincenthuto.hemomancy.common.effect.SerpentineGuileEffect;
import com.vincenthuto.hemomancy.common.effect.SilverWardEffect;
import com.vincenthuto.hemomancy.common.effect.SpinedBarricadeEffect;
import com.vincenthuto.hemomancy.common.effect.VenomousResilienceEffect;
import com.vincenthuto.hemomancy.common.effect.VerminousAuraEffect;
import com.vincenthuto.hemomancy.common.effect.ArcaneResonanceEffect;
import com.vincenthuto.hemomancy.common.effect.SanguineClarityEffect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EffectInit {
	public static final DeferredRegister<Potion> POTION_TYPES = DeferredRegister.create(ForgeRegistries.POTIONS,
			Hemomancy.MOD_ID);
	public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,
			Hemomancy.MOD_ID);

	public static final RegistryObject<MobEffect> fungal_elytra = EFFECTS.register("fungal_elytra", ElytraEffect::new);

	public static final RegistryObject<MobEffect> blood_binding = EFFECTS.register("blood_binding",
			() -> new BloodBindingEffect(MobEffectCategory.HARMFUL, 3735555));
	public static final RegistryObject<Potion> potion_of_blood_binding = POTION_TYPES.register(
			"potion_of_blood_binding",
			() -> new Potion("potion_of_blood_binding", new MobEffectInstance(blood_binding.get(), 1000, 3)));

	public static final RegistryObject<MobEffect> blood_loss = EFFECTS.register("blood_loss",
			() -> new BloodLossEffect(MobEffectCategory.HARMFUL, 11075587).addAttributeModifier(
					Attributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -0.15F,
					AttributeModifier.Operation.MULTIPLY_TOTAL));
	public static final RegistryObject<Potion> potion_of_blood_loss = POTION_TYPES.register("potion_of_blood_loss",
			() -> new Potion("potion_of_blood_loss", new MobEffectInstance(blood_loss.get(), 1000, 3)));

	public static final RegistryObject<MobEffect> blood_rush = EFFECTS.register("blood_rush",
			() -> new BloodRushEffect(MobEffectCategory.BENEFICIAL, 16711680)
					.addAttributeModifier(Attributes.MOVEMENT_SPEED, "91AEAA56-376B-4498-935B-2F7F68070635", 0.2F,
							AttributeModifier.Operation.MULTIPLY_TOTAL)
					.addAttributeModifier(Attributes.ATTACK_SPEED, "AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3", 0.1F,
							AttributeModifier.Operation.MULTIPLY_TOTAL)
					.addAttributeModifier(Attributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 0.0D,
							AttributeModifier.Operation.ADDITION));
	public static final RegistryObject<Potion> potion_of_blood_rush = POTION_TYPES.register("potion_of_blood_rush",
			() -> new Potion("potion_of_blood_rush", new MobEffectInstance(blood_rush.get(), 1000, 3)));

	public static final RegistryObject<MobEffect> hemolysis = EFFECTS.register("hemolysis",
			() -> new HemolysisEffect(MobEffectCategory.NEUTRAL, 15186121));

	public static final RegistryObject<Potion> potion_of_hemolysis = POTION_TYPES.register("potion_of_hemolysis",
			() -> new Potion("potion_of_hemolysis", new MobEffectInstance(hemolysis.get(), 1000, 3)));

	public static final RegistryObject<MobEffect> sanguine_fertility = EFFECTS.register("sanguine_fertility",
			() -> new SanguineFertilityEffect(MobEffectCategory.BENEFICIAL, 0xCC3344));
	public static final RegistryObject<Potion> potion_of_sanguine_fertility = POTION_TYPES.register(
			"potion_of_sanguine_fertility",
			() -> new Potion("potion_of_sanguine_fertility",
					new MobEffectInstance(sanguine_fertility.get(), 1200, 0)));

	public static final RegistryObject<MobEffect> arachnid_anastomosis = EFFECTS.register("arachnid_anastomosis",
			() -> new ArachnidAnastomosisEffect(MobEffectCategory.BENEFICIAL, 0x8B0000));
	public static final RegistryObject<Potion> potion_of_arachnid_anastomosis = POTION_TYPES.register(
			"potion_of_arachnid_anastomosis",
			() -> new Potion("potion_of_arachnid_anastomosis",
					new MobEffectInstance(arachnid_anastomosis.get(), 1200, 0)));

	public static final RegistryObject<MobEffect> mycorrhizal_mending = EFFECTS.register("mycorrhizal_mending",
			() -> new MycorrhizalMendingEffect(MobEffectCategory.BENEFICIAL, 0x7B4F2A));
	public static final RegistryObject<Potion> potion_of_mycorrhizal_mending = POTION_TYPES.register(
			"potion_of_mycorrhizal_mending",
			() -> new Potion("potion_of_mycorrhizal_mending",
					new MobEffectInstance(mycorrhizal_mending.get(), 1200, 0)));

	public static final RegistryObject<MobEffect> sanguine_siphon = EFFECTS.register("sanguine_siphon",
			() -> new SanguineSiphonEffect(MobEffectCategory.BENEFICIAL, 0x8B0000));
	public static final RegistryObject<Potion> potion_of_sanguine_siphon = POTION_TYPES.register(
			"potion_of_sanguine_siphon",
			() -> new Potion("potion_of_sanguine_siphon",
					new MobEffectInstance(sanguine_siphon.get(), 1200, 0)));

	public static final RegistryObject<MobEffect> chitinous_bulwark = EFFECTS.register("chitinous_bulwark",
			() -> new ChitinousBulwarkEffect(MobEffectCategory.BENEFICIAL, 0x556B2F)
					.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, "A1C3E5F7-9B0D-4E2F-8A6C-1D3F5B7E9A0C",
							4.0D, AttributeModifier.Operation.ADDITION));
	public static final RegistryObject<Potion> potion_of_chitinous_bulwark = POTION_TYPES.register(
			"potion_of_chitinous_bulwark",
			() -> new Potion("potion_of_chitinous_bulwark",
					new MobEffectInstance(chitinous_bulwark.get(), 1200, 0)));

	public static final RegistryObject<MobEffect> serpentine_guile = EFFECTS.register("serpentine_guile",
			() -> new SerpentineGuileEffect(MobEffectCategory.BENEFICIAL, 0x2E8B57)
					.addAttributeModifier(Attributes.MOVEMENT_SPEED, "B2D4F6A8-0C1E-3F5B-7D9A-2E4C6F8A0B1D",
							0.15F, AttributeModifier.Operation.MULTIPLY_TOTAL)
					.addAttributeModifier(Attributes.ATTACK_SPEED, "C3E5A7B9-1D0F-4A2C-6E8B-3F5D7A9C1E0B",
							0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL));
	public static final RegistryObject<Potion> potion_of_serpentine_guile = POTION_TYPES.register(
			"potion_of_serpentine_guile",
			() -> new Potion("potion_of_serpentine_guile",
					new MobEffectInstance(serpentine_guile.get(), 1200, 0)));

	public static final RegistryObject<MobEffect> verminous_aura = EFFECTS.register("verminous_aura",
			() -> new VerminousAuraEffect(MobEffectCategory.BENEFICIAL, 0x4A3728));
	public static final RegistryObject<Potion> potion_of_verminous_aura = POTION_TYPES.register(
			"potion_of_verminous_aura",
			() -> new Potion("potion_of_verminous_aura",
					new MobEffectInstance(verminous_aura.get(), 1200, 0)));

	// New Morphling Effects
	public static final RegistryObject<MobEffect> luminous_dissipation = EFFECTS.register("luminous_dissipation",
			() -> new LuminousDissipationEffect(MobEffectCategory.BENEFICIAL, 0xFFFFAA)
					.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "F1A2B3C4-D5E6-7F89-0A1B-2C3D4E5F6A7B",
							0.1D, AttributeModifier.Operation.ADDITION));
	public static final RegistryObject<Potion> potion_of_luminous_dissipation = POTION_TYPES.register(
			"potion_of_luminous_dissipation",
			() -> new Potion("potion_of_luminous_dissipation",
					new MobEffectInstance(luminous_dissipation.get(), 1200, 0)));

	public static final RegistryObject<MobEffect> hemorrhagic_venom = EFFECTS.register("hemorrhagic_venom",
			() -> new HemorrhagicVenomEffect(MobEffectCategory.BENEFICIAL, 0x660033));
	public static final RegistryObject<Potion> potion_of_hemorrhagic_venom = POTION_TYPES.register(
			"potion_of_hemorrhagic_venom",
			() -> new Potion("potion_of_hemorrhagic_venom",
					new MobEffectInstance(hemorrhagic_venom.get(), 1200, 0)));

	public static final RegistryObject<MobEffect> venomous_resilience = EFFECTS.register("venomous_resilience",
			() -> new VenomousResilienceEffect(MobEffectCategory.BENEFICIAL, 0x336B87)
					.addAttributeModifier(Attributes.MOVEMENT_SPEED, "A7B8C9D0-E1F2-3A4B-5C6D-7E8F9A0B1C2D",
							0.05F, AttributeModifier.Operation.MULTIPLY_TOTAL));
	public static final RegistryObject<Potion> potion_of_venomous_resilience = POTION_TYPES.register(
			"potion_of_venomous_resilience",
			() -> new Potion("potion_of_venomous_resilience",
					new MobEffectInstance(venomous_resilience.get(), 1200, 0)));

	public static final RegistryObject<MobEffect> echoic_perception = EFFECTS.register("echoic_perception",
			() -> new EchoicPerceptionEffect(MobEffectCategory.BENEFICIAL, 0x2A0A3C));
	public static final RegistryObject<Potion> potion_of_echoic_perception = POTION_TYPES.register(
			"potion_of_echoic_perception",
			() -> new Potion("potion_of_echoic_perception",
					new MobEffectInstance(echoic_perception.get(), 1200, 0)));

	public static final RegistryObject<MobEffect> spined_barricade = EFFECTS.register("spined_barricade",
			() -> new SpinedBarricadeEffect(MobEffectCategory.BENEFICIAL, 0x1A8A9F)
					.addAttributeModifier(Attributes.ARMOR, "C8D9E0F1-A2B3-4C5D-6E7F-8A9B0C1D2E3F",
							2.0D, AttributeModifier.Operation.ADDITION));
	public static final RegistryObject<Potion> potion_of_spined_barricade = POTION_TYPES.register(
			"potion_of_spined_barricade",
			() -> new Potion("potion_of_spined_barricade",
					new MobEffectInstance(spined_barricade.get(), 1200, 0)));

	public static final RegistryObject<MobEffect> burrowers_instinct = EFFECTS.register("burrowers_instinct",
			() -> new BurrowersInstinctEffect(MobEffectCategory.BENEFICIAL, 0x8B6914)
					.addAttributeModifier(Attributes.ATTACK_SPEED, "B8C9D0E1-F2A3-4B5C-6D7E-8F9A0B1C2D3E",
							0.15F, AttributeModifier.Operation.MULTIPLY_TOTAL));
	public static final RegistryObject<Potion> potion_of_burrowers_instinct = POTION_TYPES.register(
			"potion_of_burrowers_instinct",
			() -> new Potion("potion_of_burrowers_instinct",
					new MobEffectInstance(burrowers_instinct.get(), 1200, 0)));

	// Unstained Path Effects
	public static final RegistryObject<MobEffect> silver_ward = EFFECTS.register("silver_ward",
			() -> new SilverWardEffect(MobEffectCategory.BENEFICIAL, 0xC0C0C0)
					.addAttributeModifier(Attributes.ARMOR, "D4E6F8A0-2B1C-4D3E-9F5A-6C8B0E2D4A7F",
							4.0D, AttributeModifier.Operation.ADDITION)
					.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "E5F7A9B1-3C2D-5E4F-0A6B-7D9C1F3E5B8A",
							0.2D, AttributeModifier.Operation.ADDITION));

	// MnA Cross-Mod Combo Effects
	public static final RegistryObject<MobEffect> arcane_resonance = EFFECTS.register("arcane_resonance",
			() -> new ArcaneResonanceEffect(MobEffectCategory.BENEFICIAL, 0x8B0020));

	public static final RegistryObject<MobEffect> sanguine_clarity = EFFECTS.register("sanguine_clarity",
			() -> new SanguineClarityEffect(MobEffectCategory.BENEFICIAL, 0x4020A0));

}
