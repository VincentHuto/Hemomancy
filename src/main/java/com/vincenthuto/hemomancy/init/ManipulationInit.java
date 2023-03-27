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

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ManipulationInit {

	public static List<BloodManipulation> MANIPULATIONS = new ArrayList<>();

	public static final ResourceKey<Registry<BloodManipulation>> MANIP_KEY = ResourceKey
			.createRegistryKey(Hemomancy.rloc("bloodmanipulations"));

	public static final DeferredRegister<BloodManipulation> MANIPS = DeferredRegister.create(MANIP_KEY,
			Hemomancy.MOD_ID);

	public static Supplier<IForgeRegistry<BloodManipulation>> MANIPS_TYPE_REGISTRY = MANIPS
			.makeRegistry(() -> new RegistryBuilder<BloodManipulation>().setMaxID(Integer.MAX_VALUE - 1)
					.setDefaultKey(Hemomancy.rloc("bloodmanipulations")));

	public static final RegistryObject<BloodManipulation> venous_travel = MANIPS.register("venous_travel",
			() -> new ConjurationManip("venous_travel", ItemInit.venous_recaller, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM));

	public static final RegistryObject<BloodManipulation> blood_shot = MANIPS.register("blood_shot",
			() -> new BloodShotManip("blood_shot", 100, 0, 0, EnumManipulationType.QUICK, EnumManipulationRank.HUMILIS,
					EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD));

	public static final RegistryObject<BloodManipulation> deadly_gaze = MANIPS.register("deadly_gaze",
			() -> new DeadlyGazeManip("deadly_gaze", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD));

	public static final RegistryObject<BloodManipulation> summon_avatar = MANIPS.register("summon_avatar",
			() -> new SummonAvatarManip("summon_avatar", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY));

	public static final RegistryObject<BloodManipulation> blood_needle = MANIPS.register("blood_needle",
			() -> new BloodNeedleManip("blood_needle", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD));

	public static final RegistryObject<BloodManipulation> blood_cloud = MANIPS.register("blood_cloud",
			() -> new BloodCloudManip("blood_cloud", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD));

	public static final RegistryObject<BloodManipulation> blood_rush = MANIPS.register("blood_rush",
			() -> new BloodRushManip("blood_rush", 100, 0, 0, EnumManipulationType.PASSIVE,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY));

	public static final RegistryObject<BloodManipulation> blood_aneurysm = MANIPS.register("blood_aneurysm",
			() -> new BloodAneurysmManip("blood_aneurysm", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY));

	public static final RegistryObject<BloodManipulation> ferric_transmutation = MANIPS.register("ferric_transmutation",
			() -> new FerricTransmutationManip("ferric_transmutation", 1000, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.BODY));

	public static final RegistryObject<BloodManipulation> activation_potential = MANIPS.register("activation_potential",
			() -> new ActivationPotentialManip("activation_potential", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY));

	public static final RegistryObject<BloodManipulation> sanguine_ward = MANIPS.register("sanguine_ward",
			() -> new SanguineWardManip("sanguine_ward", 5, 0, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY));

//	public static final RegistryObject<BloodManipulation> conjure_blade = MANIPS.register("conjure_blade",
//			() -> new ManipConjuration("conjure_blade", ItemInit.living_blade, 1000, 0, 0,
//					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM));
//

	public static final RegistryObject<BloodManipulation> blood_absorption = MANIPS.register("blood_absorption",
			() -> new ConjurationManip("blood_absorption", ItemInit.blood_absorption, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM));

	public static final RegistryObject<BloodManipulation> blood_projection = MANIPS.register("blood_projection",
			() -> new ConjurationManip("blood_projection", ItemInit.blood_projection, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM));

	public static List<BloodManipulation> getAllEntries() {
		List<BloodManipulation> blocks = new ArrayList<>();
		MANIPS.getEntries().stream().map(RegistryObject::get).forEach(b -> blocks.add(b));
		return blocks;
	}

	public static BloodManipulation getByName(String name) {
		for (BloodManipulation manip : getAllEntries()) {
			if (name.equals(manip.getName())) {
				return manip;
			}
		}
		return null;
	}
}
