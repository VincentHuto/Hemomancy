package com.vincenthuto.hemomancy.common.init;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodAneurysmManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodCloudManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodNeedleManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodRushManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.BloodShotManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.CrimsonFlameConjurationManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.DeadlyGazeManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.SummonAvatarManip;
import com.vincenthuto.hemomancy.common.manipulation.animus.SummonThrallManip;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.CryogenicPulseManip;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialBastionManip;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialGraspManip;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.ActivationPotentialManip;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.CrimsonHarvestManip;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.SanguineWardManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.ConjurationManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricTransmutationManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.SanguineExcavationManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.SanguineMendingManip;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.PyreticForgeManip;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.SanguineIgnitionManip;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.VitricCombustionManip;
import com.vincenthuto.hemomancy.common.manipulation.lux.BloodLampManip;
import com.vincenthuto.hemomancy.common.manipulation.lux.CrimsonSightManip;
import com.vincenthuto.hemomancy.common.manipulation.lux.HemosynthesisManip;
import com.vincenthuto.hemomancy.common.manipulation.mortem.ExsanguinateManip;
import com.vincenthuto.hemomancy.common.manipulation.mortem.HemorrhageManip;
import com.vincenthuto.hemomancy.common.manipulation.mortem.VitalReservoirManip;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.BloodEclipseManip;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.UmbralStepManip;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.VoidShroudManip;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ManipulationInit {

	public static final ResourceKey<Registry<BloodManipulation>> MANIP_KEY = ResourceKey
			.createRegistryKey(Hemomancy.rloc("bloodmanipulations"));

	public static final DeferredRegister<BloodManipulation> MANIPS = DeferredRegister.create(MANIP_KEY,
			Hemomancy.MOD_ID);

	public static Supplier<IForgeRegistry<BloodManipulation>> MANIPS_TYPE_REGISTRY = MANIPS
			.makeRegistry(() -> new RegistryBuilder<BloodManipulation>().setMaxID(Integer.MAX_VALUE - 1)
					.setDefaultKey(Hemomancy.rloc("bloodmanipulations")));

	public static final RegistryObject<BloodManipulation> venous_travel = MANIPS.register("venous_travel",
			() -> new BloodManipulation("venous_travel", 1000, 0, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM)
					.setCooldownTicks(20));

	public static final RegistryObject<BloodManipulation> blood_shot = MANIPS.register("blood_shot",
			() -> new BloodShotManip("blood_shot", 100, 0, 0, EnumManipulationType.QUICK, EnumManipulationRank.HUMILIS,
					EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)
					.setCooldownTicks(10));

	public static final RegistryObject<BloodManipulation> deadly_gaze = MANIPS.register("deadly_gaze",
			() -> new DeadlyGazeManip("deadly_gaze", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)
					.setCooldownTicks(20));

	public static final RegistryObject<BloodManipulation> summon_avatar = MANIPS.register("summon_avatar",
			() -> new SummonAvatarManip("summon_avatar", 500, 50, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setCooldownTicks(100));

	public static final RegistryObject<BloodManipulation> blood_needle = MANIPS.register("blood_needle",
			() -> new BloodNeedleManip("blood_needle", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)
					.setCooldownTicks(10));

	public static final RegistryObject<BloodManipulation> blood_cloud = MANIPS.register("blood_cloud",
			() -> new BloodCloudManip("blood_cloud", 300, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)
					.setCooldownTicks(40));

	public static final RegistryObject<BloodManipulation> blood_rush = MANIPS.register("blood_rush",
			() -> new BloodRushManip("blood_rush", 100, 0, 0, EnumManipulationType.PASSIVE,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setCooldownTicks(60));

	public static final RegistryObject<BloodManipulation> blood_aneurysm = MANIPS.register("blood_aneurysm",
			() -> new BloodAneurysmManip("blood_aneurysm", 400, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setCooldownTicks(40));

	public static final RegistryObject<BloodManipulation> ferric_transmutation = MANIPS.register("ferric_transmutation",
			() -> new FerricTransmutationManip("ferric_transmutation", 1000, 50, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.FERRIC, EnumVeinSections.BODY)
					.setCooldownTicks(20));

	public static final RegistryObject<BloodManipulation> activation_potential = MANIPS.register("activation_potential",
			() -> new ActivationPotentialManip("activation_potential", 200, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY)
					.setCooldownTicks(30));

	public static final RegistryObject<BloodManipulation> sanguine_ward = MANIPS.register("sanguine_ward",
			() -> new SanguineWardManip("sanguine_ward", 10, 10, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY)
					.setCooldownTicks(20));

	public static final RegistryObject<BloodManipulation> conjure_blade = MANIPS.register("conjure_blade",
			() -> new ConjurationManip("conjure_blade", ItemInit.living_blade, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM)
					.setCooldownTicks(40));


	public static final RegistryObject<BloodManipulation> blood_absorption = MANIPS.register("blood_absorption",
			() -> new ConjurationManip("blood_absorption", ItemInit.blood_absorption, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM)
					.setCooldownTicks(40));

	public static final RegistryObject<BloodManipulation> blood_projection = MANIPS.register("blood_projection",
			() -> new ConjurationManip("blood_projection", ItemInit.blood_projection, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM)
					.setCooldownTicks(40));

	public static final RegistryObject<BloodManipulation> summon_thrall = MANIPS.register("summon_thrall",
			() -> new SummonThrallManip("summon_thrall", 500, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setCooldownTicks(60));

	public static final RegistryObject<BloodManipulation> crimson_flame_conjuration = MANIPS.register("crimson_flame_conjuration",
			() -> new CrimsonFlameConjurationManip("crimson_flame_conjuration", 150, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.RIGHTARM)
					.setCooldownTicks(15));

	// ── Utilitarian Manipulations ──

	public static final RegistryObject<BloodManipulation> sanguine_mending = MANIPS.register("sanguine_mending",
			() -> new SanguineMendingManip("sanguine_mending", 150, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM)
					.setCooldownTicks(30));

	public static final RegistryObject<BloodManipulation> hemosynthesis = MANIPS.register("hemosynthesis",
			() -> new HemosynthesisManip("hemosynthesis", 200, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.LUX, EnumVeinSections.BODY)
					.setCooldownTicks(40));

	public static final RegistryObject<BloodManipulation> blood_lamp = MANIPS.register("blood_lamp",
			() -> new BloodLampManip("blood_lamp", 75, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.LUX, EnumVeinSections.LEFTARM)
					.setCooldownTicks(10));

	public static final RegistryObject<BloodManipulation> crimson_harvest = MANIPS.register("crimson_harvest",
			() -> new CrimsonHarvestManip("crimson_harvest", 200, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.DUCTILIS, EnumVeinSections.LEFTLEG)
					.setCooldownTicks(60));

	public static final RegistryObject<BloodManipulation> glacial_grasp = MANIPS.register("glacial_grasp",
			() -> new GlacialGraspManip("glacial_grasp", 125, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.CONGEATIO, EnumVeinSections.LEFTARM)
					.setCooldownTicks(20));

	// ── Mid-Game Utilitarian Manipulations (MEDIOCRITAS) ──

	public static final RegistryObject<BloodManipulation> sanguine_excavation = MANIPS.register("sanguine_excavation",
			() -> new SanguineExcavationManip("sanguine_excavation", 400, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.RIGHTARM)
					.setCooldownTicks(40));

	public static final RegistryObject<BloodManipulation> pyretic_forge = MANIPS.register("pyretic_forge",
			() -> new PyreticForgeManip("pyretic_forge", 350, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.BODY)
					.setCooldownTicks(30));

	public static final RegistryObject<BloodManipulation> umbral_step = MANIPS.register("umbral_step",
			() -> new UmbralStepManip("umbral_step", 300, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.LEFTLEG)
					.setCooldownTicks(40));

	public static final RegistryObject<BloodManipulation> crimson_sight = MANIPS.register("crimson_sight",
			() -> new CrimsonSightManip("crimson_sight", 250, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.LUX, EnumVeinSections.HEAD)
					.setCooldownTicks(60));

	public static final RegistryObject<BloodManipulation> vital_reservoir = MANIPS.register("vital_reservoir",
			() -> new VitalReservoirManip("vital_reservoir", 50, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.MORTEM, EnumVeinSections.HEART)
					.setCooldownTicks(60));

	// ── CONGEATIO — expanded tendencies ──

	public static final RegistryObject<BloodManipulation> cryogenic_pulse = MANIPS.register("cryogenic_pulse",
			() -> new CryogenicPulseManip("cryogenic_pulse", 150, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.CONGEATIO, EnumVeinSections.BODY)
					.setCooldownTicks(30));

	public static final RegistryObject<BloodManipulation> glacial_bastion = MANIPS.register("glacial_bastion",
			() -> new GlacialBastionManip("glacial_bastion", 350, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.CONGEATIO, EnumVeinSections.LEFTARM)
					.setCooldownTicks(50));

	// ── FLAMMEUS — expanded tendencies ──

	public static final RegistryObject<BloodManipulation> sanguine_ignition = MANIPS.register("sanguine_ignition",
			() -> new SanguineIgnitionManip("sanguine_ignition", 125, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.BODY)
					.setCooldownTicks(25));

	public static final RegistryObject<BloodManipulation> vitric_combustion = MANIPS.register("vitric_combustion",
			() -> new VitricCombustionManip("vitric_combustion", 500, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.FLAMMEUS, EnumVeinSections.BODY)
					.setCooldownTicks(60));

	// ── TENEBRIS — expanded tendencies ──

	public static final RegistryObject<BloodManipulation> void_shroud = MANIPS.register("void_shroud",
			() -> new VoidShroudManip("void_shroud", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.TENEBRIS, EnumVeinSections.BODY)
					.setCooldownTicks(20));

	public static final RegistryObject<BloodManipulation> blood_eclipse = MANIPS.register("blood_eclipse",
			() -> new BloodEclipseManip("blood_eclipse", 300, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.HEAD)
					.setCooldownTicks(45));

	// ── MORTEM — expanded tendencies ──

	public static final RegistryObject<BloodManipulation> hemorrhage = MANIPS.register("hemorrhage",
			() -> new HemorrhageManip("hemorrhage", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.MORTEM, EnumVeinSections.RIGHTARM)
					.setCooldownTicks(20));

	public static final RegistryObject<BloodManipulation> exsanguinate = MANIPS.register("exsanguinate",
			() -> new ExsanguinateManip("exsanguinate", 300, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.MORTEM, EnumVeinSections.RIGHTARM)
					.setCooldownTicks(50));

	public static List<BloodManipulation> getAllEntries() {
		List<BloodManipulation> entries = new ArrayList<>();
		IForgeRegistry<BloodManipulation> registry = MANIPS_TYPE_REGISTRY.get();
		if (registry != null) {
			entries.addAll(registry.getValues());
		}
		return entries;
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
