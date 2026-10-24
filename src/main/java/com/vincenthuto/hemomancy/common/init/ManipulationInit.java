package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.util.RegistryLookup;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.manipulation.animus.*;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.*;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.*;
import com.vincenthuto.hemomancy.common.manipulation.ferric.*;
import com.vincenthuto.hemomancy.common.manipulation.flammeus.*;
import com.vincenthuto.hemomancy.common.manipulation.lux.*;
import com.vincenthuto.hemomancy.common.manipulation.mortem.*;
import com.vincenthuto.hemomancy.common.manipulation.saint.BloomOfRotManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.CrimsonTitheManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.EndlessHourManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.UnclosingEyeManip;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.*;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class ManipulationInit {

	public static final ResourceKey<Registry<BloodManipulation>> MANIP_KEY = ResourceKey
			.createRegistryKey(Hemomancy.rloc("bloodmanipulations"));

	public static final DeferredRegister<BloodManipulation> MANIPS = DeferredRegister.create(MANIP_KEY,
			Hemomancy.MOD_ID);

	public static final Registry<BloodManipulation> MANIPS_TYPE_REGISTRY = MANIPS
			.makeRegistry(builder -> builder.maxId(Integer.MAX_VALUE - 1)
					.defaultKey(Hemomancy.rloc("blood_shot")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> venous_travel = MANIPS.register("venous_travel",
			() -> new BloodManipulation("venous_travel", 1000, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(20)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_shot = MANIPS.register("blood_shot",
			() -> new BloodShotManip("blood_shot", 100, 0, 0, EnumManipulationType.QUICK, EnumManipulationRank.HUMILIS,
					EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)
					.setCooldownTicks(10)
					.setEntityCast(EntityCasts::bloodShot)
					.setDrudgeAction(DrudgeActions::bloodShot, Component.translatable("manipulation.hemomancy.blood_shot.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> guided_blood_shot = MANIPS.register("guided_blood_shot",
			() -> new BloodShotManip("guided_blood_shot", 140, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD,
					BloodShotManip.Mode.GUIDED)
					.setCooldownTicks(20)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> hematic_mortar = MANIPS.register("hematic_mortar",
			() -> new BloodShotManip("hematic_mortar", 200, 0, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD,
					BloodShotManip.Mode.MORTAR)
					.setCooldownTicks(60)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_halo = MANIPS.register("sanguine_halo",
			() -> new BloodShotManip("sanguine_halo", 220, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD,
					BloodShotManip.Mode.HALO)
					.setCooldownTicks(120)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_binding = MANIPS.register("blood_binding",
			() -> new BloodBindingManip("blood_binding", 125, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.LEGS)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(60)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> lingering_blood_binding = MANIPS.register("lingering_blood_binding",
			() -> new BloodBindingManip("lingering_blood_binding", 165, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.LEGS,
					BloodBindingManip.Mode.LINGERING)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(80)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> chain_blood_binding = MANIPS.register("chain_blood_binding",
			() -> new BloodBindingManip("chain_blood_binding", 225, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.LEGS,
					BloodBindingManip.Mode.CHAIN)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(100)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_marionette = MANIPS.register("sanguine_marionette",
            com.vincenthuto.hemomancy.common.manipulation.ductilis.SanguineMarionetteManip::new);

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_lattice = MANIPS.register("blood_lattice",
			() -> new BloodBindingManip("blood_lattice", 275, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.ANIMUS, EnumVeinSections.LEGS,
					BloodBindingManip.Mode.LATTICE)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(140)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> deadly_gaze = MANIPS.register("deadly_gaze",
			() -> new DeadlyGazeManip("deadly_gaze", 100, 0, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.TENEBRIS)
					.setCooldownTicks(20)
					.setEntityCast(EntityCasts::deadlyGaze)
					.setDrudgeAction(DrudgeActions::deadlyGaze, Component.translatable("manipulation.hemomancy.deadly_gaze.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> hematic_rebuke = MANIPS.register("hematic_rebuke",
			() -> new HematicCommandManip("hematic_rebuke", 225, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD, false)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(100)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> hematic_riposte = MANIPS.register("hematic_riposte",
			() -> new HematicRiposteManip("hematic_riposte", 60, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.ANIMUS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(40)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> hematic_impressment = MANIPS.register("hematic_impressment",
			() -> new HematicCommandManip("hematic_impressment", 500, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD, true)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(200)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> summon_avatar = MANIPS.register("summon_avatar",
			() -> new SummonAvatarManip("summon_avatar", 75, 50, 0, EnumManipulationType.PASSIVE,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setCooldownTicks(0)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> summon_avatar_arms = MANIPS.register("summon_avatar_arms",
			() -> new SummonAvatarManip("summon_avatar_arms", 125, 50, 0, EnumManipulationType.PASSIVE,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setCooldownTicks(0)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> summon_avatar_armor = MANIPS.register("summon_avatar_armor",
			() -> new SummonAvatarManip("summon_avatar_armor", 175, 50, 0, EnumManipulationType.PASSIVE,
					EnumManipulationRank.SUMMA, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setCooldownTicks(0)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> summon_avatar_legs = MANIPS.register("summon_avatar_legs",
			() -> new SummonAvatarManip("summon_avatar_legs", 250, 50, 0, EnumManipulationType.PASSIVE,
					EnumManipulationRank.SUMMA, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setCooldownTicks(0)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> summon_avatar_complete = MANIPS.register("summon_avatar_complete",
			() -> new SummonAvatarManip("summon_avatar_complete", 400, 50, 0, EnumManipulationType.PASSIVE,
					EnumManipulationRank.MAGISTER, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setCooldownTicks(0)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_needle = MANIPS.register("blood_needle",
			() -> new BloodNeedleManip("blood_needle", 100, 0, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FERRIC, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(10)
					.setEntityCast(EntityCasts::bloodNeedle)
					.setDrudgeAction(DrudgeActions::bloodNeedle, Component.translatable("manipulation.hemomancy.blood_needle.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_needle_fan = MANIPS.register("blood_needle_fan",
			() -> new BloodNeedleManip("blood_needle_fan", 140, 0, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FERRIC, EnumVeinSections.HEAD,
					BloodNeedleManip.Mode.FAN)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(20)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_needle_lance = MANIPS.register("blood_needle_lance",
			() -> new BloodNeedleManip("blood_needle_lance", 150, 0, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FERRIC, EnumVeinSections.HEAD,
					BloodNeedleManip.Mode.LANCE)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(25)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_cloud = MANIPS.register("blood_cloud",
			() -> new BloodCloudManip("blood_cloud", 300, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(40)
					.setEntityCast(EntityCasts::bloodCloud)
					.setDrudgeAction(DrudgeActions::bloodCloud, Component.translatable("manipulation.hemomancy.blood_cloud.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> expansive_blood_cloud = MANIPS.register("expansive_blood_cloud",
			() -> new BloodCloudManip("expansive_blood_cloud", 375, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD,
					BloodCloudManip.Mode.EXPANSIVE)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(60)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> pursuing_blood_cloud = MANIPS.register("pursuing_blood_cloud",
			() -> new BloodCloudManip("pursuing_blood_cloud", 425, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD,
					BloodCloudManip.Mode.PURSUING)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(80)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_tempest = MANIPS.register("sanguine_tempest",
			() -> new BloodCloudManip("sanguine_tempest", 600, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MAGISTER, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD,
					BloodCloudManip.Mode.TEMPEST)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(120)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_rush = MANIPS.register("blood_rush",
			() -> new BloodRushManip("blood_rush", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.FLAMMEUS)
					.setCooldownTicks(160)
					.setDrudgeAction(DrudgeActions::bloodRush, Component.translatable("manipulation.hemomancy.blood_rush.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_aneurysm = MANIPS.register("blood_aneurysm",
			() -> new BloodAneurysmManip("blood_aneurysm", 400, 25, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.SUMMA, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(40)
					.setEntityCast(EntityCasts::bloodAneurysm)
					.setDrudgeAction(DrudgeActions::bloodAneurysm, Component.translatable("manipulation.hemomancy.blood_aneurysm.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> vital_effusion = MANIPS.register("vital_effusion",
			() -> new VitalEffusionManip("vital_effusion", 350, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(60)
					.setDrudgeAction(DrudgeActions::vitalEffusion, Component.translatable("manipulation.hemomancy.vital_effusion.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> ferric_transmutation = MANIPS.register("ferric_transmutation",
			() -> new FerricTransmutationManip("ferric_transmutation", 1000, 50, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.FERRIC, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(20)
					.setDrudgeAction(DrudgeActions::ferricTransmutation, Component.translatable("manipulation.hemomancy.ferric_transmutation.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> activation_potential = MANIPS.register("activation_potential",
			() -> new ActivationPotentialManip("activation_potential", 200, 10, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY)
					.setCooldownTicks(30)
					.setEntityCast(EntityCasts::activationPotential)
					.setDrudgeAction(DrudgeActions::activationPotential, Component.translatable("manipulation.hemomancy.activation_potential.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> synaptic_jolt = MANIPS.register("synaptic_jolt",
			() -> new SynapticJoltManip("synaptic_jolt", 150, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD)
					.setCooldownTicks(25)
					.setEntityCast(EntityCasts::synapticJolt)
					.setDrudgeAction(DrudgeActions::synapticJolt, Component.translatable("manipulation.hemomancy.synaptic_jolt.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conductive_mark = MANIPS.register("conductive_mark",
			() -> new ConductiveMarkManip("conductive_mark", 150, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setCooldownTicks(50)
					.setEntityCast(EntityCasts::conductiveMark)
					.setDrudgeAction(DrudgeActions::conductiveMark, Component.translatable("manipulation.hemomancy.conductive_mark.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_ward = MANIPS.register("sanguine_ward",
			() -> new SanguineWardManip("sanguine_ward", 40, 10, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setCooldownTicks(20)
					.setDrudgeAction(DrudgeActions::sanguineWard, Component.translatable("manipulation.hemomancy.sanguine_ward.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> hemolymphal_pulse = MANIPS.register("hemolymphal_pulse",
			() -> new HemolymphalPulseManip("hemolymphal_pulse", 175, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(200)
					.setEntityCast(EntityCasts::hemolymphalPulse)
					.setDrudgeAction(DrudgeActions::hemolymphalPulse, Component.translatable("manipulation.hemomancy.hemolymphal_pulse.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_blade = MANIPS.register("conjure_blade",
			() -> new StaffWeaponFormManip("conjure_blade", ItemInit.living_blade, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.ANIMUS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_axe = MANIPS.register("conjure_axe",
			() -> new StaffWeaponFormManip("conjure_axe", ItemInit.living_axe, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_spear = MANIPS.register("conjure_spear",
			() -> new StaffWeaponFormManip("conjure_spear", ItemInit.living_spear, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.LUX, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_claws = MANIPS.register("conjure_claws",
			() -> new StaffWeaponFormManip("conjure_claws", ItemInit.living_baghnakh, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_crossbow = MANIPS.register("conjure_crossbow",
			() -> new StaffWeaponFormManip("conjure_crossbow", ItemInit.living_crossbow, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.DUCTILIS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_torch = MANIPS.register("conjure_torch",
			() -> new StaffWeaponFormManip("conjure_torch", ItemInit.living_torch, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.LUX)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_flail = MANIPS.register("conjure_flail",
			() -> new StaffWeaponFormManip("conjure_flail", ItemInit.living_flail, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.CONGEATIO, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_sickle = MANIPS.register("conjure_sickle",
			() -> new StaffWeaponFormManip("conjure_sickle", ItemInit.living_sickle, 0, 0,
					EnumManipulationRank.PERFECTUS, EnumBloodTendency.MORTEM, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> conjure_staff = MANIPS.register("conjure_staff",
			() -> new ConjurationManip("conjure_staff", ItemInit.living_staff, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(40)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));


	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_absorption = MANIPS.register("blood_absorption",
			() -> new ConjurationManip("blood_absorption", ItemInit.blood_absorption, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(40)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_projection = MANIPS.register("blood_projection",
			() -> new ConjurationManip("blood_projection", ItemInit.blood_projection, 1000, 0, 0,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(40)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> summon_thrall = MANIPS.register("summon_thrall",
			() -> new SummonThrallManip("summon_thrall", 500, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.ANIMUS, EnumVeinSections.BODY)
					.setCooldownTicks(60)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> crimson_flame_conjuration = MANIPS.register("crimson_flame_conjuration",
			() -> new CrimsonFlameConjurationManip("crimson_flame_conjuration", 150, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(15)
					.setEntityCast(EntityCasts::crimsonFlameConjuration)
					.setDrudgeAction(DrudgeActions::crimsonFlameConjuration, Component.translatable("manipulation.hemomancy.crimson_flame_conjuration.drudge")));

	// ── Utilitarian Manipulations ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_mending = MANIPS.register("sanguine_mending",
			() -> new SanguineMendingManip("sanguine_mending", 150, 0, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(30)
					.setDrudgeAction(DrudgeActions::sanguineMending, Component.translatable("manipulation.hemomancy.sanguine_mending.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> hemosynthesis = MANIPS.register("hemosynthesis",
			() -> new HemosynthesisManip("hemosynthesis", 200, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.LUX, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(40)
					.setDrudgeAction(DrudgeActions::hemosynthesis, Component.translatable("manipulation.hemomancy.hemosynthesis.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_lamp = MANIPS.register("blood_lamp",
			() -> new BloodLampManip("blood_lamp", 75, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.LUX, EnumVeinSections.ARMS)
					.setCooldownTicks(10)
					.setDrudgeAction(DrudgeActions::bloodLamp, Component.translatable("manipulation.hemomancy.blood_lamp.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> hematic_flare = MANIPS.register("hematic_flare",
			() -> new HematicFlareManip("hematic_flare", 125, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.LUX, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.FLAMMEUS)
					.setCooldownTicks(30)
					.setEntityCast(EntityCasts::hematicFlare)
					.setDrudgeAction(DrudgeActions::hematicFlare, Component.translatable("manipulation.hemomancy.hematic_flare.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> crimson_harvest = MANIPS.register("crimson_harvest",
			() -> new CrimsonHarvestManip("crimson_harvest", 200, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.DUCTILIS, EnumVeinSections.LEGS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(60)
					.setDrudgeAction(DrudgeActions::crimsonHarvest, Component.translatable("manipulation.hemomancy.crimson_harvest.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> glacial_grasp = MANIPS.register("glacial_grasp",
			() -> new GlacialGraspManip("glacial_grasp", 125, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.CONGEATIO, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.TENEBRIS)
					.setCooldownTicks(20)
					.setEntityCast(EntityCasts::glacialGrasp)
					.setDrudgeAction(DrudgeActions::glacialGrasp, Component.translatable("manipulation.hemomancy.glacial_grasp.drudge")));

	// ── Mid-Game Utilitarian Manipulations (MEDIOCRITAS) ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_excavation = MANIPS.register("sanguine_excavation",
			() -> new SanguineExcavationManip("sanguine_excavation", 400, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(40)
					.setDrudgeAction(DrudgeActions::sanguineExcavation, Component.translatable("manipulation.hemomancy.sanguine_excavation.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> vascular_dowsing = MANIPS.register("vascular_dowsing",
			() -> new VascularDowsingManip("vascular_dowsing", 75, 0, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.LUX)
					.setCooldownTicks(40)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> ferric_resonance = MANIPS.register("ferric_resonance",
			() -> new FerricResonanceManip("ferric_resonance", 600, 20, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FLAMMEUS)
					.setCooldownTicks(200)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> iron_retort = MANIPS.register("iron_retort",
			() -> new IronRetortManip("iron_retort", 250, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(80)
					.setEntityCast(EntityCasts::ironRetort)
					.setDrudgeAction(DrudgeActions::ironRetort, Component.translatable("manipulation.hemomancy.iron_retort.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> ironhearted = MANIPS.register("ironhearted",
			() -> new IronheartedManip("ironhearted", 300, 45, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.MAGISTER, EnumBloodTendency.FERRIC, EnumVeinSections.HEART)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(40)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_magnetism = MANIPS.register("sanguine_magnetism",
			() -> new SanguineMagnetismManip("sanguine_magnetism", 450, 20, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.FERRIC, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(140)
					.setEntityCast(EntityCasts::sanguineMagnetism)
					.setDrudgeAction(DrudgeActions::sanguineMagnetism, Component.translatable("manipulation.hemomancy.sanguine_magnetism.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> ferric_rampart = MANIPS.register("ferric_rampart",
			() -> new FerricBarrierManip("ferric_rampart", FerricConstructShapes.Kind.WALL)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS).setCooldownTicks(140)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.ferric_rampart.drudge")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> ferric_spikes = MANIPS.register("ferric_spikes",
			() -> new FerricBarrierManip("ferric_spikes", FerricConstructShapes.Kind.SPIKE)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS).setCooldownTicks(140)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.ferric_spikes.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> pyretic_forge = MANIPS.register("pyretic_forge",
			() -> new PyreticForgeManip("pyretic_forge", 150, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setCooldownTicks(30)
					.setEntityCast(EntityCasts::pyreticForge)
					.setDrudgeAction(DrudgeActions::pyreticForge, Component.translatable("manipulation.hemomancy.pyretic_forge.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> umbral_step = MANIPS.register("umbral_step",
			() -> new UmbralStepManip("umbral_step", 300, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.LEGS)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(40)
					.setEntityCast(EntityCasts::umbralStep)
					.setDrudgeAction(DrudgeActions::umbralStep, Component.translatable("manipulation.hemomancy.umbral_step.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> crimson_sight = MANIPS.register("crimson_sight",
			() -> new CrimsonSightManip("crimson_sight", 250, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.LUX, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.TENEBRIS)
					.setCooldownTicks(60)
					.setDrudgeAction(DrudgeActions::crimsonSight, Component.translatable("manipulation.hemomancy.crimson_sight.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> prismatic_reproof = MANIPS.register("prismatic_reproof",
			() -> new PrismaticReproofManip("prismatic_reproof", 325, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.LUX, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setCooldownTicks(50)
					.setEntityCast(EntityCasts::prismaticReproof)
					.setDrudgeAction(DrudgeActions::prismaticReproof, Component.translatable("manipulation.hemomancy.prismatic_reproof.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> hematic_beacon = MANIPS.register("hematic_beacon",
			() -> new HematicBeaconManip("hematic_beacon", 350, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.LUX, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(160)
					.setEntityCast(EntityCasts::hematicBeacon)
					.setDrudgeAction(DrudgeActions::hematicBeacon, Component.translatable("manipulation.hemomancy.hematic_beacon.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> lumen_suture = MANIPS.register("lumen_suture",
			() -> new LumenSutureManip("lumen_suture", 250, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.LUX, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(120)
					.setDrudgeAction(DrudgeActions::lumenSuture, Component.translatable("manipulation.hemomancy.lumen_suture.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> vital_reservoir = MANIPS.register("vital_reservoir",
			() -> new VitalReservoirManip("vital_reservoir", 50, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.MORTEM, EnumVeinSections.HEART)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(60)
					.setDrudgeAction(DrudgeActions::vitalReservoir, Component.translatable("manipulation.hemomancy.vital_reservoir.drudge")));

	// ── CONGEATIO — expanded tendencies ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> cryogenic_pulse = MANIPS.register("cryogenic_pulse",
			() -> new CryogenicPulseManip("cryogenic_pulse", 150, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.CONGEATIO, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.FLAMMEUS)
					.setCooldownTicks(30)
					.setEntityCast(EntityCasts::cryogenicPulse)
					.setDrudgeAction(DrudgeActions::cryogenicPulse, Component.translatable("manipulation.hemomancy.cryogenic_pulse.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> glacial_circulation = MANIPS.register("glacial_circulation",
			() -> new GlacialCirculationManip("glacial_circulation", 175, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.CONGEATIO, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(100));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> glacial_bastion = MANIPS.register("glacial_bastion",
			() -> new GlacialBastionManip("glacial_bastion", 350, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.CONGEATIO, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setCooldownTicks(50)
					.setEntityCast(EntityCasts::glacialBastion)
					.setDrudgeAction(DrudgeActions::glacialBastion, Component.translatable("manipulation.hemomancy.glacial_bastion.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> glacial_rampart = MANIPS.register("glacial_rampart",
			() -> new GlacialRampartManip("glacial_rampart", 350, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.CONGEATIO, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setCooldownTicks(50)
					.setEntityCast(EntityCasts::glacialRampart)
					.setDrudgeAction(DrudgeActions::glacialRampart, Component.translatable("manipulation.hemomancy.glacial_rampart.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> osseous_bloom = MANIPS.register("osseous_bloom",
			() -> new OsseousBloomManip("osseous_bloom", 600, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.CONGEATIO, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(60));

	// ── FLAMMEUS — expanded tendencies ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> sanguine_ignition = MANIPS.register("sanguine_ignition",
			() -> new SanguineIgnitionManip("sanguine_ignition", 125, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(25)
					.setEntityCast(EntityCasts::sanguineIgnition)
					.setDrudgeAction(DrudgeActions::sanguineIgnition, Component.translatable("manipulation.hemomancy.sanguine_ignition.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> vitric_combustion = MANIPS.register("vitric_combustion",
			() -> new VitricCombustionManip("vitric_combustion", 350, 25, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.SUMMA, EnumBloodTendency.FLAMMEUS, EnumVeinSections.BODY)
					.setCooldownTicks(60)
					.setEntityCast(EntityCasts::vitricCombustion)
					.setDrudgeAction(DrudgeActions::vitricCombustion, Component.translatable("manipulation.hemomancy.vitric_combustion.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> cauterizing_rebuke = MANIPS.register("cauterizing_rebuke",
			() -> new CauterizingRebukeManip("cauterizing_rebuke", 150, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(90)
					.setDrudgeAction(DrudgeActions::cauterizingRebuke, Component.translatable("manipulation.hemomancy.cauterizing_rebuke.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> scalding_updraft = MANIPS.register("scalding_updraft",
			() -> new ScaldingUpdraftManip("scalding_updraft", 225, 5, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.LEGS)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(80)
					.setEntityCast(EntityCasts::scaldingUpdraft)
					.setDrudgeAction(DrudgeActions::scaldingUpdraft, Component.translatable("manipulation.hemomancy.scalding_updraft.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> soaring_updraft = MANIPS.register("soaring_updraft",
			() -> new ScaldingUpdraftManip("soaring_updraft", 300, 5, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.LEGS,
					ScaldingUpdraftManip.Mode.SOARING)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(100)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> suspended_updraft = MANIPS.register("suspended_updraft",
			() -> new ScaldingUpdraftManip("suspended_updraft", 350, 5, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.LEGS,
					ScaldingUpdraftManip.Mode.SUSPENDED)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(120)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> expulsive_updraft = MANIPS.register("expulsive_updraft",
			() -> new ScaldingUpdraftManip("expulsive_updraft", 450, 5, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.LEGS,
					ScaldingUpdraftManip.Mode.EXPULSIVE)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(140)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	// ── TENEBRIS — expanded tendencies ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> void_shroud = MANIPS.register("void_shroud",
			() -> new VoidShroudManip("void_shroud", 100, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.TENEBRIS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(20)
					.setEntityCast(EntityCasts::voidShroud)
					.setDrudgeAction(DrudgeActions::voidShroud, Component.translatable("manipulation.hemomancy.void_shroud.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> gloam_laceration = MANIPS.register("gloam_laceration",
			() -> new GloamLacerationManip("gloam_laceration", 175, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.TENEBRIS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(35)
					.setEntityCast(EntityCasts::gloamLaceration)
					.setDrudgeAction(DrudgeActions::gloamLaceration, Component.translatable("manipulation.hemomancy.gloam_laceration.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_eclipse = MANIPS.register("blood_eclipse",
			() -> new BloodEclipseManip("blood_eclipse", 300, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.LUX)
					.setCooldownTicks(45)
					.setEntityCast(EntityCasts::bloodEclipse)
					.setDrudgeAction(DrudgeActions::bloodEclipse, Component.translatable("manipulation.hemomancy.blood_eclipse.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> black_veil_covenant = MANIPS.register("black_veil_covenant",
			() -> new BlackVeilCovenantManip("black_veil_covenant", 425, 15, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(900)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> umbral_reversal = MANIPS.register("umbral_reversal",
			() -> new UmbralReversalManip("umbral_reversal", 375, 15, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.LEGS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(100)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blood_eclipse_mantle = MANIPS.register("blood_eclipse_mantle",
			() -> new BloodEclipseMantleManip("blood_eclipse_mantle", 325, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.TENEBRIS, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.FLAMMEUS)
					.setCooldownTicks(180)
					.setEntityCast(EntityCasts::bloodEclipseMantle)
					.setDrudgeAction(DrudgeActions::bloodEclipseMantle, Component.translatable("manipulation.hemomancy.blood_eclipse_mantle.drudge")));

	// ── MORTEM — expanded tendencies ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> hemorrhage = MANIPS.register("hemorrhage",
			() -> new HemorrhageManip("hemorrhage", 75, 0, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.MORTEM, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(20)
					.setEntityCast(EntityCasts::hemorrhage)
					.setDrudgeAction(DrudgeActions::hemorrhage, Component.translatable("manipulation.hemomancy.hemorrhage.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> lignum_mortis = MANIPS.register("lignum_mortis",
			() -> new LignumMortisManip("lignum_mortis", 100, 0, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.MORTEM, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(20)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> canopy_mortis = MANIPS.register("canopy_mortis",
			() -> new LignumMortisManip("canopy_mortis", 125, 0, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.MORTEM, EnumVeinSections.ARMS,
					LignumMortisManip.Mode.CANOPY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(30)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> worked_lignum = MANIPS.register("worked_lignum",
			() -> new LignumMortisManip("worked_lignum", 175, 0, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.MORTEM, EnumVeinSections.ARMS,
					LignumMortisManip.Mode.WORKED)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(40)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> exsanguinate = MANIPS.register("exsanguinate",
			() -> new ExsanguinateManip("exsanguinate", 300, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.MORTEM, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.DUCTILIS)
					.setCooldownTicks(50)
					.setEntityCast(EntityCasts::exsanguinate)
					.setDrudgeAction(DrudgeActions::exsanguinate, Component.translatable("manipulation.hemomancy.exsanguinate.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> insatiable_hunger = MANIPS.register("insatiable_hunger",
			() -> new InsatiableHungerManip("insatiable_hunger", 225, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.HUMILIS, EnumBloodTendency.MORTEM, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(70)
					.setEntityCast(EntityCasts::insatiableHunger)
					.setDrudgeAction(DrudgeActions::insatiableHunger, Component.translatable("manipulation.hemomancy.insatiable_hunger.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> grave_debt = MANIPS.register("grave_debt",
			() -> new GraveDebtManip("grave_debt", 325, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.MORTEM, EnumVeinSections.HEART)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(75)
					.setEntityCast(EntityCasts::graveDebt)
					.setDrudgeAction(DrudgeActions::graveDebt, Component.translatable("manipulation.hemomancy.grave_debt.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> blackhearted = MANIPS.register("blackhearted",
			() -> new BlackheartedManip("blackhearted", 0, 35, 0, EnumManipulationType.PASSIVE,
					EnumManipulationRank.MAGISTER, EnumBloodTendency.MORTEM, EnumVeinSections.HEART)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> crimson_coronation = MANIPS.register("crimson_coronation",
			() -> new CrimsonCoronationManip("crimson_coronation", 600, 70, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.PERFECTUS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD)
					.setCooldownTicks(120).setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> sovereign_instinct = MANIPS.register("sovereign_instinct",
			() -> new SovereignInstinctManip("sovereign_instinct", 450, 60, 0, EnumManipulationType.PASSIVE,
					EnumManipulationRank.MAGISTER, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> axonal_transduction = MANIPS.register("axonal_transduction",
			AxonalTransductionManip::new);
	public static final DeferredHolder<BloodManipulation, BloodManipulation> synaptic_storm = MANIPS.register("synaptic_storm",
			() -> new SynapticStormManip("synaptic_storm", 900, 65, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.PERFECTUS, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD)
					.setCooldownTicks(100).setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> living_circuit = MANIPS.register("living_circuit",
			() -> new LivingCircuitManip("living_circuit", 100, 55, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MAGISTER, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY)
					.setCooldownTicks(80).setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> white_verdict = MANIPS.register("white_verdict",
			() -> new WhiteVerdictManip("white_verdict", 800, 65, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.PERFECTUS, EnumBloodTendency.LUX, EnumVeinSections.HEAD)
					.setCooldownTicks(100).setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> vigil_of_glass = MANIPS.register("vigil_of_glass",
			() -> new VigilOfGlassManip("vigil_of_glass", 300, 55, 0, EnumManipulationType.PASSIVE,
					EnumManipulationRank.MAGISTER, EnumBloodTendency.LUX, EnumVeinSections.HEAD)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> furnace_veins = MANIPS.register("furnace_veins",
			() -> new FurnaceVeinsManip("furnace_veins", 150, 55, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MAGISTER, EnumBloodTendency.FLAMMEUS, EnumVeinSections.BODY)
					.setCooldownTicks(100).setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> phoenix_debt = MANIPS.register("phoenix_debt",
			() -> new PhoenixDebtManip("phoenix_debt", 1200, 75, 0, EnumManipulationType.PASSIVE,
					EnumManipulationRank.PERFECTUS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.HEART)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> absolute_stillness = MANIPS.register("absolute_stillness",
			() -> new AbsoluteStillnessManip("absolute_stillness", 300, 65, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.PERFECTUS, EnumBloodTendency.CONGEATIO, EnumVeinSections.BODY)
					.setCooldownTicks(160).setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> rimebound_sentence = MANIPS.register("rimebound_sentence",
			() -> new RimeboundSentenceManip("rimebound_sentence", 750, 55, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.MAGISTER, EnumBloodTendency.CONGEATIO, EnumVeinSections.ARMS)
					.setCooldownTicks(120).setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> hematic_ballast = MANIPS.register("hematic_ballast",
			() -> new HematicBallastManip("hematic_ballast", 200, 55, 0, EnumManipulationType.PASSIVE,
					EnumManipulationRank.MAGISTER, EnumBloodTendency.FERRIC, EnumVeinSections.LEGS)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> iron_choir = MANIPS.register("iron_choir",
			() -> new IronChoirManip("iron_choir", 250, 60, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MAGISTER, EnumBloodTendency.FERRIC, EnumVeinSections.BODY)
					.setCooldownTicks(100).setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> funeral_bell = MANIPS.register("funeral_bell",
			() -> new FuneralBellManip("funeral_bell", 1000, 75, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.PERFECTUS, EnumBloodTendency.MORTEM, EnumVeinSections.HEART)
					.setCooldownTicks(160).setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> carrion_communion = MANIPS.register("carrion_communion",
			() -> new CarrionCommunionManip("carrion_communion", 220, 60, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MAGISTER, EnumBloodTendency.MORTEM, EnumVeinSections.BODY)
					.setCooldownTicks(120).setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> penumbral_drift = MANIPS.register("penumbral_drift",
			() -> new PenumbralDriftManip("penumbral_drift", 75, 55, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MAGISTER, EnumBloodTendency.TENEBRIS, EnumVeinSections.LEGS)
					.setCooldownTicks(60).setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> eclipse_well = MANIPS.register("eclipse_well",
			() -> new EclipseWellManip("eclipse_well", 900, 65, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.PERFECTUS, EnumBloodTendency.TENEBRIS, EnumVeinSections.HEAD)
					.setCooldownTicks(140).setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	// ── Tier-gap fills (see docs/MANIPULATION_BALANCE_PLAN.md §6) ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> searing_lash = MANIPS.register("searing_lash",
			() -> new SearingLashManip("searing_lash", 225, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(30)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> cruor_javelin = MANIPS.register("cruor_javelin",
			() -> new CruorJavelinManip("cruor_javelin", 200, 10, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.CONGEATIO, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.FERRIC)
					.setCooldownTicks(25)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> fruiting_contagion = MANIPS.register("fruiting_contagion",
			() -> new FruitingContagionManip("fruiting_contagion", 400, 20, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.MORTEM, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(80)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> umbral_rend = MANIPS.register("umbral_rend",
			() -> new UmbralRendManip("umbral_rend", 450, 20, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.TENEBRIS, EnumVeinSections.ARMS)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(80)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> radiant_wake = MANIPS.register("radiant_wake",
			() -> new RadiantWakeManip("radiant_wake", 120, 55, 0, EnumManipulationType.CONTINUOUS,
					EnumManipulationRank.MAGISTER, EnumBloodTendency.LUX, EnumVeinSections.LEGS)
					.setCooldownTicks(80).setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));
	public static final DeferredHolder<BloodManipulation, BloodManipulation> lodestone_collapse = MANIPS.register("lodestone_collapse",
			() -> new LodestoneCollapseManip("lodestone_collapse", 900, 65, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.PERFECTUS, EnumBloodTendency.FERRIC, EnumVeinSections.BODY)
					.setCooldownTicks(140).setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> thread_ripper = MANIPS.register("thread_ripper",
			() -> new ThreadRipperManip("thread_ripper", 35, 0, 0, EnumManipulationType.CHARGED,
					EnumManipulationRank.SUMMA, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.TENEBRIS)
					.setCooldownTicks(240)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.thread_ripper.drudge")));

	// ── SAINT — Canon Memories (imprinted from Sainted Mausoleums) ──

	public static final DeferredHolder<BloodManipulation, BloodManipulation> crimson_tithe = MANIPS.register("crimson_tithe",
			() -> new CrimsonTitheManip("crimson_tithe", 100, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.MORTEM, EnumVeinSections.HEART)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(100)
					.setDrudgeAction(DrudgeAction.DRUDGE_UNSUPPORTED, Component.translatable("manipulation.hemomancy.drudge_unsupported")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> unclosing_eye = MANIPS.register("unclosing_eye",
			() -> new UnclosingEyeManip("unclosing_eye", 350, 20, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.LUX, EnumVeinSections.HEAD)
					.setSecondaryTend(EnumBloodTendency.MORTEM)
					.setCooldownTicks(120)
					.setEntityCast(EntityCasts::unclosingEye)
					.setDrudgeAction(DrudgeActions::unclosingEye, Component.translatable("manipulation.hemomancy.unclosing_eye.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> bloom_of_rot = MANIPS.register("bloom_of_rot",
			() -> new BloomOfRotManip("bloom_of_rot", 500, 25, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.MORTEM, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.ANIMUS)
					.setCooldownTicks(80)
					.setEntityCast(EntityCasts::bloomOfRot)
					.setDrudgeAction(DrudgeActions::bloomOfRot, Component.translatable("manipulation.hemomancy.bloom_of_rot.drudge")));

	public static final DeferredHolder<BloodManipulation, BloodManipulation> endless_hour = MANIPS.register("endless_hour",
			() -> new EndlessHourManip("endless_hour", 600, 30, 0, EnumManipulationType.QUICK,
					EnumManipulationRank.SUMMA, EnumBloodTendency.CONGEATIO, EnumVeinSections.BODY)
					.setSecondaryTend(EnumBloodTendency.LUX)
					.setCooldownTicks(200)
					.setDrudgeAction(DrudgeActions::endlessHour, Component.translatable("manipulation.hemomancy.endless_hour.drudge")));

	public static List<BloodManipulation> getAllEntries() {
		List<BloodManipulation> entries = new ArrayList<>();
		Registry<BloodManipulation> registry = MANIPS_TYPE_REGISTRY;
		if (registry != null) {
			registry.forEach(entries::add);
		}
		return entries;
	}

	public static BloodManipulation getByName(String name) {
		return RegistryLookup.byName(MANIPS_TYPE_REGISTRY, name).orElse(null);
	}
}
