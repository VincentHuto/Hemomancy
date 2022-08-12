package com.vincenthuto.hemomancy.init;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.capa.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.manipulation.continuous.BloodAneurysmManip;
import com.vincenthuto.hemomancy.manipulation.continuous.SanguineWardManip;
import com.vincenthuto.hemomancy.manipulation.quick.ActivationPotentialManip;
import com.vincenthuto.hemomancy.manipulation.quick.BloodCloudManip;
import com.vincenthuto.hemomancy.manipulation.quick.BloodNeedleManip;
import com.vincenthuto.hemomancy.manipulation.quick.BloodRushManip;
import com.vincenthuto.hemomancy.manipulation.quick.BloodShotManip;
import com.vincenthuto.hemomancy.manipulation.quick.ConjurationManip;
import com.vincenthuto.hemomancy.manipulation.quick.DeadlyGazeManip;
import com.vincenthuto.hemomancy.manipulation.quick.FerricTransmutationManip;
import com.vincenthuto.hemomancy.manipulation.quick.SummonAvatarManip;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ManipulationInit {

	public static List<BloodManipulation> MANIPULATIONS = new ArrayList<BloodManipulation>();

	public static final DeferredRegister<BloodManipulation> MANIPS = DeferredRegister.create(BloodManipulation.class,
			Hemomancy.MOD_ID);
	public static Supplier<IForgeRegistry<BloodManipulation>> MANIPS_TYPE_REGISTRY = MANIPS
			.makeRegistry("manipulation_types", () -> new RegistryBuilder<BloodManipulation>()
					.setMaxID(Integer.MAX_VALUE - 1).setDefaultKey(new ResourceLocation(Hemomancy.MOD_ID, "null")));
	
	public static final RegistryObject<BloodManipulation> venous_recaller = MANIPS.register("venous_recaller",
			() -> register(new ConjurationManip("venous_recaller", ItemInit.venous_recaller, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM)));
	

	public static final RegistryObject<BloodManipulation> blood_shot = MANIPS.register("blood_shot",
			() -> register(new BloodShotManip("blood_shot", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)));

	public static final RegistryObject<BloodManipulation> deadly_gaze = MANIPS.register("deadly_gaze",
			() -> register(new DeadlyGazeManip("deadly_gaze", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)));
	
	public static final RegistryObject<BloodManipulation> summon_avatar = MANIPS.register("summon_avatar",
			() -> register(new SummonAvatarManip("summon_avatar", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)));

	public static final RegistryObject<BloodManipulation> blood_needle = MANIPS.register("blood_needle",
			() -> register(new BloodNeedleManip("blood_needle", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)));

	public static final RegistryObject<BloodManipulation> blood_cloud = MANIPS.register("blood_cloud",
			() -> register(new BloodCloudManip("blood_cloud", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)));

	public static final RegistryObject<BloodManipulation> blood_rush = MANIPS.register("blood_rush",
			() -> register(new BloodRushManip("blood_rush", 100, 0, 0, EnumManipulationType.PASSIVE,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)));

	public static final RegistryObject<BloodManipulation> aneurysm = MANIPS.register("aneurysm",
			() -> register(new BloodAneurysmManip("aneurysm", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)));

	public static final RegistryObject<BloodManipulation> ferric_transmutation = MANIPS.register("ferric_transmutation",
			() -> register(new FerricTransmutationManip("ferric_transmutation", 1000, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.BODY)));

	public static final RegistryObject<BloodManipulation> activation_potential = MANIPS.register("activation_potential",
			() -> register(new ActivationPotentialManip("activation_potential", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY)));

	public static final RegistryObject<BloodManipulation> sanguine_ward = MANIPS.register("sanguine_ward",
			() -> register(new SanguineWardManip("sanguine_ward", 5, 0, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY)));

//	public static final RegistryObject<BloodManipulation> conjure_blade = MANIPS.register("conjure_blade",
//			() -> register(new ManipConjuration("conjure_blade", ItemInit.living_blade, 1000, 0, 0,
//					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM)));
//

	public static final RegistryObject<BloodManipulation> conjure_blood_absorbtion = MANIPS
			.register("conjure_blood_absorbtion",
					() -> register(new ConjurationManip("conjure_blood_absorbtion", ItemInit.blood_absorption,
							1000, 0, 0, EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC,
							EnumVeinSections.RIGHTARM)));

	public static final RegistryObject<BloodManipulation> conjure_blood_projection = MANIPS
			.register("conjure_blood_projection",
					() -> register(new ConjurationManip("conjure_blood_projection", ItemInit.blood_projection,
							1000, 0, 0, EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC,
							EnumVeinSections.RIGHTARM)));

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
