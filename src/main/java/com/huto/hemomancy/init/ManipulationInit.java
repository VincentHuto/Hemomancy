package com.huto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;
import com.huto.hemomancy.manipulation.EnumManipulationType;
import com.huto.hemomancy.manipulation.continuous.ManipActivationPotential;
import com.huto.hemomancy.manipulation.continuous.ManipBloodAneurysm;
import com.huto.hemomancy.manipulation.continuous.ManipSanguineWard;
import com.huto.hemomancy.manipulation.quick.ManipBloodCloud;
import com.huto.hemomancy.manipulation.quick.ManipBloodNeedle;
import com.huto.hemomancy.manipulation.quick.ManipBloodRush;
import com.huto.hemomancy.manipulation.quick.ManipBloodShot;
import com.huto.hemomancy.manipulation.quick.ManipFerricTransmutation;
import com.huto.hemomancy.manipulation.quick.conjure.ManipBloodAbsorbtion;
import com.huto.hemomancy.manipulation.quick.conjure.ManipConjureAxe;
import com.huto.hemomancy.manipulation.quick.conjure.ManipConjureBlade;
import com.huto.hemomancy.manipulation.quick.conjure.ManipConjureClaws;
import com.huto.hemomancy.manipulation.quick.conjure.ManipConjureCrossbow;
import com.huto.hemomancy.manipulation.quick.conjure.ManipConjureSpear;

public class ManipulationInit {

	public static List<BloodManipulation> MANIPULATIONS = new ArrayList<BloodManipulation>();
	public static BloodManipulation blood_shot, blood_needle, blood_cloud, blood_rush, aneurysm;
	public static BloodManipulation ferric_transmutation, conjure_blade, conjure_axe, conjure_crossbow, conjure_claw,
			conjure_spear, conjure_blood_absorbtion, sanguine_ward;
	public static BloodManipulation activation_potential;

	// This will be an example for when I eventually switch this over to a deferred
	// register
//
//	public static final DeferredRegister<BloodManipulation> MANIPULATIONS = DeferredRegister.create(BloodManipulation.class,
//			Hemomancy.MOD_ID);
//	public static Supplier<IForgeRegistry<BloodManipulation>> MANIPS_TYPE_REGISTRY = MANIPULATIONS.makeRegistry(
//			"manipulation_types",
//			() -> new RegistryBuilder<BloodManipulation>().setMaxID(Integer.MAX_VALUE - 1)
//					.onAdd((owner, stage, id, obj, old) -> Hemomancy.LOGGER
//							.info("Manipulation Type Added: " + obj.getRegistryName().toString() + " "))
//					.setDefaultKey(new ResourceLocation(Hemomancy.MOD_ID, "null")));
//
//	public static final RegistryObject<BloodManipulation> NULL_EXPLOSION = MANIPULATIONS.register("test_manip",
//			() -> new BloodManipulation("test_manip", 100, 0, EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS,
//					EnumVeinSections.HEAD));

	public static void init() {
		blood_shot = register(new ManipBloodShot("blood_shot", 100, 0, EnumManipulationType.QUICK,
				EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD));
		blood_needle = register(new ManipBloodNeedle("blood_needle", 100, 0, EnumManipulationType.QUICK,
				EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD));
		blood_cloud = register(new ManipBloodCloud("blood_cloud", 100, 0, EnumManipulationType.QUICK,
				EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD));
		blood_rush = register(new ManipBloodRush("blood_rush", 100, 0, EnumManipulationType.PASSIVE,
				EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY));
		aneurysm = register(new ManipBloodAneurysm("aneurysm", 100, 0, EnumManipulationType.QUICK,
				EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY));
		ferric_transmutation = register(
				new ManipFerricTransmutation("ferric_transmutation", 1000, 0, EnumManipulationType.QUICK,
						EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.BODY));
		conjure_blade = register(new ManipConjureBlade("conjure_blade", 1000, 0, EnumManipulationRank.MEDIOCRITAS,
				EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM));
		conjure_axe = register(new ManipConjureAxe("conjure_axe", 1000, 0, EnumManipulationRank.MEDIOCRITAS,
				EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM));
		conjure_spear = register(new ManipConjureSpear("conjure_spear", 1000, 0, EnumManipulationRank.MEDIOCRITAS,
				EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM));
		conjure_crossbow = register(new ManipConjureCrossbow("conjure_crossbow", 1000, 0,
				EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM));
		conjure_claw = register(new ManipConjureClaws("conjure_claw", 1000, 0, EnumManipulationRank.MEDIOCRITAS,
				EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM));
		conjure_blood_absorbtion = register(new ManipBloodAbsorbtion("conjure_blood_absorbtion", 1000, 0,
				EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM));
		activation_potential = register(
				new ManipActivationPotential("activation_potential", 100, 0, EnumManipulationType.CONTINUOUS,
						EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY));
		sanguine_ward = register(new ManipSanguineWard("sanguine_ward", 5, 0, EnumManipulationType.CONTINUOUS,
				EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY));
	}

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
