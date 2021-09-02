package com.vincenthuto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.capa.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.manipulation.continuous.ManipBloodAneurysm;
import com.vincenthuto.hemomancy.manipulation.continuous.ManipSanguineWard;
import com.vincenthuto.hemomancy.manipulation.quick.ManipActivationPotential;
import com.vincenthuto.hemomancy.manipulation.quick.ManipBloodCloud;
import com.vincenthuto.hemomancy.manipulation.quick.ManipBloodNeedle;
import com.vincenthuto.hemomancy.manipulation.quick.ManipBloodRush;
import com.vincenthuto.hemomancy.manipulation.quick.ManipBloodShot;
import com.vincenthuto.hemomancy.manipulation.quick.ManipConjuration;
import com.vincenthuto.hemomancy.manipulation.quick.ManipDeadlyGaze;
import com.vincenthuto.hemomancy.manipulation.quick.ManipFerricTransmutation;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public class ManipulationInit {

	public static List<BloodManipulation> MANIPULATIONS = new ArrayList<BloodManipulation>();

	public static final DeferredRegister<BloodManipulation> MANIPS = DeferredRegister.create(BloodManipulation.class,
			Hemomancy.MOD_ID);
	public static Supplier<IForgeRegistry<BloodManipulation>> MANIPS_TYPE_REGISTRY = MANIPS
			.makeRegistry("manipulation_types", () -> new RegistryBuilder<BloodManipulation>()
					.setMaxID(Integer.MAX_VALUE - 1).setDefaultKey(new ResourceLocation(Hemomancy.MOD_ID, "null")));

	public static final RegistryObject<BloodManipulation> blood_shot = MANIPS.register("blood_shot",
			() -> register(new ManipBloodShot("blood_shot", 100, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)));

	public static final RegistryObject<BloodManipulation> deadly_gaze = MANIPS.register("deadly_gaze",
			() -> register(new ManipDeadlyGaze("deadly_gaze", 100, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)));

	public static final RegistryObject<BloodManipulation> blood_needle = MANIPS.register("blood_needle",
			() -> register(new ManipBloodNeedle("blood_needle", 100, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)));

	public static final RegistryObject<BloodManipulation> blood_cloud = MANIPS.register("blood_cloud",
			() -> register(new ManipBloodCloud("blood_cloud", 100, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)));

	public static final RegistryObject<BloodManipulation> blood_rush = MANIPS.register("blood_rush",
			() -> register(new ManipBloodRush("blood_rush", 100, 0, EnumManipulationType.PASSIVE,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)));

	public static final RegistryObject<BloodManipulation> aneurysm = MANIPS.register("aneurysm",
			() -> register(new ManipBloodAneurysm("aneurysm", 100, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)));

	public static final RegistryObject<BloodManipulation> ferric_transmutation = MANIPS.register("ferric_transmutation",
			() -> register(new ManipFerricTransmutation("ferric_transmutation", 1000, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.BODY)));

	public static final RegistryObject<BloodManipulation> activation_potential = MANIPS.register("activation_potential",
			() -> register(new ManipActivationPotential("activation_potential", 100, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY)));

	public static final RegistryObject<BloodManipulation> sanguine_ward = MANIPS.register("sanguine_ward",
			() -> register(new ManipSanguineWard("sanguine_ward", 5, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY)));

	public static final RegistryObject<BloodManipulation> conjure_blade = MANIPS.register("conjure_blade",
			() -> register(new ManipConjuration("conjure_blade", ItemInit.living_blade, 1000, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM)));

	public static final RegistryObject<BloodManipulation> conjure_axe = MANIPS.register("conjure_axe",
			() -> register(new ManipConjuration("conjure_axe", ItemInit.living_axe, 1000, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM)));

	public static final RegistryObject<BloodManipulation> conjure_spear = MANIPS.register("conjure_spear",
			() -> register(new ManipConjuration("conjure_spear", ItemInit.living_spear, 1000, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM)));

	public static final RegistryObject<BloodManipulation> conjure_crossbow = MANIPS.register("conjure_crossbow",
			() -> register(new ManipConjuration("conjure_crossbow", ItemInit.living_crossbow, 1000, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM)));

	public static final RegistryObject<BloodManipulation> conjure_claws = MANIPS.register("conjure_claws",
			() -> register(new ManipConjuration("conjure_claw", ItemInit.living_baghnakh, 1000, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM)));

	public static final RegistryObject<BloodManipulation> conjure_blood_absorbtion = MANIPS.register(
			"conjure_blood_absorbtion",
			() -> register(new ManipConjuration("conjure_blood_absorbtion", ItemInit.blood_absorption, 1000, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM)));

	public static final RegistryObject<BloodManipulation> conjure_blood_projection = MANIPS.register(
			"conjure_blood_projection",
			() -> register(new ManipConjuration("conjure_blood_projection", ItemInit.blood_projection, 1000, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM)));

	public static BloodManipulation getByName(String name) {
		for (BloodManipulation manip : MANIPULATIONS) {
			if (name.equals(manip.getName())) {
				return manip;
			}
		}
		return null;
	}

	public static BloodManipulation register(BloodManipulation manip) {
		MANIPULATIONS.add(manip);
		return manip;
	}

}
